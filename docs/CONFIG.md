# Configuration

Generated from the config specs by `python3 tools/docs_gen.py`. Edit the files in game with **Mods → Remnants: Ballistics → Config**, or by hand while the game is closed.

| File | Scope | Location |
|---|---|---|
| `remnants_ballistics-client.toml` | Each player's own visuals, HUD, controls and audio | `config/` |
| `remnants_ballistics-common.toml` | Recipe toggles and loot injection (applies on `/reload` or world load) | `config/` |
| `remnants_ballistics-server.toml` | Balance, hit zones, block interaction, anti-cheat, per-weapon overrides. Synced to clients so their prediction matches the server | `<world>/serverconfig/` (copy to `defaultconfigs/` for new worlds). NeoForge on 26.3: `config/`, overridden per world by `<world>/syncedconfig/` |

## Client

### `[hud]`: Weapon HUD

| Option | Default | Range | Description |
|---|---|---|---|
| `enabled` | `true` | true / false | - |
| `anchor` | `BOTTOM_RIGHT` | BOTTOM_RIGHT / BOTTOM_LEFT / TOP_RIGHT / TOP_LEFT | Screen corner the ammo panel is anchored to |
| `offsetX` | `8` | -2000 to 2000 | Horizontal offset from the anchor in GUI pixels |
| `offsetY` | `8` | -2000 to 2000 | Vertical offset from the anchor in GUI pixels |
| `scale` | `1.0` | 0.5 to 3.0 | - |
| `opacity` | `0.85` | 0.1 to 1.0 | - |
| `dynamicCrosshair` | `true` | true / false | Replace the vanilla crosshair with a spread-aware crosshair while holding a weapon |
| `crosshairColor` | `0xFFFFFF` | 0x000000 to 0xFFFFFF | Crosshair colour as 0xRRGGBB |
| `hitmarkers` | `true` | true / false | - |
| `highContrast` | `false` | true / false | Draw HUD text and crosshair with dark outlines for readability |
| `reloadText` | `true` | true / false | Show a 'RELOADING' label in addition to the progress bar |

### `[controls]`: Controls

| Option | Default | Range | Description |
|---|---|---|---|
| `toggleAim` | `false` | true / false | Aim is toggled by a click instead of held |
| `aimSensitivity` | `0.85` | 0.05 to 2.0 | Mouse sensitivity multiplier while aiming |
| `scaleSensitivityWithZoom` | `true` | true / false | Additionally divide sensitivity by optic magnification |

### `[visuals]`: Visual effects. Reduce these for accessibility or performance.

| Option | Default | Range | Description |
|---|---|---|---|
| `weaponBob` | `1.0` | 0.0 to 2.0 | - |
| `weaponSway` | `1.0` | 0.0 to 2.0 | - |
| `cameraShake` | `1.0` | 0.0 to 2.0 | Camera shake from firing (0 disables; does not change recoil itself) |
| `muzzleFlash` | `true` | true / false | - |
| `flashBrightness` | `1.0` | 0.0 to 1.0 | Muzzle flash brightness (lower for photosensitivity) |
| `tracers` | `true` | true / false | - |
| `shellEjection` | `true` | true / false | - |
| `maxBulletHoles` | `256` | 0 to 4096 | - |
| `particleIntensity` | `1.0` | 0.0 to 2.0 | Scales impact particle counts (0 disables) |
| `viewmodelOffsetX` | `0.0` | -0.5 to 0.5 | Viewmodel horizontal offset |
| `viewmodelOffsetY` | `0.0` | -0.5 to 0.5 | Viewmodel vertical offset |
| `viewmodelOffsetZ` | `0.0` | -0.5 to 0.5 | Viewmodel depth offset |

### `[audio]`: Audio

| Option | Default | Range | Description |
|---|---|---|---|
| `gunshotVolume` | `1.0` | 0.0 to 2.0 | - |
| `mechanicalVolume` | `1.0` | 0.0 to 2.0 | Reloads, bolts, pumps, casings |
| `hitmarkerVolume` | `0.7` | 0.0 to 2.0 | - |

## Common

### `[crafting]`: Recipe toggles. Changes apply on the next datapack reload (/reload) or world load.

| Option | Default | Range | Description |
|---|---|---|---|
| `weaponCrafting` | `true` | true / false | Enable crafting recipes for weapons |
| `ammoCrafting` | `true` | true / false | Enable ammunition press recipes |
| `attachmentCrafting` | `true` | true / false | Enable crafting recipes for attachments |

### `[loot]`: Loot injection

| Option | Default | Range | Description |
|---|---|---|---|
| `structureLoot` | `true` | true / false | Add weapons, ammunition, attachments and blueprints to structure chests |
| `mobDrops` | `true` | true / false | Armed mobs (pillagers, vindicators, piglin brutes) may drop ammunition and parts |
| `lootChanceMultiplier` | `1.0` | 0.0 to 10.0 | Multiplier applied to every injected loot chance |

