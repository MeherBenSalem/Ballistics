# Asset pipeline

Every asset in the mod is generated from source in [`tools/assets`](../tools/assets): models, textures, icons, animations, sounds, GUIs, particles, translations, recipes, loot and tags. Nothing is hand-edited in `1.21.1/src/main/resources`, and nothing is copied from Minecraft or other mods.

```
python3 tools/assets/generate.py            # everything (about 30 s)
python3 tools/assets/generate.py --no-sound # skip audio synthesis
python3 tools/assets/check_assets.py --manifest docs/ASSET_MANIFEST.md
python3 tools/docs_gen.py                   # CONFIG/WEAPONS/ATTACHMENTS/PROGRESSION docs
```

Requirements: Python 3.10+, `numpy`, `Pillow`, and `ffmpeg` built with `libvorbis`. Generation is deterministic.

## Sources

| File | Produces |
| --- | --- |
| `voxel.py` | Voxel model description; per-face texture painting at 2 texels per pixel (machined edges, stippling, ribs, M-LOK and vent slots, wood grain, brass, glass, glow, see-through lenses, roll-marks); skin recolouring; Java/Blockbench model JSON export |
| `kit.py` | Reusable weapon components: octagonal tubes and crowns, picatinny and bottom rails, serrations, screws, trigger guards, triggers, raked pistol grips, selectors, flip-up and notch sights, curved magazines, butt pads, sling loops |
| `guns.py` | The 16 weapons, one function each, plus their presentation data (`gun_visuals`: grip pivot, hip and ADS placement, sight point, muzzle, ejection port, support-hand anchor, attachment mounts, bone pivots, sprint pose, shell type, flash size) |
| `attachments.py` | The 23 attachment models, built around the mount point (8, 8, 8) |
| `items.py`, `blocks.py` | Ammunition, materials and kits; the Gunsmith Workbench and Ammunition Press block models |
| `anims.py` | One Bedrock-format animation set per weapon, built from its family's choreography and its own geometry |
| `sfx.py` | Procedural audio: layered gunshots (crack, body, thump, mechanism, reverb tail), suppressed and distant variants, energy weapons, every mechanical handling sound, impacts, UI and blocks. Encoded as mono Ogg Vorbis |
| `art.py` | Container GUIs, HUD sprites (scope, reflex dot, holographic reticle), muzzle and energy flash sheets, bullet hole, particles, void orb, mod-list logo |
| `data_gen.py` | Recipes (crafting, smelting, blasting, ammunition press), loot tables and global loot modifiers, tags, damage types, advancements |
| `lang.py` | `en_us.json`, including config-screen keys parsed from the config sources |
| `raster.py` | A small numpy rasteriser used for icons, viewmodel previews and marketing renders |
| `preview_viewmodel.py`, `render_hero.py` | Offline first-person (hip and ADS) previews and three-quarter hero renders used to tune poses and review designs |
| `marketing_renders.py` | Studio, build and first-person renders for the marketing kit (see [marketing/README.md](../marketing/README.md)) |
| `check_assets.py` | Cross-checks the generated set against the Java registries and writes the [asset manifest](ASSET_MANIFEST.md) |

## Model conventions

- Java model pixels: barrel toward −Z (north), +Y up, weapon centred on x = 8, within the −16..32 element range. Rotations are single-axis multiples of 22.5°, as Java item models require.
- Each weapon is split into six part models (`models/gun/<weapon>/{body,action,magazine,stock,sights,muzzle}.json`). Parts a weapon lacks are shipped empty so registration is uniform.
- `tools/assets/blockbench/<weapon>.json` holds the whole weapon as one model with its parts as outliner groups, for editing in Blockbench. Open it as a Java Block/Item model. Edits should be ported back into `guns.py`, which stays the source of truth.

## Adding a weapon

1. Add a `WeaponDefinition` to `Weapons.java`: stats, ammunition, modes, slots, sound profile, rarity, tier.
2. Add a function to `guns.py` with the model and visuals. Use `render_hero.py` and `preview_viewmodel.py` to review the design and tune the hip and ADS poses.
3. Add `TUNING` (and, if it is charged with a handle, `CHARGE_HANDLE`) entries in `anims.py`.
4. Add a crafting recipe in `data_gen.py`, a display name in `lang.py`, and loot entries if wanted.
5. Run `generate.py`, `check_assets.py` and `docs_gen.py`.
