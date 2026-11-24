# Remnants: Ballistics

A firearms mod for Minecraft built for multiplayer: sixteen hand-modelled weapons, a server-authoritative ballistics engine, gunsmithing with real trade-offs, and first-person handling that feels like a modern shooter.

![Remnants: Ballistics](marketing/images/banner.png)

## Features

- **16 weapons in 7 classes:** pistols, a revolver, SMGs, assault rifles, shotguns, bolt-action and anti-materiel snipers, LMGs, and two experimental energy weapons. Each has its own stats, fire modes, sound profile, rarity, durability and crafting tier. See [docs/WEAPONS.md](docs/WEAPONS.md).
- **Shooting that holds up online:**
  - Server-validated hitscan with bullet drop, damage falloff and head, body and leg zones.
  - Penetration through soft blocks, shallow-angle ricochets and glass that shatters.
  - Lag-compensated hitboxes and a shared spread seed, so what you see is what the server scores.
- **Handling:**
  - Aim-down-sights with true sight alignment; hip fire and ADS accuracy.
  - Movement, crouch and jump penalties; bloom, recoil and recovery.
  - Weapon sway and bob, fire-mode switching, tactical and empty reloads, single-round loading, pump and bolt cycling, and a charged railgun.
- **Gunsmithing:** a Gunsmith Workbench with optic, muzzle, underbarrel, magazine, stock and skin slots. A live stat preview shows every attachment's trade-offs before you commit. See [docs/ATTACHMENTS.md](docs/ATTACHMENTS.md).
- **Presentation:**
  - Voxel weapons built from up to 118 elements each, with crisp 32×32 icons.
  - First-person viewmodels with per-weapon keyframed animations: equip, idle, inspect, fire, reloads, pump and bolt cycling, charge.
  - Animated muzzle flashes, tracers, per-material impacts, bullet holes, ejected brass and shells, hit markers.
  - 59 sound events: real-recording style gunshots and handling sounds made with [ElevenLabs](https://elevenlabs.io), synthesized impacts, UI and energy weapons.
- **Progression:** steel and brass, an ammunition press, gun parts and precision parts, blueprints found in structures, remnant alloy and void cores. Loot is injected into 25 vanilla structure and mob tables.
- **Configurable:** client visuals, HUD and accessibility; common recipe and loot toggles; server balance, hit zones, anti-cheat tolerances and per-weapon overrides. See [docs/CONFIG.md](docs/CONFIG.md).

## Supported versions

These are the only supported targets (the NightBeam convention): 8 jars per release, all with the same version.

| Minecraft | Loaders | Java | Folder |
| --- | --- | --- | --- |
| 1.20.1 | Forge, Fabric | 17 bytecode (build on 21) | `1.20.1/{common,forge,fabric}` |
| 1.21.1 | NeoForge, Fabric | 21 | `1.21.1/{common,neoforge,fabric}` |
| 26.2 | NeoForge, Fabric | 25 | `26.2/{common,neoforge,fabric}` |
| 26.3 | NeoForge, Fabric | 25 | `26.3/{common,neoforge,fabric}` |

| Minecraft | Forge / NeoForge jar | Fabric jar |
| --- | --- | --- |
| 1.20.1 | Forge 47.1+ (built against 47.4.28) | Loader 0.16.14+, Fabric API, Forge Config API Port 8.0.3+ |
| 1.21.1 | NeoForge 21.1.264+ | Loader 0.16.14+, Fabric API, Forge Config API Port 21.1.6+ |
| 26.2 | NeoForge 26.2.0.93+ | Loader 0.19.5+, Fabric API, Forge Config API Port 26.2.1+ |
| 26.3 | NeoForge 26.3.0.99+ | Loader 0.19.5+, Fabric API, Forge Config API Port 26.3.1+ |

Jar names are `remnants_ballistics-<loader>-<minecraft>-<version>.jar`; match both the Minecraft version and the loader. Each version folder is an independent Gradle project with `common`, `fabric` and `neoforge` subprojects (`forge` instead of `neoforge` on 1.20.1), like the other NightBeam mods: gameplay, rendering, assets and data live in `common`, and each loader project only adds its entry points, registration, networking and event hooks. The release version is `0.6.0` (`version=` in each workspace's `gradle.properties`; `./gradlew syncVersion -PmodVersion=x.y.z` sets all of them). See [the release notes](patchnotes/0.6.0.md).

## Requirements

- JDK 21 to build `1.20.1/` and `1.21.1/` (`1.20.1/` compiles for, and runs the game on, Java 17, which Gradle provisions if it is not installed), JDK 25 to build `26.2/` and `26.3/`
- Minecraft 1.20.1, 1.21.1, 26.2 or 26.3 with either:
  - **Forge** 47.1+ on 1.20.1, or **NeoForge** (21.1.264+ on 1.21.1, 26.2.0.93+ on 26.2, 26.3.0.99+ on 26.3); no other mods required, or
  - **Fabric** Loader (0.16.14+ on 1.20.1 and 1.21.1, 0.19.5+ on 26.x) with [Fabric API](https://modrinth.com/mod/fabric-api) and [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (8.0.3+ on 1.20.1, 21.1.6+, 26.2.1+ or 26.3.1+). [Mod Menu](https://modrinth.com/mod/modmenu) is optional and opens the config screen (1.21.1 and 26.x; on 1.20.1 the configs are edited as TOML files).
- Install on **both the server and every client**.

## Build

From the repository root:

```
./gradlew buildAll              # checks the generated assets, builds every workspace, copies the 8 loader jars to releases/
./gradlew build263              # one workspace (keys: 1201, 1211, 262, 263)
./gradlew gameTestAll           # GameTests of every workspace on both loaders
./gradlew checkVersions         # fails unless every workspace has the same version=
```

From a workspace (`1.20.1`, `1.21.1`, `26.2` or `26.3`):

```
cd 26.3
./gradlew build                                              # both loader jars + unit tests
./gradlew :neoforge:runGameTestServer :fabric:runGameTestServer  # in-game GameTests on headless servers (:forge: on 1.20.1)
```

The root build hands the 26.x workspaces `JAVA_HOME_25_X64` (or `-Pjava25Home=<jdk>`) and the others `JAVA_HOME_21_X64` (or `-Pjava21Home=<jdk>`); without them each workspace uses the current `JAVA_HOME`. The release jars are `<game>/neoforge/build/libs/remnants_ballistics-neoforge-<game>-<version>.jar` (on 1.20.1 `1.20.1/forge/build/libs/remnants_ballistics-forge-1.20.1-<version>.jar`, reobfuscated to Forge's SRG names; the development jar is in `forge/build/devlibs`) and `<game>/fabric/build/libs/remnants_ballistics-fabric-<game>-<version>.jar`, one pair per workspace. `python3 scripts/check_targets.py workspaces` checks the 8 targets, the shared version and the Minecraft ranges (each loader's metadata stays inside its own Minecraft line). Release notes for a version live in `patchnotes/<version>.md`.

## Releasing

CI (`.github/workflows/ci.yml`) builds and tests every workspace on every push to `main` that touches the build, checks that exactly the 8 jars come out, and refreshes the `dev-build` prerelease on GitHub for testing. Publishing to CurseForge is `.github/workflows/publish.yml`, run by hand (`version`, `dry_run`) or by a `v*.*.*` tag; it never runs on a plain push. It refuses a second release of the mod on the same day (Africa/Tunis), takes the jars from `releases/`, the GitHub Release or a build from source, fails unless exactly the 8 jars are found and verified, and uploads each with `patchnotes/<version>.md` as its changelog. It needs the secrets `CURSEFORGE_TOKEN` (upload token) and `CURSEFORGE_API_KEY` (Core API key) and the variable `CURSEFORGE_ID` (it falls back to this project's ID, `1759493`). NightBeam publishes to CurseForge only; the Modrinth page stays up but gets no new versions. Prove a change with a dry run first: `gh workflow run publish.yml -f version=<version> -f dry_run=true`.

## Run

```
cd 26.3                         # or 1.20.1 (use :forge: instead of :neoforge:), 1.21.1, 26.2
./gradlew :neoforge:runClient   # development client (or :fabric:runClient)
./gradlew :neoforge:runServer   # dedicated server (or :fabric:runServer; set online-mode=false in server.properties for dev clients)
./gradlew :neoforge:runClient2  # second client "Dev2" for two-player testing
```

See [docs/MULTIPLAYER_TESTING.md](docs/MULTIPLAYER_TESTING.md) for latency and packet-loss testing.

## Controls

| Action | Default |
| --- | --- |
| Fire | Left mouse (Attack) |
| Aim down sights | Right mouse (Use), hold or toggle |
| Reload | `R` |
| Switch fire mode | `V` |
| Inspect weapon | `I` |

Charge the Eclipse Railgun by holding fire. See [docs/CONTROLS.md](docs/CONTROLS.md).

## Assets

Every model, texture, icon, animation, sound, GUI and data file is generated from source by the scripts in [`tools/assets`](tools/assets) into `1.21.1/common/src/main/resources`, shared by both loaders, and ported to the 1.20.1 formats in `1.20.1/` and to the 26.x formats in `26.2/` and `26.3/` by `tools/assets/port_resources.py`. All assets are original; the firearm sounds are generated with ElevenLabs. The asset set is cross-checked against the Java registries:

```
python3 tools/assets/generate.py        # regenerate everything (needs Python 3, numpy, Pillow and ffmpeg)
python3 tools/assets/port_resources.py  # only re-port to 1.20.1/26.2/26.3 (standard library only; generate.py runs it)
python3 tools/assets/check_assets.py    # validate (also run by ./gradlew buildAll)
```

Weapon models also export to `tools/assets/blockbench/` for editing in Blockbench. See [docs/ASSET_PIPELINE.md](docs/ASSET_PIPELINE.md) and the [asset manifest](docs/ASSET_MANIFEST.md).

## Documentation

- [Feature list](docs/FEATURES.md)
- [Weapons](docs/WEAPONS.md)
- [Attachments](docs/ATTACHMENTS.md)
- [Crafting and progression](docs/PROGRESSION.md)
- [Configuration](docs/CONFIG.md)
- [Controls](docs/CONTROLS.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Multiplayer testing](docs/MULTIPLAYER_TESTING.md)
- [Known limitations](docs/KNOWN_LIMITATIONS.md)
- [Changelog](CHANGELOG.md)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md).

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) and [NOTICE](NOTICE).
