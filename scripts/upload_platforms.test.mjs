import assert from "node:assert/strict";
import fs from "node:fs";
import os from "node:os";
import path from "node:path";
import { spawnSync } from "node:child_process";
import { fileURLToPath, pathToFileURL } from "node:url";
import test from "node:test";
import { DEPENDENCY_PROJECTS, LOADER_DEPENDENCIES, LOADER_IDS, SUPPORTED_TARGETS, curseforgeRelations,
  modrinthDependencies, modrinthMetadata, parseJar, releaseType } from "./release_metadata.mjs";
import { parseArgs, uploadPlatforms } from "./upload_platforms.mjs";

const ROOT = fileURLToPath(new URL("../", import.meta.url));
const TARGETS = SUPPORTED_TARGETS.map((target) => target.split(":"));
const VERSION = "0.3.0";
const jarName = (loader, game, version = VERSION) => `remnants_ballistics-${loader}-${game}-${version}.jar`;
const silent = () => {};
const ids = { MODRINTH_ID: "mrTest01", CURSEFORGE_ID: "424242" };
const tokens = { ...ids, MODRINTH_TOKEN: "mock-token", CURSEFORGE_TOKEN: "mock-token", CURSEFORGE_API_KEY: "mock-key" };

function fixture(t, version = VERSION) {
  const dir = fs.mkdtempSync(path.join(os.tmpdir(), "ballistics-publisher-"));
  t.after(() => fs.rmSync(dir, { recursive: true, force: true }));
  for (const [loader, game] of TARGETS) fs.writeFileSync(path.join(dir, jarName(loader, game, version)), "fixture jar");
  return dir;
}

