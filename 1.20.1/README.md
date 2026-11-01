# Remnants: Ballistics 1.20.1

Forge and Fabric workspace for Minecraft 1.20.1, in the MultiLoader layout of the other NightBeam mods. The code is the
1.21.1 workspace's, ported to the 1.20.1 APIs (see "Version workspaces" in [../docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md)).

| Path | Contents |
| --- | --- |
| `common/src/main/java` | Mod code (`com.nightbeam.remnants.ballistics`), compiled against vanilla Minecraft only |
| `common/src/main/resources` | Assets and data shared by both loaders, ported from 1.21.1 by `../tools/assets/port_resources.py` (do not hand-edit) |
| `common/src/test/java` | JUnit tests for the Minecraft-free core |
| `forge/` | Forge entry points, registration, `SimpleChannel` payloads, configs, event hooks, loot modifier, recipe condition, GameTest registration and the aim-sensitivity mixin |
| `fabric/` | Fabric entry points, registration, payloads, configs (Forge Config API Port 8), callbacks, client mixins, loot injection and GameTest registration |
| `smoketest/java` | The scripted client run shared by both loaders' `src/smoketest` companion mods (development only) |
| `buildSrc/` | `multiloader-common` and `multiloader-loader` Gradle conventions |

Gradle runs on Java 21 (Fabric Loom needs it); the code is compiled for, and the game runs on, Java 17, which Gradle
provisions if it is not installed. ModDevGradle Legacy (Forge, and vanilla MCP mode for `common`) and Loom download
Minecraft, Forge and Fabric on the first build.

```
./gradlew build                          # both loader jars + unit tests
./gradlew :forge:runClient               # development clients (also :fabric:runClient)
./gradlew :forge:runClient2              # second Forge client "Dev2"
./gradlew :forge:runServer               # dedicated servers (also :fabric:runServer)
./gradlew :forge:runGameTestServer       # GameTests, exits with a non-zero code on failure (also :fabric:)
./gradlew :forge:runClientSmokeTest      # scripted client run with screenshots (also :fabric:)
```

On Windows use `gradlew.bat`. The jars are written to `forge/build/libs/remnants_ballistics-forge-1.20.1-<version>.jar`
(reobfuscated to Forge's SRG names; the Mojang-named development jar is in `forge/build/devlibs`) and
`fabric/build/libs/remnants_ballistics-fabric-1.20.1-<version>.jar`.
