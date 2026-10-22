# Marketing assets: 0.3 "First Shots"

Listing copy and artwork for CurseForge and Modrinth. Nothing here ships in the mod jar.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full project description (Markdown; also works on Modrinth) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short project summary (219 characters) |
| [`../patchnotes/0.3.0.md`](../patchnotes/0.3.0.md) | File changelog for 0.3.0 |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/Ballistics/main/marketing/images/...`, so they work once this folder is on `main`. To host the images on CurseForge instead, upload them to the project gallery and replace each URL.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600 | Description header (ARX-16 custom build and Ghostline M24) |
| `images/icon.png` | 512×512 | Project avatar |
| `images/card-arsenal.png` | 1600×900 | All sixteen weapons |
| `images/card-handling.png` | 1600×900 | First-person aiming and hip fire |
| `images/card-ballistics.png` | 1600×900 | Bullet drop, penetration, ricochet and hit-zone defaults |
| `images/card-gunsmith.png` | 1600×900 | Gunsmith Workbench layout with an ARX-16 build and its stat changes |
| `images/card-attachments.png` | 1600×900 | Eight attachments with their trade-offs |
| `images/card-finishes.png` | 1600×900 | The five weapon finishes on the ARX-16 |
| `images/card-prototypes.png` | 1600×900 | Voidcaster and Eclipse Railgun |
| `images/card-progression.png` | 1600×900 | Material, blueprint and ammunition chain (item icons) |
| `images/card-multiplayer.png` | 1600×900 | Client prediction and server validation |
| `images/card-config.png` | 1600×900 | Excerpt of the default server config |
| `images/header-*.png`, `images/divider.png` | 1400 wide, transparent | Section headings for the description |
| `images/renders/*.jpg` | 1920×1080 | Gallery: first-person views |

### How the images were made

There are no in-game screenshots yet, so every image is a **render of the mod's own assets**, and the description says so in its footer:

- **3D renders:** the weapon, attachment and first-person images come from `tools/assets/marketing_renders.py`. It drives the same voxel models, painted textures and skins that ship in the jar through the project's software rasteriser (`tools/assets/raster.py`).
- **First-person views:** these use the in-game viewmodel placement (hip pose, ADS sight alignment, arm reach; see `preview_viewmodel.py`) on a small voxel shooting range built for the renders. The arms wear a neutral sleeve and glove; in game they use the player's skin.
- **Builds:** the custom builds (`build-*.png`) place real attachment models on each weapon's mount points and hide the parts they replace, exactly as `GunRenderer` does.
- **Workbench:** the Gunsmith Workbench on `card-gunsmith.png` is laid out from the mod's real GUI texture and item icons, using the slot positions from `GunsmithMenu`. The stat panel beside it is an illustration: its numbers are the ARX-16's real values with that build, but the in-game panel is styled differently.

Replace the renders with real captures once a development client is available (`cd 1.21.1 && ./gradlew runClient`). Keep the file names and rerun the script below.

## Regenerating the artwork

```bash
python3 tools/assets/marketing_renders.py                               # renders into marketing/source/renders and images/renders
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs             # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon # selected assets
```

- `source/renders/` holds the transparent studio renders (`hero-*`, `side-*`, `build-*`, `skin-*`, `att-*`) and downscaled first-person views used by the cards.
- `source/fonts/` holds Barlow Condensed and Inter (WOFF2, from Fontsource) with their SIL Open Font License 1.1 texts. The script uses only these files and the mod's generated textures, so it depends on nothing outside this repository.

## Wording

- Use the mod's own names. Do not use real manufacturers' names or other games' names, logos or art in listing images or titles.
- Every number in the copy and on the cards is a default from the code or the generated docs (`docs/WEAPONS.md`, `docs/ATTACHMENTS.md`, `docs/CONFIG.md`, `docs/PROGRESSION.md`). Update the copy when those defaults change.
