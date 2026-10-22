# Multiplayer and latency testing

Remnants: Ballistics is server-authoritative. Clients predict their own shots and the server corrects them, so it has to be tested under real latency, jitter and packet loss, not only on a local integrated server.

## Automated checks

| Command (in `1.21.1/`) | What it covers |
| --- | --- |
| `./gradlew test` | Minecraft-free core: spread determinism, ballistic drop, recoil, bloom, falloff, hit zones, reload maths, stat modifiers, the fire-rate limiter (steady fire, spam, jitter, idle-credit caps) |
| `./gradlew runGameTestServer` | GameTests on a headless server, driving the real server pipeline with a fake player. See below. |
| `python3 tools/assets/check_assets.py` (repository root) | Every registered item, block, sound, particle, model part, animation clip, translation and data reference resolves |

The GameTests (`gametest/BallisticsGameTests.java`) cover:

- an aimed rifle shot damages a target and uses one round;
- bullets shatter glass;
- the fire-rate limiter rejects a same-tick double shot and replayed sequence numbers;
- the draw delay blocks firing;
- empty weapons do not fire;
- the Voidcaster spawns its orb;
- magazine reloads draw from the reserve;
- single-load reloads insert round by round;
- the ammunition press recipe matches;
- every weapon has a recipe;
- loot tables and damage types are loaded.

## Local two-player setup

```
cd 1.21.1
./gradlew runServer      # first run: accept run/eula.txt, set online-mode=false in run/server.properties
./gradlew runClient      # player "Dev"
./gradlew runClient2     # player "Dev2" (separate game directory run/client2)
```

Connect both clients to `localhost`. Give items with `/give Dev remnants_ballistics:arx_16`, `/give Dev remnants_ballistics:ammo_556 128`, and so on, or use the creative tabs.

## Simulating a bad network

Shape the loopback interface that carries the client–server traffic:

- **Linux:** `sudo tc qdisc add dev lo root netem delay 100ms 20ms loss 1%`. Remove it with `sudo tc qdisc del dev lo root`. On loopback this applies in both directions, so the round trip is about 200 ms.
- **Windows:** [clumsy](https://jagt.github.io/clumsy/) with the filter `tcp.DstPort == 25565 or tcp.SrcPort == 25565`, then enable Lag, Drop or Throttle.
- **macOS:** Network Link Conditioner (Xcode Additional Tools), or `dnctl` and `pfctl` pipes on port 25565.

Test at least at 0, 100, 200 and 300 ms round trip, with 20–40 ms jitter and 1–2% loss.

## Checklist

| Scenario | Expected |
| --- | --- |
| Hold full-auto on a stationary target at 200 ms | The fire rate feels unchanged; hit markers arrive about one round trip later; the magazine count never jumps backwards except after a rejected shot |
| Strafe and shoot another player at 200 ms | Hits register where the target appeared on the shooter's screen, up to `maxLagCompensationMs` |
| Spam-click a bolt-action or the railgun | No more shots than the weapon's rate allows, even after idling; extra clicks are dropped |
| Reload, then switch weapons halfway | The reload cancels on both clients; no ammunition is taken; the magazine is unchanged |
| Reload a single-load weapon and fire mid-reload | The reload stops after the current round; the shot fires |
| Disconnect mid-reload and reconnect | No ammunition is lost or duplicated |
| Fit and remove attachments rapidly at the Gunsmith Workbench with 1–2% loss | No attachment is duplicated or lost; closing the screen keeps the weapon as shown |
| Take ammunition out of the press while the other player watches | One output per set of inputs; the inputs are consumed |
| Fire across 100+ blocks | The far player hears the far-field report with a delay; the near player hears the full report |
| Observe the other player aiming, reloading and firing in third person | Aim pose, magazine and action animations, muzzle flash, tracers and casings |
| Change `remnants_ballistics-server.toml` (for example `fireRate` of `arx_16`) and reconnect | Both the client prediction and the server use the new value |

## Diagnosing

- The server's view of a player's latency (`ShooterState.rttMs`) drives the lag-compensation rewind. Large rejection counts usually mean `fireRateToleranceTicks` is too low for the network, or a client is modified.
- `maxAimDeviation` clamps the aim a client reports. If legitimate fast flicks are being clamped at high latency, raise it slightly.