## Server

### `[combat]`: Damage and hit zones

| Option | Default | Range | Description |
|---|---|---|---|
| `damageMultiplier` | `1.0` | 0.0 to 100.0 | Global multiplier for all firearm damage |
| `pvpDamageMultiplier` | `1.0` | 0.0 to 100.0 | Extra multiplier applied when a player shoots another player |
| `mobDamageMultiplier` | `1.0` | 0.0 to 100.0 | Extra multiplier applied when a player shoots a non-player entity |
| `headshotMultiplier` | `2.0` | 0.0 to 20.0 | Damage multiplier for headshots (multiplied with the weapon's own headshot stat) |
| `bodyMultiplier` | `1.0` | 0.0 to 20.0 | Damage multiplier for torso hits |
| `limbMultiplier` | `0.75` | 0.0 to 20.0 | Damage multiplier for leg hits |
| `headshotsOnAllMobs` | `false` | true / false | If false, only entities in the remnants_ballistics:humanoid_hit_zones tag use head/body/limb zones; others use a narrow eye-level head band |
| `ignoreInvulnerabilityFrames` | `true` | true / false | Bullets ignore and do not grant post-hit invulnerability, so automatic fire deals full damage |
| `knockback` | `0.15` | 0.0 to 2.0 | Knockback strength per 10 damage dealt (vanilla sword hit is ~0.4) |

### `[handling]`: Accuracy, recoil and handling

| Option | Default | Range | Description |
|---|---|---|---|
| `spreadMultiplier` | `1.0` | 0.0 to 10.0 | Multiplier for every spread value (lower = more accurate) |
| `recoilMultiplier` | `1.0` | 0.0 to 10.0 | Multiplier for camera recoil |
| `reloadSpeedMultiplier` | `1.0` | 0.1 to 10.0 | Reload speed multiplier (2.0 = reloads take half as long) |
| `allowSprintFiring` | `false` | true / false | Allow firing while sprinting (firing otherwise stops the sprint) |

### `[ammunition]`: Ammunition and durability

| Option | Default | Range | Description |
|---|---|---|---|
| `requireAmmo` | `true` | true / false | Survival players must carry ammunition to reload |
| `creativeInfiniteAmmo` | `true` | true / false | Creative mode players reload without consuming ammunition |
| `useDurability` | `true` | true / false | Weapons lose durability when fired and stop working at 0 until repaired |

### `[blocks]`: Block interaction

| Option | Default | Range | Description |
|---|---|---|---|
| `breakGlass` | `true` | true / false | Bullets shatter blocks in the remnants_ballistics:bullet_breakable tag (glass, panes). Respects the mobGriefing rule for non-player shooters. |
| `penetration` | `true` | true / false | Bullets may pass through soft blocks and entities, losing damage |
| `ricochet` | `true` | true / false | Bullets may ricochet off hard surfaces at shallow angles |
| `ricochetChance` | `0.35` | 0.0 to 1.0 | Chance that a qualifying shallow impact ricochets |
| `ricochetMaxAngle` | `22.0` | 0.0 to 89.0 | Maximum impact angle in degrees (0 = grazing) that can ricochet |
| `energyBlockDamage` | `false` | true / false | Voidcaster orb bursts can destroy blocks |

### `[network]`: Networking and anti-cheat

| Option | Default | Range | Description |
|---|---|---|---|
| `fireRateToleranceTicks` | `2.0` | 0.0 to 10.0 | How many ticks early a shot packet may arrive (absorbs network jitter) |
| `maxAimDeviation` | `30.0` | 1.0 to 180.0 | Maximum degrees between the client-reported aim and the server's view of the player; larger deviations are clamped |
| `lagCompensation` | `true` | true / false | Rewind other players' hitboxes by the shooter's latency when tracing shots |
| `maxLagCompensationMs` | `250` | 0 to 1000 | Upper bound for lag compensation rewind |
| `fxBroadcastRange` | `128` | 16 to 512 | Players within this many blocks receive tracer/impact effects for a shot |

### `[weapons.<weapon>]`: per-weapon overrides

One section per weapon (`r9_sidearm`, `arx_16`, …). Values are absolute and default to the weapon's base stats listed in [WEAPONS.md](WEAPONS.md).

| Option | Description |
|---|---|
| `enabled` | `false` stops the weapon from firing (it can still be held and traded) |
| `damage` | Damage per bullet or pellet, in half-hearts |
| `fireRate` | Rounds per minute |
| `magazine` | Magazine capacity |
| `reloadTicks` | Tactical reload ticks (single-load weapons: ticks to insert one round) |
| `emptyReloadTicks` | Empty reload ticks (single-load weapons: ticks to open and close the action) |
| `recoilMultiplier` | Recoil multiplier for this weapon |
| `spreadMultiplier` | Spread multiplier for this weapon |
