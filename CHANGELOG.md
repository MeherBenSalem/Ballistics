# Changelog

Full release notes live in [`patchnotes/`](patchnotes/README.md).

## 0.6.0 — Minecraft 1.20.1

See [patchnotes/0.6.0.md](patchnotes/0.6.0.md).

- Forge and Fabric builds for **Minecraft 1.20.1** from a new `1.20.1/` workspace (Java 17; Forge 47.1+, or Fabric Loader 0.16.14+ with Fabric API and Forge Config API Port 8.0.3+), with the same weapons, attachments, stations, crafting, loot, advancements, animations, HUD and multiplayer behaviour as the other versions. Weapon state is stored in the item's NBT there, and the configs are edited as TOML files (1.20.1 has no in-game config screen).
- Assets and data are ported to the 1.20.1 formats by `tools/assets/port_resources.py`; the common ingot tags are `c:steel_ingots` / `c:brass_ingots` (which include `forge:ingots/steel` / `forge:ingots/brass`).
- Unit tests, GameTests, the client smoke test and jar verification cover 1.20.1 on both loaders in CI; the release tooling ships a Forge jar for 1.20.1 and NeoForge jars for 1.21.1 and 26.x.
- Ammunition press recipes no longer log an "Unknown recipe category" warning per recipe in the client log (1.20.1, 1.21.1).

## 0.5.0 — Minecraft 26.2 & 26.3

NeoForge and Fabric builds for Minecraft 26.2 and 26.3 alongside 1.21.1, from new `26.2/` and `26.3/` workspaces; all three versions are tested (unit tests, GameTests, client smoke tests) on both loaders in CI. See [patchnotes/0.5.0.md](patchnotes/0.5.0.md).

## 0.4.0 — Fabric & Real Gunfire

Fabric support for Minecraft 1.21.1 (with Fabric API and Forge Config API Port) alongside NeoForge, from a shared multi-loader workspace; real-recording style gunshots and handling sounds for every firearm. See [patchnotes/0.4.0.md](patchnotes/0.4.0.md).

## 0.3.0 — First Shots

First public pre-release: 16 weapons, 23 attachments, the Gunsmith Workbench and Ammunition Press, server-authoritative ballistics with client prediction and lag compensation, synthesized audio, loot and advancements. See [patchnotes/0.3.0.md](patchnotes/0.3.0.md).
