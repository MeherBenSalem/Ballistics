# Controls

| Action | Default | Notes |
| --- | --- | --- |
| Fire | Left mouse (vanilla **Attack**) | Hold for automatic fire. Bursts fire their full burst per click. |
| Aim down sights | Right mouse (vanilla **Use**) | Hold to aim; set `controls.toggleAim = true` to click on and off. |
| Reload | `R` | Tactical reload keeps a round chambered; empty reloads take longer. Single-load weapons load one round per step, and firing interrupts them. |
| Switch fire mode | `V` | Cycles the weapon's modes (for example Auto → Semi, or Auto → Burst → Semi). |
| Inspect | `I` | Plays the inspect animation (not while aiming or reloading). |
| Charge the railgun | Hold fire | The Eclipse Railgun charges while the trigger is held and fires when charged. Releasing early cancels the charge. |

Reload, fire mode and inspect live under **Options → Controls → Key Binds → Remnants: Ballistics** and can be rebound. Fire and aim follow the vanilla Attack and Use bindings.

While holding a weapon, the Attack and Use keys do not break blocks or use items. Switch to another hotbar slot to interact with the world.

## Sensitivity

- `controls.aimSensitivity` (default `0.85`) scales mouse sensitivity while aiming.
- With `controls.scaleSensitivityWithZoom` (default on), sensitivity is also divided by the optic's magnification, so 4× and 8× scopes stay controllable.

## Accessibility

| Setting | Effect |
| --- | --- |
| `visuals.flashBrightness` | Dims muzzle flashes (photosensitivity) |
| `visuals.cameraShake` | Scales or removes firing camera shake (recoil itself is unchanged) |
| `visuals.weaponBob`, `visuals.weaponSway` | Reduce viewmodel motion |
| `hud.highContrast` | Outlines HUD text and the crosshair |
| `hud.scale`, `hud.opacity`, `hud.anchor` | Resize, fade or move the ammo panel |
| `audio.gunshotVolume`, `audio.mechanicalVolume`, `audio.hitmarkerVolume` | Per-category volume |

See [CONFIG.md](CONFIG.md) for every option.