/** Required dependencies declared in a loader's descriptor, other than the loader, Minecraft and Java. */
function sourceDependencies(loader, game) {
  const resources = path.join(ROOT, game, loader, "src/main/resources");
  if (loader === "fabric") {
    const descriptor = JSON.parse(fs.readFileSync(path.join(resources, "fabric.mod.json"), "utf8"));
    return Object.keys(descriptor.depends).filter((mod) => !["fabricloader", "minecraft", "java"].includes(mod));
  }
  const toml = loader === "neoforge" ? "META-INF/neoforge.mods.toml" : "META-INF/mods.toml";
  const source = fs.readFileSync(path.join(resources, toml), "utf8");
  return source.split(/\[\[dependencies\./).slice(1).flatMap((block) => {
    const mod = block.match(/modId\s*=\s*"([^"]+)"/)?.[1];
    const required = /mandatory\s*=\s*true|type\s*=\s*"required"/.test(block);
    return required && ![loader, "minecraft"].includes(mod) ? [mod] : [];
  });
}

test("every target's payload matches the runtime requirements in its loader descriptor", () => {
  for (const [loader, game] of TARGETS) {
    assert.deepEqual(sourceDependencies(loader, game).sort(), [...LOADER_DEPENDENCIES[loader]].sort(), `${loader} ${game}`);
    const release = parseJar(jarName(loader, game), VERSION);
    const body = modrinthMetadata(release, { version: VERSION, changelog: "Test", projectId: ids.MODRINTH_ID });
    assert.deepEqual(body.dependencies.map((d) => d.project_id), LOADER_DEPENDENCIES[loader].map((m) => DEPENDENCY_PROJECTS[m].modrinth));
    assert.ok(body.dependencies.every((d) => d.dependency_type === "required"));
    assert.deepEqual(curseforgeRelations(loader, game).projects.map((p) => p.slug),
      LOADER_DEPENDENCIES[loader].map((m) => DEPENDENCY_PROJECTS[m].curseforge));
    assert.deepEqual(body.loaders, [loader]);
    assert.deepEqual(body.game_versions, [game]);
    assert.equal(body.version_number, `${VERSION}+${loader}-${game}`);
    assert.equal(body.version_type, "beta");
    assert.equal(body.project_id, ids.MODRINTH_ID);
    assert.deepEqual(body.file_parts, ["file_0"]);
    assert.equal(body.primary_file, "file_0");
  }
});

test("pre-1.0 and suffixed versions publish as beta, others as release", () => {
  assert.equal(releaseType("0.3.0"), "beta");
  assert.equal(releaseType("1.0.0-rc.1"), "beta");
  assert.equal(releaseType("1.0.0"), "release");
});

test("invalid targets, mismatched versions and missing arguments fail closed", () => {
  for (const name of ["unknown.jar", "remnants_ballistics-quilt-1.21.1-0.3.0.jar", "remnants_ballistics-neoforge-1.19.2-0.3.0.jar"]) {
    assert.throws(() => parseJar(name));
  }
  assert.throws(() => parseJar(jarName("neoforge", "1.21.1"), "0.3.1"), /does not match/);
  assert.throws(() => modrinthDependencies("neoforge", "1.22"), /Unsupported/);
  assert.throws(() => parseArgs(["node", "script", "--version"]), /Missing value/);
  assert.throws(() => parseArgs(["node", "script", "--version", VERSION, "--jar-dir", "--dry-run"]), /Missing value/);
  assert.throws(() => parseArgs(["node", "script", "--version", "bad"]), /release version/);
  assert.throws(() => parseArgs(["node", "script", "--version", VERSION, "--unknown"]), /Unknown arg/);
  assert.equal(parseArgs(["node", "script", "--version", `v${VERSION}`]).version, VERSION);
});

test("dry-run emits complete payloads without credentials or network", async (t) => {
  const jarDir = fixture(t);
  const logs = [];
  let calls = 0;
  await uploadPlatforms({ version: VERSION, jarDir, dryRun: true }, {
    env: {}, fetchImpl: async () => { calls++; throw new Error("Network forbidden"); },
    log: (...values) => logs.push(values),
  });
  assert.equal(calls, 0);
  assert.equal(logs.filter(([label]) => label === "[dry-run] Modrinth").length, TARGETS.length);
  const cf = logs.filter(([label]) => label === "[dry-run] CurseForge");
  assert.equal(cf.length, TARGETS.length);
  for (const entry of cf) {
    const preview = JSON.parse(entry[2]);
    const loader = Object.keys(LOADER_IDS).find((l) => LOADER_IDS[l] === preview.gameVersionLookup.loaderId);
    assert.ok(loader, "unknown CurseForge loader id");
    assert.equal(preview.metadata.releaseType, "beta");
    assert.deepEqual(preview.metadata.relations.projects.map((p) => p.slug), LOADER_DEPENDENCIES[loader].map((m) => DEPENDENCY_PROJECTS[m].curseforge));
  }
});

test("CLI dry-run never reads local.env, jar bytes or calls fetch", (t) => {
  const dir = fixture(t);
  const desktop = path.join(dir, "Desktop");
  fs.mkdirSync(desktop);
  fs.writeFileSync(path.join(desktop, "local.env"), "MODRINTH_TOKEN=not-a-token\n");
  const preload = path.join(dir, "forbid-io.mjs");
  fs.writeFileSync(preload, `import fs from 'node:fs';
    const original = fs.readFileSync;
    fs.readFileSync = function(file, ...args) {
      if (String(file).endsWith('local.env') || String(file).endsWith('.jar')) throw new Error('Forbidden read');
      return original.call(this, file, ...args);
    };
    globalThis.fetch = () => { throw new Error('Forbidden network'); };`);
  const env = { ...process.env, USERPROFILE: dir };
  for (const key of ["MODRINTH_TOKEN", "CURSEFORGE_TOKEN", "CURSEFORGE_API_KEY"]) delete env[key];
  const child = spawnSync(process.execPath, ["--import", pathToFileURL(preload).href,
    path.join(ROOT, "scripts/upload_platforms.mjs"), "--version", VERSION, "--jar-dir", dir, "--require-all-targets", "--dry-run"],
  { encoding: "utf8", env });
  assert.equal(child.status, 0, child.stderr);
  assert.equal((child.stdout.match(/\[dry-run\] Modrinth/g) || []).length, TARGETS.length);
  assert.doesNotMatch(child.stdout, /Loaded secrets|not-a-token/);
});

test("full-release and regular-file preflight stops before any upload", async (t) => {
  const jarDir = fixture(t);
  let calls = 0;
  const transport = { env: tokens, fetchImpl: async () => { calls++; }, log: silent };
  const jar = path.join(jarDir, jarName("neoforge", "1.21.1"));
  fs.unlinkSync(jar);
  fs.writeFileSync(path.join(jarDir, jarName("neoforge", "1.21.1", "0.2.0")), "old jar");
  await assert.rejects(uploadPlatforms({ version: VERSION, jarDir, requireAllTargets: true }, transport), /Missing release targets: neoforge:1\.21\.1/);
  assert.equal(calls, 0);
  fs.mkdirSync(jar);
  await assert.rejects(uploadPlatforms({ version: VERSION, jarDir }, transport), /Not a regular jar file/);
  assert.equal(calls, 0);
});

/** Fake CurseForge game version ids. */
const CF_GAME_IDS = Object.freeze({ "1.20.1": 1201, "1.21.1": 1211 });

function mockTransport(records, { modrinthFailure = false, modernLookupFails = false } = {}) {
  return async (url, options = {}) => {
    if (url === "https://api.modrinth.com/v2/version") {
      assert.equal(options.method, "POST");
      assert.ok(options.body instanceof FormData);
      assert.deepEqual([...options.body.keys()], ["data", "file_0"]);
      const body = JSON.parse(options.body.get("data"));
      const file = options.body.get("file_0");
      assert.equal(await file.text(), "fixture jar");
      assert.deepEqual(body, modrinthMetadata(parseJar(file.name), {
        version: VERSION, changelog: body.changelog, projectId: ids.MODRINTH_ID,
      }));
      records.push({ platform: "modrinth", body });
      return new Response(modrinthFailure ? "test rejection" : "{}", { status: modrinthFailure ? 400 : 200 });
    }
    if (url === "https://minecraft.curseforge.com/api/game/versions") {
      return Response.json([{ id: 1, name: "Client" }, { id: 2, name: "Server" },
        ...Object.entries(CF_GAME_IDS).map(([name, id]) => ({ id, name }))]);
    }
    if (url.startsWith("https://api.curseforge.com/v1/minecraft/version/")) {
      const game = decodeURIComponent(url.slice("https://api.curseforge.com/v1/minecraft/version/".length));
      return modernLookupFails ? new Response("unavailable", { status: 503 }) : Response.json({ data: { gameVersionId: CF_GAME_IDS[game] } });
    }
    if (url === `https://minecraft.curseforge.com/api/projects/${ids.CURSEFORGE_ID}/upload-file`) {
      assert.equal(options.method, "POST");
      assert.ok(options.body instanceof FormData);
      const body = JSON.parse(options.body.get("metadata"));
      const file = options.body.get("file");
      const release = parseJar(file.name);
      assert.equal(await file.text(), "fixture jar");
      assert.deepEqual(body.gameVersions, [1, 2, LOADER_IDS[release.loader], CF_GAME_IDS[release.game]]);
      assert.equal(body.releaseType, "beta");
      assert.deepEqual(body.relations, curseforgeRelations(release.loader, release.game));
      records.push({ platform: "curseforge", body });
      return Response.json({ id: 42 });
    }
    throw new Error(`Unexpected network target ${url}`);
  };
}

test("actual upload sends one Modrinth version and one CurseForge file", async (t) => {
  const records = [];
  await uploadPlatforms({ version: VERSION, jarDir: fixture(t), requireAllTargets: true }, {
    env: tokens, fetchImpl: mockTransport(records), log: silent,
  });
  assert.equal(records.filter((r) => r.platform === "modrinth").length, TARGETS.length);
  assert.equal(records.filter((r) => r.platform === "curseforge").length, TARGETS.length);
});

test("CurseForge-only needs no Modrinth token and retains legacy lookup fallback", async (t) => {
  const records = [];
  await uploadPlatforms({ version: VERSION, jarDir: fixture(t), curseforgeOnly: true }, {
    env: { CURSEFORGE_ID: ids.CURSEFORGE_ID, CURSEFORGE_TOKEN: "mock-token", CURSEFORGE_API_KEY: "mock-key" },
    fetchImpl: mockTransport(records, { modernLookupFails: true }), log: silent,
  });
  assert.equal(records.filter((r) => r.platform === "modrinth").length, 0);
  assert.equal(records.filter((r) => r.platform === "curseforge").length, TARGETS.length);
});

test("live mode without project ids, credentials or after an API rejection stops publication", async (t) => {
  const jarDir = fixture(t);
  let called = false;
  await assert.rejects(uploadPlatforms({ version: VERSION, jarDir }, {
    env: { MODRINTH_TOKEN: "x", CURSEFORGE_TOKEN: "x", CURSEFORGE_API_KEY: "x" }, fetchImpl: () => { called = true; }, log: silent,
  }), /Set MODRINTH_ID and CURSEFORGE_ID/);
  await assert.rejects(uploadPlatforms({ version: VERSION, jarDir }, {
    env: ids, fetchImpl: () => { called = true; }, log: silent,
  }), /Set MODRINTH_TOKEN/);
  assert.equal(called, false);
  const records = [];
  await assert.rejects(uploadPlatforms({ version: VERSION, jarDir }, {
    env: tokens, fetchImpl: mockTransport(records, { modrinthFailure: true }), log: silent,
  }), /Modrinth 400/);
  assert.equal(records.length, 1);
});
