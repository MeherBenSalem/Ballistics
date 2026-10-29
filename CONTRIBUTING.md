# Contributing to Remnants: Ballistics

Thank you for your interest in contributing!

## How to contribute

1. **Fork** the repository and create a feature branch from `main`.
2. Make your changes in every version workspace (`1.21.1/`, `26.2/`, `26.3/`); the code is the same apart from the Minecraft and loader APIs. Gameplay maths that does not need Minecraft belongs in the `core` package, with unit tests.
3. Assets are generated. Change the sources in `tools/assets`, not the files in `<workspace>/common/src/main/resources`, then run (`generate.py` also ports the result to the 26.x formats with `port_resources.py`):
   ```
   python3 tools/assets/generate.py
   python3 tools/assets/check_assets.py --manifest docs/ASSET_MANIFEST.md
   python3 tools/docs_gen.py
   ```
4. **Build and test** to verify nothing is broken:
   ```
   ./gradlew buildAll
   ./gradlew gameTestAll           # or, in one workspace: ./gradlew :neoforge:runGameTestServer :fabric:runGameTestServer
   ```
5. Open a **pull request** with a clear description of the change.

## Coding expectations

- Follow the existing code style and package structure.
- `<workspace>/common` compiles against vanilla Minecraft only. Loader-specific code (registration, events, networking, mixins) belongs in `<workspace>/neoforge` or `<workspace>/fabric`, reached from common code through `platform.Services` or a hook method both loaders call.
- The server owns every gameplay outcome. Client code may predict, but must never decide damage, ammunition or timing.
- Client-only classes must stay out of common code paths so dedicated servers never load them.
- Test your changes in game, including on a dedicated server with a second client (see [docs/MULTIPLAYER_TESTING.md](docs/MULTIPLAYER_TESTING.md)).
- Only original assets: no textures, models or sounds from Minecraft, other mods or games.

## Reporting issues

Use GitHub Issues. Include your Minecraft version, loader (NeoForge or Fabric) and its version, mod version and steps to reproduce.

## License

Unless you explicitly state otherwise, any contribution you intentionally submit for inclusion in this project shall be licensed under the Apache License 2.0, without any additional terms or conditions.
