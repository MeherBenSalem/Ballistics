# Feature list

## Weapons

- 16 weapons in 7 classes, with full stats in [WEAPONS.md](WEAPONS.md):
  - **Pistols:** R-9 Sidearm, Ironfang .50 and the Warden .357 revolver.
  - **SMGs:** Viper-9 and Spectre PDW.
  - **Assault rifles:** ARX-16, Raven AK and the Mantis BR bullpup.
  - **Shotguns:** Breacher-12 (magazine-fed) and Thunderpump (pump).
  - **Snipers:** Ghostline M24 (bolt-action) and Longshot .50 (anti-materiel).
  - **LMGs:** Titan-56 and Bulwark-762.
  - **Experimental:** the Voidcaster and the Eclipse Railgun.
- Every weapon has its own:
  - damage, fire rate, magazine, tactical and empty reload times;
  - damage falloff and range, hip, aimed and moving spread, bloom, vertical and horizontal recoil, kick, sway;
  - aim time, zoom, move speed, muzzle velocity, bullet drop, penetration and headshot multiplier;
  - fire modes, sound profile, rarity, durability, crafting tier and attachment slots.
- **Fire modes:** semi-automatic, burst (with per-weapon burst size and pacing) and full-auto, switched with a key.
- **Actions:**
  - magazine reloads with separate tactical and empty timings, where a round stays chambered on a tactical reload;
  - single-round loading for the revolver, pump shotgun and bolt-action sniper, which firing interrupts;
  - pump and bolt cycling between shots;
  - a charged railgun shot;
  - an energy projectile weapon.
- **Durability:** weapons wear as they fire and stop working at zero until repaired, with a Weapon Repair Kit at the Gunsmith Workbench or with Gun Parts in an anvil.

## Shooting

- **Server-validated hitscan** with bullet drop and muzzle velocity, damage falloff, and head, body and leg hit zones (configurable multipliers).
- **Penetration** through soft blocks (leaves, panes, thin wood) and through entities, with damage loss.
- **Ricochets** off hard surfaces at shallow angles (configurable chance and angle).
- **Glass and panes shatter** when shot (configurable; respects `mobGriefing` for non-players).
- **Aim down sights:** the sight picture lines up exactly with the crosshair, and reflex and holographic sights draw their reticle. 4× and 8× scopes switch to a full scope overlay with mil-dots, extra sway and zoom-scaled sensitivity.
- **Accuracy:**
  - hip fire is less accurate than aiming;
  - moving, jumping and sprinting widen spread, while crouching tightens it;
  - bloom grows with sustained fire and recovers;
  - firing stops a sprint unless the server allows sprint-firing.
- **Recoil:** vertical climb with horizontal drift and recovery, scaled by aiming, attachments and config, plus camera shake.
- **Visual feedback:** tracers, animated muzzle flashes, gun smoke, ejected brass and shotgun shells (with clink sounds), impact particles and sounds per material, bullet holes that fade, hit markers with distinct headshot and kill feedback, and a whiz when rounds pass close by.
- **Sound:** a distinct report per weapon family, a suppressed report, and a far-field report for distant gunfire that arrives with a speed-of-sound delay.

## Customization

- **Gunsmith Workbench** with six slots: optic, muzzle, underbarrel, magazine, stock and skin.
- 18 functional attachments and 5 cosmetic finishes, each with trade-offs. See [ATTACHMENTS.md](ATTACHMENTS.md).
- Compatibility rules per weapon class: for example, no muzzle brake on pistols and no scopes on SMGs. Unsupported slots are greyed out.
- A live stat panel shows the rotating 3D weapon with its attachments, every stat bar and the change each attachment makes.
- Attachments are stored on the weapon item itself, so they travel with it, survive restarts, show in tooltips and render on the weapon in first person, third person, on the ground and in item frames.
- Skins recolour the whole weapon (desert and arctic camo, crimson, gilded, void).

## Visuals and animation

- Voxel weapon models of 48 to 118 elements, with hand-painted-style textures at 2 texels per pixel (edge highlights, stippling, M-LOK slots, wood grain, engraving).
- Per-weapon keyframed animations: idle, equip, inspect, fire, last-round fire, tactical and empty reloads, single-round start/insert/end, pump and bolt cycling, railgun charge. Hands reach the weapon's own magazine, charging handle, pump or bolt.
- Separate first- and third-person presentation: a full viewmodel with arms in first person; animated actions and magazines plus a two-handed aiming pose in third person.
- Crisp 32×32 inventory icons for every weapon, attachment, ammunition type and material.

## UI

- Minimal ammo panel with the weapon name in its rarity colour, magazine and reserve count, fire mode, reload and charge progress, and durability.
- Dynamic crosshair that opens with the current spread.
- Configurable HUD corner, offset, scale and opacity; a high-contrast mode; a toggle for the reload label.
- Inspect animation on a key.
- Rich tooltips: class, ammunition, fire modes, rarity, tier, attachments and full stats with Shift.

## Crafting and progression

- Steel and brass ingots, casings and hulls, polymer, gun parts, precision parts, remnant alloy and void cores.
- **Ammunition Press** for all nine ammunition types.
- Five weapon tiers. Tiers 3 to 5 need blueprints found in structures; blueprints are never consumed.
- Loot in 25 vanilla chest and mob tables, from village weaponsmiths to end cities, with themed skins per structure.
- Twelve advancements, from forging steel to a 100-block kill.
- Creative tabs for weapons, ammunition and materials, and attachments.

See [PROGRESSION.md](PROGRESSION.md).

## Multiplayer

- Server-authoritative ammunition, fire rate, spread, hits, damage, reloads and block changes.
- Client prediction for instant feel, with server corrections.
- Lag compensation for player targets, bounded by config.
- Anti-cheat: sequence numbers against replays, a fire-rate limiter with jitter tolerance, aim-deviation clamping, a spread floor, and server-side reload timing.
- Duplication-safe gunsmithing and ammunition press.
- Remote players' shots, reloads and aiming are animated and heard by everyone nearby.
- Dedicated-server safe: client code is never loaded on a server.

## Configuration

Client, common and server config files with an in-game editor, including per-weapon overrides. See [CONFIG.md](CONFIG.md).
