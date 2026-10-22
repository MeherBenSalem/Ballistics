# Weapons

Generated from `Weapons.java` by `python3 tools/docs_gen.py`. Damage is in half-hearts per bullet (per pellet for shotguns) before hit-zone multipliers; server owners can override every value in `remnants_ballistics-server.toml` (see [CONFIG.md](CONFIG.md)).

| Weapon | Class | Ammo | Damage | RPM | Magazine | Reload (tactical / empty) | Fire modes | Rarity | Tier |
|---|---|---|---:|---:|---:|---|---|---|---:|
| **R-9 Sidearm** | Pistol | 9mm | 5 | 420 | 15 | 1.40s / 1.80s | Semi | Common | 1 |
| **Ironfang .50** | Pistol | Magnum | 11 | 160 | 7 | 1.70s / 2.10s | Semi | Rare | 3 |
| **Warden .357** | Pistol | Magnum | 9 | 200 | 6 | 0.50s per round + 0.90s | Semi | Uncommon | 2 |
| **Viper-9** | SMG | 9mm | 3.8 | 900 | 30 | 1.60s / 2s | Auto, Semi | Uncommon | 2 |
| **Spectre PDW** | SMG | 5.7mm | 4.2 | 800 | 40 | 1.80s / 2.20s | Auto, Burst, Semi | Rare | 3 |
| **ARX-16** | Assault rifle | 5.56mm | 5.5 | 720 | 30 | 2s / 2.50s | Auto, Semi | Uncommon | 2 |
| **Raven AK** | Assault rifle | 7.62mm | 6.8 | 600 | 30 | 2.20s / 2.70s | Auto, Semi | Uncommon | 2 |
| **Mantis BR** | Assault rifle | 5.56mm | 6.0 | 900 | 24 | 2s / 2.50s | Burst, Semi | Rare | 3 |
| **Breacher-12** | Shotgun | 12 Gauge | 3.2 × 9 | 240 | 8 | 2.20s / 2.60s | Semi | Rare | 3 |
| **Thunderpump** | Shotgun | 12 Gauge | 3.6 × 8 | 70 | 6 | 0.45s per round + 0.80s | Semi | Uncommon | 2 |
| **Ghostline M24** | Sniper rifle | .308 | 18 | 48 | 5 | 0.60s per round + 1s | Semi | Epic | 4 |
| **Longshot .50** | Sniper rifle | .50 BMG | 28 | 36 | 5 | 2.80s / 3.50s | Semi | Legendary | 4 |
| **Titan-56** | LMG | 5.56mm | 5.2 | 700 | 100 | 4.50s / 5s | Auto | Epic | 3 |
| **Bulwark-762** | LMG | 7.62mm | 6.6 | 560 | 75 | 5s / 5.50s | Auto | Epic | 4 |
| **Voidcaster** | Experimental | Energy Cell | 8 | 300 | 24 | 2s / 2s | Semi, Auto | Epic | 4 |
| **Eclipse Railgun** | Experimental | Energy Cell | 30 | 40 | 6 | 2.50s / 2.50s | Semi | Legendary | 5 |

## Details

### R-9 Sidearm (`remnants_ballistics:r9_sidearm`)

- **Range:** full damage to 16 blocks, falling to 55% at 48 blocks; maximum range 96 blocks. Muzzle velocity 360 blocks/s.
- **Durability:** 1200 shots. **Attachment slots:** optic, muzzle, magazine.

### Ironfang .50 (`remnants_ballistics:ironfang_50`)

- **Range:** full damage to 20 blocks, falling to 60% at 56 blocks; maximum range 112 blocks. Muzzle velocity 420 blocks/s.
- **Durability:** 900 shots. **Attachment slots:** optic, muzzle, magazine.

### Warden .357 (`remnants_ballistics:warden_357`)

- **Range:** full damage to 22 blocks, falling to 60% at 60 blocks; maximum range 112 blocks. Muzzle velocity 400 blocks/s.
- **Durability:** 1400 shots. **Attachment slots:** optic.
- Loads one round at a time; firing interrupts the reload.

### Viper-9 (`remnants_ballistics:viper_9`)

