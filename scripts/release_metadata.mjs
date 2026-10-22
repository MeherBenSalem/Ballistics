import path from "node:path";

// Store project IDs. Fill these in once the Modrinth and CurseForge projects exist, or provide MODRINTH_ID /
// CURSEFORGE_ID in the environment (the publish workflow passes the repository variables of the same names).
export const MODRINTH_ID = "";
export const CURSEFORGE_ID = "";
export const MOD_ID = "remnants_ballistics";
export const MOD_TITLE = "Remnants: Ballistics";
export const LOADER_IDS = Object.freeze({ fabric: 7499, forge: 7498, neoforge: 10150 });

// Runtime requirements besides the loader and Minecraft, keyed by mod id (as declared in fabric.mod.json or
// mods.toml). Modrinth project ids verified at https://modrinth.com/project/<id>; CurseForge relations use slugs.
export const DEPENDENCY_PROJECTS = Object.freeze({
  "fabric-api": Object.freeze({ modrinth: "P7dR8mSH", curseforge: "fabric-api" }),
  forgeconfigapiport: Object.freeze({ modrinth: "ohNO6lps", curseforge: "forge-config-api-port-fabric" }),
});

// Keep in sync with each loader's descriptor (<version>/<loader>/src/main/resources); the tests compare them.
export const LOADER_DEPENDENCIES = Object.freeze({
  fabric: Object.freeze(["fabric-api", "forgeconfigapiport"]),
  forge: Object.freeze([]),
  neoforge: Object.freeze([]),
});

export const SUPPORTED_TARGETS = Object.freeze(["neoforge:1.21.1", "fabric:1.21.1"]);
const SUPPORTED = new Set(SUPPORTED_TARGETS);

export function assertCompleteRelease(releases) {
  const targets = new Set(releases.map((p) => `${p.loader}:${p.game}`));
  const missing = SUPPORTED_TARGETS.filter((target) => !targets.has(target));
  if (missing.length) throw new Error(`Missing release targets: ${missing.join(", ")}`);
}

export function normalizeVersion(value) {
  const version = String(value ?? "").replace(/^v/, "");
  if (!/^\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?$/.test(version)) {
    throw new Error("--version must be a release version such as 0.3.0");
  }
  return version;
}

/** 0.x versions and versions with a pre-release suffix are published as beta files. */
export function releaseType(version) {
  version = normalizeVersion(version);
  return version.startsWith("0.") || version.includes("-") ? "beta" : "release";
}

function dependencies(loader, game) {
  if (!SUPPORTED.has(`${loader}:${game}`)) {
    throw new Error(`Unsupported release target ${loader} ${game}`);
  }
  return LOADER_DEPENDENCIES[loader];
}

export function modrinthDependencies(loader, game) {
  return dependencies(loader, game).map((mod) => ({
    project_id: DEPENDENCY_PROJECTS[mod].modrinth,
    version_id: null,
    file_name: null,
    dependency_type: "required",
  }));
}

export function curseforgeRelations(loader, game) {
  return { projects: dependencies(loader, game).map((mod) => ({ slug: DEPENDENCY_PROJECTS[mod].curseforge, type: "requiredDependency" })) };
}

export function parseJar(jar, expectedVersion) {
  const name = path.basename(jar);
  const match = name.match(
    /^remnants_ballistics-(neoforge|fabric|forge)-(\d+\.\d+(?:\.\d+)?)-(\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?)\.jar$/,
  );
  if (!match) throw new Error(`Cannot parse release jar ${name}`);
  const [, loader, game, version] = match;
  modrinthDependencies(loader, game); // Fail closed instead of guessing a target.
  if (expectedVersion && version !== normalizeVersion(expectedVersion)) {
    throw new Error(`Jar version ${version} does not match ${expectedVersion}: ${name}`);
  }
  return { jar, name, loader, game, version };
}

export function modrinthMetadata(release, { version, changelog, projectId = MODRINTH_ID }) {
  version = normalizeVersion(version);
  if (release.version !== version) throw new Error(`Jar version does not match ${version}`);
  return {
    name: `${version} · ${release.loader} · ${release.game}`,
    version_number: `${version}+${release.loader}-${release.game}`,
    changelog,
    dependencies: modrinthDependencies(release.loader, release.game),
    game_versions: [release.game],
    version_type: releaseType(version),
    loaders: [release.loader],
    featured: false,
    status: "listed",
    project_id: projectId,
    file_parts: ["file_0"],
    primary_file: "file_0",
  };
}
