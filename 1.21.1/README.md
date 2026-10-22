# Remnants: Ballistics 1.21.1

Independent workspace for Minecraft 1.21.1 with a NeoForge and a Fabric jar, in the MultiLoader layout.

| Path | Contents |
| --- | --- |
| `common/src/main/java` | Loader-neutral mod code (`com.nightbeam.remnants.ballistics`), vanilla Minecraft only |
| `common/src/main/resources` | Generated assets and data (do not hand-edit; see `../tools/assets`), plus the access transformer and widener |
| `common/src/test/java` | JUnit tests for the Minecraft-free core |
| `common/src/smoketest` | Scripted client run shared by both loaders (never packaged) |
| `neoforge/` | NeoForge entry points, events, networking and the GameTest delegates |
| `fabric/` | Fabric entry points, networking and client mixins (hooks Fabric API has no event for) |
| `buildSrc/` | `multiloader-common` and `multiloader-loader` conventions: each loader compiles the common sources into its own jar |

Java 21 is required. The first build downloads Minecraft, NeoForge (ModDevGradle) and Fabric (Loom).

```
./gradlew :common:test                 # unit tests
./gradlew build                        # both jars
./gradlew :neoforge:runClient          # development client (also :neoforge:runClient2, a second client "Dev2")
./gradlew :neoforge:runServer          # dedicated server
./gradlew :neoforge:runGameTestServer  # GameTests, exits with a non-zero code on failure
./gradlew :fabric:runClient            # Fabric client (also :fabric:runServer)
./gradlew :fabric:runGametest          # Fabric GameTests
./gradlew :neoforge:runClientSmokeTest # scripted client run: every weapon and screen, screenshots in run/smoketest
```

On Windows use `gradlew.bat`. The jars are written to `neoforge/build/libs/remnants_ballistics-neoforge-1.21.1-<version>.jar`
and `fabric/build/libs/remnants_ballistics-fabric-1.21.1-<version>.jar`. The Fabric jar needs Fabric API and
Forge Config API Port.