- **Range:** full damage to 12 blocks, falling to 45% at 36 blocks; maximum range 72 blocks. Muzzle velocity 330 blocks/s.
- **Durability:** 2400 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.

### Spectre PDW (`remnants_ballistics:spectre_pdw`)

- **Range:** full damage to 16 blocks, falling to 50% at 44 blocks; maximum range 80 blocks. Muzzle velocity 450 blocks/s.
- **Durability:** 2600 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.

### ARX-16 (`remnants_ballistics:arx_16`)

- **Range:** full damage to 28 blocks, falling to 60% at 80 blocks; maximum range 160 blocks. Muzzle velocity 600 blocks/s.
- **Durability:** 3000 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.

### Raven AK (`remnants_ballistics:raven_ak`)

- **Range:** full damage to 26 blocks, falling to 60% at 76 blocks; maximum range 160 blocks. Muzzle velocity 560 blocks/s.
- **Durability:** 3600 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.

### Mantis BR (`remnants_ballistics:mantis_br`)

- **Range:** full damage to 34 blocks, falling to 65% at 96 blocks; maximum range 192 blocks. Muzzle velocity 650 blocks/s.
- **Durability:** 2800 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.

### Breacher-12 (`remnants_ballistics:breacher_12`)

- **Range:** full damage to 6 blocks, falling to 25% at 20 blocks; maximum range 40 blocks. Muzzle velocity 300 blocks/s.
- **Durability:** 1800 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.

### Thunderpump (`remnants_ballistics:thunderpump`)

- **Range:** full damage to 8 blocks, falling to 25% at 22 blocks; maximum range 40 blocks. Muzzle velocity 300 blocks/s.
- **Durability:** 2000 shots. **Attachment slots:** optic, muzzle, underbarrel, stock.
- Pump action: the pump is worked after every shot; Loads one round at a time; firing interrupts the reload.

### Ghostline M24 (`remnants_ballistics:ghostline_m24`)

- **Range:** full damage to 80 blocks, falling to 80% at 200 blocks; maximum range 320 blocks. Muzzle velocity 800 blocks/s.
- **Durability:** 1200 shots. **Attachment slots:** optic, muzzle, underbarrel, stock.
- Bolt action: the bolt is cycled after every shot; Loads one round at a time; firing interrupts the reload; Ships with the Longeye 8x Scope fitted.

### Longshot .50 (`remnants_ballistics:longshot_50`)

- **Range:** full damage to 120 blocks, falling to 85% at 300 blocks; maximum range 400 blocks. Muzzle velocity 850 blocks/s.
- **Durability:** 900 shots. **Attachment slots:** optic, muzzle, underbarrel, magazine, stock.
- Bolt action: the bolt is cycled after every shot; Ships with the Longeye 8x Scope fitted.

### Titan-56 (`remnants_ballistics:titan_56`)

- **Range:** full damage to 28 blocks, falling to 60% at 80 blocks; maximum range 160 blocks. Muzzle velocity 600 blocks/s.
- **Durability:** 5000 shots. **Attachment slots:** optic, muzzle, underbarrel, stock.

### Bulwark-762 (`remnants_ballistics:bulwark_762`)

- **Range:** full damage to 30 blocks, falling to 60% at 90 blocks; maximum range 176 blocks. Muzzle velocity 580 blocks/s.
- **Durability:** 5000 shots. **Attachment slots:** optic, muzzle, underbarrel, stock.

### Voidcaster (`remnants_ballistics:voidcaster`)

- **Range:** full damage to 64 blocks, falling to 100% at 96 blocks; maximum range 96 blocks. Muzzle velocity 30 blocks/s.
- **Durability:** 1500 shots. **Attachment slots:** none (cosmetic skins only).
- Energy weapon; Fires a slow void orb projectile that bursts on impact instead of a hitscan bullet.

### Eclipse Railgun (`remnants_ballistics:eclipse_railgun`)

- **Range:** full damage to 300 blocks, falling to 100% at 400 blocks; maximum range 256 blocks. Muzzle velocity 2000 blocks/s.
- **Durability:** 800 shots. **Attachment slots:** none (cosmetic skins only).
- Energy weapon; Must be charged for 1s before each shot; Integrated scope (no optic slot).
