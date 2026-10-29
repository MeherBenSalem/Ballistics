# Remnants: Ballistics 26.2

Fabric and NeoForge workspace for Minecraft 26.2, in the MultiLoader layout of the other NightBeam mods. It mirrors
`../1.21.1/` on the 26.2 APIs: Minecraft is unobfuscated (Mojang names, `Identifier`), Fabric uses the non-remapping
Loom plugin, and rendering goes through the extract/submit pipeline.

| Path | Contents |
| --- | --- |
| `common/src/main/java` | Mod code (`com.nightbeam.remnants.ballistics`), compiled against vanilla Minecraft only |
| `common/src/main/resources` | Assets and data shared by both loaders; `assets/`, `data/` and `pack.mcmeta` are ported from `../1.21.1` by `../tools/assets/port_resources.py` (do not hand-edit them) |
| `common/src/test/java` | JUnit tests for the Minecraft-free core |
| `neoforge/` | NeoForge entry points, registration, payloads, configs, event hooks, loot modifier and GameTest registration |
| `fabric/` | Fabric entry points, registration, payloads, configs (Forge Config API Port), callbacks, client mixins, loot injection and GameTest registration |
| `smoketest/java` | The scripted client run shared by both loaders' `src/smoketest` companion mods (development only) |
| `buildSrc/` | `multiloader-common` and `multiloader-loader` Gradle conventions |

Java 25 is required (Gradle 9 runs its daemon on it; see `gradle/gradle-daemon-jvm.properties`). ModDevGradle and Loom
download Minecraft, NeoForge and Fabric on the first build.

```
./gradlew build                          # both loader jars + unit tests
./gradlew :neoforge:runClient            # development clients (also :fabric:runClient)
./gradlew :neoforge:runClient2           # second NeoForge client "Dev2"
./gradlew :neoforge:runServer            # dedicated servers (also :fabric:runServer)
./gradlew :neoforge:runGameTestServer    # GameTests, exits with a non-zero code on failure (also :fabric:)
./gradlew :neoforge:runClientSmokeTest   # scripted client run with screenshots (also :fabric:)
```

On Windows use `gradlew.bat`. The jars are written to `neoforge/build/libs/remnants_ballistics-neoforge-26.2-<version>.jar` and `fabric/build/libs/remnants_ballistics-fabric-26.2-<version>.jar`.
