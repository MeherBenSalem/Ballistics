# Remnants: Ballistics 1.21.1

Fabric and NeoForge workspace for Minecraft 1.21.1, in the MultiLoader layout of the other NightBeam mods.

| Path | Contents |
| --- | --- |
| `common/src/main/java` | Mod code (`com.nightbeam.remnants.ballistics`), compiled against vanilla Minecraft only |
| `common/src/main/resources` | Generated assets and data shared by both loaders (do not hand-edit; see `../tools/assets`) |
| `common/src/test/java` | JUnit tests for the Minecraft-free core |
| `neoforge/` | NeoForge entry points, registration, payloads, configs, event hooks, loot modifier and GameTest registration |
| `fabric/` | Fabric entry points, registration, payloads, configs (Forge Config API Port), callbacks, client mixins, loot injection and GameTest registration |
| `smoketest/java` | The scripted client run shared by both loaders' `src/smoketest` companion mods (development only) |
| `buildSrc/` | `multiloader-common` and `multiloader-loader` Gradle conventions |

Java 21 is required. ModDevGradle and Loom download Minecraft, NeoForge and Fabric on the first build.

```
./gradlew build                          # both loader jars + unit tests
./gradlew :neoforge:runClient            # development clients (also :fabric:runClient)
./gradlew :neoforge:runClient2           # second NeoForge client "Dev2"
./gradlew :neoforge:runServer            # dedicated servers (also :fabric:runServer)
./gradlew :neoforge:runGameTestServer    # GameTests, exits with a non-zero code on failure (also :fabric:)
./gradlew :neoforge:runClientSmokeTest   # scripted client run with screenshots (also :fabric:)
```

On Windows use `gradlew.bat`. The jars are written to `neoforge/build/libs/remnants_ballistics-neoforge-1.21.1-<version>.jar` and `fabric/build/libs/remnants_ballistics-fabric-1.21.1-<version>.jar`.
