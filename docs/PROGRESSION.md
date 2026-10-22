# Crafting and progression

Generated from the recipe and loot definitions in `tools/assets/data_gen.py` by `python3 tools/docs_gen.py`. Recipes show up in the recipe book and in JEI/EMI. Every recipe group can be switched off in `remnants_ballistics-common.toml` (see [CONFIG.md](CONFIG.md)).

## Materials

| Item | How to get it |
|---|---|
| Steel Ingot | Smelt an Iron Ingot (blast furnace: twice as fast). Also accepts other mods' `#c:ingots/steel` |
| Brass Ingot | 3 Copper Ingots + 1 Gold Nugget → 3 (shapeless). Also accepts `#c:ingots/brass` |
| Brass Casing | 1 Brass Ingot → 8 |
| Shotgun Hull | 2 Paper + Red Dye + Brass Ingot → 6 |
| Polymer Sheet | 2 Dried Kelp + Coal or Charcoal + Slime Ball → 4 |
| Gun Parts | 4 Steel Ingots around an Iron Nugget → 2 |
| Precision Parts | Gun Parts, 2 Gold Ingots, 2 Redstone and 4 Nether Quartz → 2 |
| Remnant Alloy | Netherite Scrap + 2 Steel + 2 Brass + Precision Parts → 2. Also found in the Nether, ancient cities and end cities |
| Void Core | An Echo Shard surrounded by 4 Ender Pearls and 4 Amethyst Shards. Also found in ancient cities and end cities |
| Weapon Repair Kit | Gun Parts + Steel Ingot + Iron Ingot + Leather → 2. Restores durability at the Gunsmith Workbench (Gun Parts also repair weapons in an anvil) |
| Advanced / Prototype Weapon Blueprint | Found in structures (see below). Copy one with Paper and Lapis Lazuli (advanced) or Paper, an Amethyst Shard and an Ender Pearl (prototype) |

## Stations

| Station | Recipe | Use |
|---|---|---|
| **Gunsmith Workbench** | 3 Steel Ingots, 4 Iron Ingots and a Crafting Table | Fit and remove attachments with a live stat preview; repair weapons with repair kits |
| **Ammunition Press** | 2 Steel Ingots, a Piston, 2 Iron Ingots, Redstone and 3 Cobblestone | Presses ammunition from casings, propellant and projectiles |

## Weapons by tier

Blueprints are returned after crafting, so one blueprint unlocks a tier for good.

| Tier | Weapon | Ingredients |
|---:|---|---|
| 1 | R-9 Sidearm | 2× Steel Ingot, Gun Parts, 2× Polymer Sheet |
| 2 | ARX-16 | 3× Steel Ingot, 2× Gun Parts, 3× Polymer Sheet |
| 2 | Raven AK | 5× Steel Ingot, 2× any Planks, Gun Parts |
| 2 | Thunderpump | 4× Steel Ingot, 2× any Planks, Gun Parts |
| 2 | Viper-9 | 3× Steel Ingot, 3× Polymer Sheet, Gun Parts |
| 2 | Warden .357 | 3× Steel Ingot, Gun Parts, 2× any Planks |
| 3 | Breacher-12 | 4× Steel Ingot, Advanced Weapon Blueprint, Precision Parts, 3× Polymer Sheet |
| 3 | Ironfang .50 | 3× Steel Ingot, Advanced Weapon Blueprint, Precision Parts, 2× any Planks |
| 3 | Mantis BR | 2× Steel Ingot, 4× Polymer Sheet, Advanced Weapon Blueprint, Precision Parts |
| 3 | Spectre PDW | 3× Polymer Sheet, 2× Steel Ingot, Advanced Weapon Blueprint, Precision Parts |
| 3 | Titan-56 | 4× Steel Ingot, Advanced Weapon Blueprint, Precision Parts, 2× Polymer Sheet, Gun Parts |
| 4 | Bulwark-762 | 4× Steel Ingot, Prototype Weapon Blueprint, Precision Parts, 2× any Planks, Gun Parts |
| 4 | Ghostline M24 | 4× Steel Ingot, Prototype Weapon Blueprint, Precision Parts, 2× Polymer Sheet |
| 4 | Longshot .50 | 2× Remnant Alloy, 3× Steel Ingot, Prototype Weapon Blueprint, Precision Parts, 2× Polymer Sheet |
| 4 | Voidcaster | 3× Remnant Alloy, Void Core, Prototype Weapon Blueprint, Precision Parts, Polymer Sheet |
| 5 | Eclipse Railgun | 4× Remnant Alloy, 2× Void Core, Prototype Weapon Blueprint, Precision Parts, Polymer Sheet |

## Ammunition press

Put the ingredients in any of the three input slots; the arrow lights up when they match.

| Output | Ingredients |
|---|---|
| 16× 9mm Rounds | 4× Brass Casing, 1× Gunpowder, 1× Copper Ingot |
| 12× Magnum Rounds | 4× Brass Casing, 2× Gunpowder, 1× Iron Ingot |
| 16× 5.7mm Rounds | 4× Brass Casing, 1× Gunpowder, 4× Iron Nugget |
| 16× 5.56mm Rounds | 4× Brass Casing, 2× Gunpowder, 1× Steel Ingot |
| 16× 7.62mm Rounds | 4× Brass Casing, 1× Blaze Powder, 1× Copper Ingot |
| 8× .308 Rounds | 4× Brass Casing, 1× Blaze Powder, 1× Steel Ingot |
| 4× .50 BMG Rounds | 4× Brass Casing, 2× Blaze Powder, 2× Iron Ingot |
| 8× 12 Gauge Shells | 4× Shotgun Hull, 1× Gunpowder, 4× Iron Nugget |
| 4× Energy Cell | 4× Redstone, 2× Amethyst Shard, 1× Gold Ingot |

