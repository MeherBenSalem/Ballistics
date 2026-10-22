# Remnants: Ballistics 1.21.1

Independent NeoForge workspace for Minecraft 1.21.1.

| Path | Contents |
| --- | --- |
| `src/main/java` | Mod code (`com.nightbeam.remnants.ballistics`) |
| `src/main/resources` | Generated assets and data (do not hand-edit; see `../tools/assets`) |
| `src/test/java` | JUnit tests for the Minecraft-free core |

Java 21 is required. ModDevGradle downloads Minecraft and NeoForge on the first build.

```
./gradlew build               # jar + unit tests
./gradlew runClient           # development client
./gradlew runClient2          # second client "Dev2"
./gradlew runServer           # dedicated server
./gradlew runGameTestServer   # GameTests, exits with a non-zero code on failure
```

On Windows use `gradlew.bat`. The jar is written to `build/libs/remnants_ballistics-neoforge-1.21.1-<version>.jar`.
