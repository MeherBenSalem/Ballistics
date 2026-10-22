# Remnants: Ballistics 1.20.1

Independent workspace for Minecraft 1.20.1 with a Forge and a Fabric jar, in the MultiLoader layout.

| Path | Contents |
| --- | --- |
| `common/src/main/java` | Loader-neutral mod code (`com.nightbeam.remnants.ballistics`), vanilla Minecraft only |
| `common/src/main/resources` | Assets and data ported from `../1.21.1` by `../tools/assets/port_resources.py` (do not hand-edit), plus the access transformer and widener |
| `common/src/test/java` | JUnit tests for the Minecraft-free core |
| `common/src/smoketest` | Scripted client run shared by both loaders (never packaged) |
| `forge/` | Forge entry points, events, networking, the GameTest delegates and one client mixin (mouse sensitivity) |
| `fabric/` | Fabric entry points, networking and client mixins (hooks Fabric API has no event for) |
| `buildSrc/` | `multiloader-common` and `multiloader-loader` conventions: each loader compiles the common sources into its own jar |

The common code is the 1.21.1 code ported to 1.20.1 APIs: weapon state lives in item NBT instead of data
components, payloads are written to plain buffers, recipes and loot conditions use JSON serializers, and the
configs use Forge's `ForgeConfigSpec`.

Java 17 is required. The first build downloads Minecraft, Forge (ModDevGradle's legacy plugin) and Fabric (Loom).

```
./gradlew :common:test               # unit tests
./gradlew build                      # both jars
./gradlew :forge:runClient           # development client (also :forge:runClient2, a second client "Dev2")
./gradlew :forge:runServer           # dedicated server
./gradlew :forge:runGameTestServer   # GameTests, exits with a non-zero code on failure
./gradlew :fabric:runClient          # Fabric client (also :fabric:runServer)
./gradlew :fabric:runGametest        # Fabric GameTests
./gradlew :forge:runClientSmokeTest  # scripted client run: every weapon and screen, screenshots in run/smoketest
```

On Windows use `gradlew.bat`. The jars are written to `forge/build/libs/remnants_ballistics-forge-1.20.1-<version>.jar`
and `fabric/build/libs/remnants_ballistics-fabric-1.20.1-<version>.jar`. The Fabric jar needs Fabric API and
Forge Config API Port.
