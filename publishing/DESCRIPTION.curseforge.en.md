## Overview

| Item | Details |
| --- | --- |
| Requirements | Minecraft Java Edition 1.20.1 · Forge 47.4.10 or a compatible later 47.x release · Java 17 |
| Installation | Required on both the client and the server |
| World generation | A single vanilla loot chest buried below the terrain |
| Optional integration | JEI + Just Enough Resources (JER), installed on the client |
| License | MIT for the original project material; third-party notices are preserved |

**Ubiquitous Chests** adds randomly generated underground treasure chests across your world. Each structure contains just **one chest**—no building, room, spawner, or surrounding blocks.

All biomes and dimensions are eligible by default, provided the terrain supports generation. This includes modded biomes. You decide how often chests appear, how deep they are buried, where they cannot generate, and what they contain.

*Sometimes the treasure really is right under your feet.*

## Finding a chest

Explore newly generated terrain, or use the vanilla structure-locating command with cheats or operator permissions:

```mcfunction
/locate structure ubiquitous_chests:underground_chest
```

The returned X/Z coordinates point to the chest's column. Vanilla `/locate` displays `~` for Y, so you still need to explore below ground. Clear the block above the chest to open it.

Existing worlds are supported, but new chests only appear in **newly generated chunks**. The world must have structure generation enabled.

## Configuration

The mod creates `config/ubiquitous_chests-common.toml` on first launch, with English and Chinese comments. Stop the game or server before editing, then restart. Multiplayer worlds use the server's configuration.

| Setting | Default | What it controls |
| --- | --- | --- |
| `enabled` | `true` | Natural generation |
| `chancePerChunk` | `0.02` | A 2% generation attempt chance per 16×16 chunk, with at most one chest |
| `minDepth` / `maxDepth` | `8` / `48` | Depth below the base terrain, including both endpoints |
| `blockedBiomes` | `[]` | Biome IDs or `#tags` in which generation is forbidden |
| `blockedDimensions` | `[]` | Dimension IDs in which generation is forbidden |
| `minRolls` / `maxRolls` | `3` / `7` | Number of weighted loot rolls per chest |
| `items` | Basic resources and iron equipment | Item IDs, weights, and quantity ranges |

Depth is measured from the generator's base terrain: the seabed in oceans and the upper terrain in the Nether. Trees and later decorations are not included. Chunks without a suitable solid position in the configured range are skipped, so actual generation can be lower than the attempt probability. Later cave carving or other mods may expose a chest in a cave.

### Example: exclude oceans, the Nether, and the End

```toml
[generation]
enabled = true
chancePerChunk = 0.05
minDepth = 10
maxDepth = 35
blockedBiomes = ["#minecraft:is_ocean", "minecraft:deep_dark"]
blockedDimensions = ["minecraft:the_nether", "minecraft:the_end"]
```

Both the surface biome and the chest's underground biome are checked. Empty blacklists allow all biomes or dimensions; custom dimensions still need a generator that supports structures.

## Choosing the loot

Each loot entry uses this format:

```text
item_id|weight|minimum_count|maximum_count
```

```toml
[loot]
minRolls = 3
maxRolls = 7
items = [
  "minecraft:iron_ingot|30|2|8",
  "minecraft:gold_ingot|18|1|5",
  "minecraft:ender_pearl|8|1|3",
  "minecraft:obsidian|12|2|6",
  "minecraft:iron_pickaxe|5|1|1"
]
```

The defaults also include coal, bread, an iron sword, and all four iron armor pieces. Items from installed mods are supported.

| Rule | Details |
| --- | --- |
| Weight | 1–100000; higher weights make an entry more likely on each roll |
| Quantity | 1–64 per roll; non-stackable items follow vanilla splitting rules |
| Roll count | 0–27; reversed minimum/maximum roll or depth ranges are normalized |
| Invalid entries | Skipped with a log warning |
| Empty loot | An empty item list or zero rolls produces an empty chest |
| Capacity | The chest has 27 slots; excessive loot may not all fit |

The same item can be selected more than once. Entry-level NBT customization is not supported.

## JEI / JER integration

Install both **Just Enough Items (JEI)** and **Just Enough Resources (JER)** on the client to view **Ubiquitous Chests** in JER's dungeon chest loot category. You can also look up a loot item's recipes and check its chest sources.

The page shows items, quantity ranges, roll counts, and weighted probability **per roll**. This is not the probability of finding at least one copy in a whole chest.

Loot data is synchronized from the server on login and after `/reload`, so players do not need to copy the server's configuration. Neither JEI nor JER is required for chest generation, and neither is required on the dedicated server.

Tested with JEI **15.20.0.106** and JER **1.4.0.247** for Forge 1.20.1.

## Frequently asked questions

**Does this add a custom chest block?**

No. The structure places one vanilla chest with the mod's loot table.

**Will changing the configuration affect existing chests?**

Generation changes affect new chunks and do not remove existing structures. Unopened chests use the current loot table; opened chests retain their contents. Previously generated chests may still be found after changing blacklists.

**Can I use it in a modpack or modify it?**

Yes. The original project material is MIT licensed. Preserve the license notice when redistributing it, and observe the separate licenses of Minecraft, Forge and other dependencies.

**Where can I find the source or report a problem?**

[Source code](https://github.com/zz123456789/UbiquitousChests) · [Issue tracker](https://github.com/zz123456789/UbiquitousChests/issues)
