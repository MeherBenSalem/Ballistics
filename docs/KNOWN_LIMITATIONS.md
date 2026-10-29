# Known limitations

## Release status

0.5.0 is a **pre-release**. The Minecraft-free core is covered by unit tests, the generated assets are cross-checked against the code, and the GameTests exercise the server pipeline. The first public builds should still be treated as beta, and server owners should keep backups.

## Platform

- Minecraft **1.21.1**, **26.2** and **26.3** on **NeoForge** and **Fabric** are supported. There is no Forge build and no port to other Minecraft versions.
- On Fabric the weapon HUD and crosshair are drawn from a mixin on the vanilla crosshair, and NeoForge's projectile-impact event has no Fabric counterpart, so other mods cannot cancel Voidcaster orb impacts there.
- It is a mod, not a server plugin. It does not run on Paper, Spigot or Folia, and it must be installed on both the server and every client.

## Ballistics

- Bullets other than the Voidcaster orb are hitscan. The trajectory includes drop, but flight time is not simulated: the impact is instant.
- Lag compensation rewinds **players** only. Mobs are hit where the server has them, which can lag slightly behind what a high-latency client sees.
- Penetration and ricochet are simplified: a fixed damage loss per penetrated block or entity, and a random ricochet chance under a maximum angle.
- Explosive or incendiary ammunition and launchers are not included.

## Presentation

- The first-person viewmodel uses Minecraft's fixed hand field of view (70°) and does not change with the FOV slider. It can be nudged with `viewmodelOffsetX/Y/Z`.
- Third-person animation is applied to the weapon (action, magazine, recoil) with a fixed two-handed aiming pose; the player's arms are not keyframed per weapon.
- Bullet holes are client-side decals with a configurable cap (`maxBulletHoles`). They fade out after 30 seconds and are not saved.
- Shader packs (Iris, Oculus) are untested. Muzzle flashes and the void orb use additive "eyes" rendering, which most shader packs support.
- Skins recolour a weapon's metal and furniture materials. Glass, brass, glowing parts and recesses keep their colours by design.

## Gameplay

- Mobs do not use firearms.
- Weapons cannot be enchanted, so Mending does not apply. Repair them with Weapon Repair Kits at the Gunsmith Workbench, or with Gun Parts in an anvil.
- The default keys `R` (reload), `V` (fire mode) and `I` (inspect) may clash with other mods' bindings. Rebind them under **Controls → Key Binds**.
- Some attachments trade one stat for another. The Gunsmith Workbench shows every change before you commit.
