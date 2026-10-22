import path from "node:path";

// Store project IDs. Fill these in once the Modrinth and CurseForge projects exist, or provide MODRINTH_ID /
// CURSEFORGE_ID in the environment (the publish workflow passes the repository variables of the same names).
export const MODRINTH_ID = "";
export const CURSEFORGE_ID = "";
export const MOD_ID = "remnants_ballistics";
export const MOD_TITLE = "Remnants: Ballistics";
export const LOADER_IDS = Object.freeze({ neoforge: 10150 });

// Runtime requirements besides NeoForge and Minecraft, keyed by mod id -> Modrinth project id.
// Remnants: Ballistics has none; keep this in sync with 1.21.1/src/main/templates/META-INF/neoforge.mods.toml.
export const DEPENDENCY_PROJECTS = Object.freeze({});

const SUPPORTED_TARGETS = new Set(["neoforge:1.21.1"]);

export function assertCompleteRelease(releases) {
  const targets = new Set(releases.map((p) => `${p.loader}:${p.game}`));
  const missing = [...SUPPORTED_TARGETS].filter((target) => !targets.has(target));
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

export function modrinthDependencies(loader, game) {
  if (!SUPPORTED_TARGETS.has(`${loader}:${game}`)) {
    throw new Error(`Unsupported release target ${loader} ${game}`);
  }
  return Object.values(DEPENDENCY_PROJECTS).map((projectId) => ({
    project_id: projectId,
    version_id: null,
    file_name: null,
    dependency_type: "required",
  }));
}

export function curseforgeRelations(loader, game) {
  const ids = modrinthDependencies(loader, game).map((d) => d.project_id);
  return { projects: Object.entries(DEPENDENCY_PROJECTS)
    .filter(([, id]) => ids.includes(id))
    .map(([slug]) => ({ slug, type: "requiredDependency" })) };
}

export function parseJar(jar, expectedVersion) {
  const name = path.basename(jar);
  const match = name.match(
    /^remnants_ballistics-(neoforge)-(\d+\.\d+\.\d+)-(\d+\.\d+\.\d+(?:-[A-Za-z0-9.]+)?)\.jar$/,
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