## Attachments

Shapeless crafting-table recipes.

| Attachment | Ingredients |
|---|---|
| Holo Reflex Sight | 2× Steel Ingot, Glass Pane, Redstone |
| Tripoint Holographic Sight | 2× Steel Ingot, 2× Glass Pane, Redstone, Precision Parts |
| Ranger 4x Scope | 3× Steel Ingot, 2× Glass Pane, Precision Parts |
| Longeye 8x Scope | 3× Steel Ingot, 2× Glass Pane, Precision Parts, Diamond |
| Suppressor | 3× Steel Ingot, any Wool, Polymer Sheet |
| Compensator | 2× Steel Ingot, Gun Parts |
| Muzzle Brake | 3× Steel Ingot, Gun Parts |
| Flash Hider | Steel Ingot, Gun Parts, Iron Nugget |
| Shotgun Choke | Steel Ingot, 2× Iron Nugget |
| Vertical Grip | 2× Polymer Sheet, Gun Parts |
| Angled Grip | 3× Polymer Sheet, Gun Parts |
| Stubby Grip | Polymer Sheet, Gun Parts |
| Extended Magazine | 2× Steel Ingot, 2× Polymer Sheet, Gun Parts |
| Drum Magazine | 3× Steel Ingot, 3× Polymer Sheet, Precision Parts |
| Quick-Swap Magazine | 2× Steel Ingot, 2× Polymer Sheet, String |
| Tactical Stock | 3× Polymer Sheet, 2× Steel Ingot, Gun Parts |
| Light Stock | 2× Steel Ingot, 2× Iron Ingot, Leather |
| Heavy Precision Stock | 3× Steel Ingot, 2× Polymer Sheet, Precision Parts, Leather |
| Desert Camo Kit | Polymer Sheet, Sand, Brown Dye, Yellow Dye |
| Arctic Camo Kit | Polymer Sheet, Snowball, White Dye, Light Gray Dye |
| Crimson Finish Kit | Polymer Sheet, 2× Red Dye, Black Dye |
| Gilded Finish Kit | Polymer Sheet, 2× Gold Ingot, Black Dye |
| Void Finish Kit | Polymer Sheet, Ender Pearl, 2× Purple Dye |

## Where to find gear

Loot is added to vanilla tables by global loot modifiers. Each source rolls its table with the listed chance (scaled by `lootChanceMultiplier`). Found weapons are partly worn.

| Source | Chance | Highlights |
|---|---:|---|
| pillager outpost, woodland mansion | 75% | 9mm Rounds, 5.56mm Rounds, 12 Gauge Shells, Gun Parts, Brass Casing, Advanced Weapon Blueprint, R-9 Sidearm, Viper-9, Thunderpump, Raven AK, Holo Reflex Sight, Compensator, Vertical Grip |
| village/village weaponsmith, village/village toolsmith | 60% | 9mm Rounds, Steel Ingot, Gun Parts, R-9 Sidearm, Weapon Repair Kit |
| simple dungeon, abandoned mineshaft, trial chambers/corridor | 50% | 9mm Rounds, 7.62mm Rounds, Brass Casing, Gun Parts, Warden .357, Raven AK, Magnum Rounds, Flash Hider, Stubby Grip |
| stronghold corridor, stronghold crossing, stronghold library | 60% | Advanced Weapon Blueprint, Precision Parts, Ranger 4x Scope, Tripoint Holographic Sight, Spectre PDW, Mantis BR, 5.7mm Rounds, Extended Magazine, Tactical Stock |
| desert pyramid | 60% | Desert Camo Kit, .308 Rounds, 5.56mm Rounds, Brass Ingot, Ghostline M24, Longeye 8x Scope, Muzzle Brake |
| jungle temple | 60% | 9mm Rounds, Quick-Swap Magazine, Angled Grip, Suppressor, Viper-9 |
| igloo chest | 35% | Arctic Camo Kit |
| shipwreck supply, buried treasure | 50% | 12 Gauge Shells, Thunderpump, Shotgun Choke, Shotgun Hull |
| nether bridge | 50% | Crimson Finish Kit, Magnum Rounds, Ironfang .50, Breacher-12, Remnant Alloy, Advanced Weapon Blueprint |
| bastion treasure, bastion other, bastion bridge | 55% | Gilded Finish Kit, Remnant Alloy, Prototype Weapon Blueprint, Ironfang .50, Titan-56, Bulwark-762, 7.62mm Rounds, Drum Magazine, Heavy Precision Stock |
| ancient city, ancient city ice box | 50% | Void Core, Prototype Weapon Blueprint, Voidcaster, Energy Cell, Longshot .50, .50 BMG Rounds |
| end city treasure | 50% | Void Finish Kit, Eclipse Railgun, Voidcaster, Energy Cell, Void Core, Remnant Alloy, Prototype Weapon Blueprint |
| pillager, vindicator, piglin brute | 25% | 9mm Rounds, 5.56mm Rounds, Brass Casing, Gun Parts |

Armed mobs only drop loot when killed by a player.
