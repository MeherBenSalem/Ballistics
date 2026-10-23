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

| Workspace | Minecraft | Loaders | Java |
| --- | --- | --- | --- |
| `1.21.1/` | 1.21.1 | NeoForge 21.1.264+, Fabric (Loader 0.16.14+) | 21 |

Each version folder is an independent Gradle project with `common`, `fabric` and `neoforge` subprojects, like the other NightBeam mods: gameplay, rendering, assets and data live in `common`, and each loader project only adds its entry points, registration, networking and event hooks. The release version is `0.4.0` (`version=` in `1.21.1/gradle.properties`). See [the release notes](patchnotes/0.4.0.md).

## Requirements

- JDK 21
- Minecraft 1.21.1 with either:
  - **NeoForge** 21.1.264 or newer (no other mods required), or
  - **Fabric** Loader 0.16.14 or newer with [Fabric API](https://modrinth.com/mod/fabric-api) and [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) 21.1.6 or newer. [Mod Menu](https://modrinth.com/mod/modmenu) is optional and opens the config screen.
- Install on **both the server and every client**.

## Build

From the workspace:

```
cd 1.21.1
./gradlew build                                              # Fabric and NeoForge jars + unit tests
./gradlew :neoforge:runGameTestServer :fabric:runGameTestServer  # in-game GameTests on headless servers
```

From the repository root:

```
./gradlew buildAll              # checks the generated assets, builds every workspace, copies jars to releases/
```

The release jars are `1.21.1/neoforge/build/libs/remnants_ballistics-neoforge-1.21.1-<version>.jar` and `1.21.1/fabric/build/libs/remnants_ballistics-fabric-1.21.1-<version>.jar`.

## Run

```
cd 1.21.1
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

Every model, texture, icon, animation, sound, GUI and data file is generated from source by the scripts in [`tools/assets`](tools/assets) into `1.21.1/common/src/main/resources`, shared by both loaders. All assets are original; the firearm sounds are generated with ElevenLabs. The asset set is cross-checked against the Java registries:

```
python3 tools/assets/generate.py        # regenerate everything (needs Python 3, numpy, Pillow and ffmpeg)
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
