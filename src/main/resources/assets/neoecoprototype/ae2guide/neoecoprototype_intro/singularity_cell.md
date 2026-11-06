---
navigation:
  title: L1 Singularity Cell
  icon: neoecoprototype:simplify_singularity_cell
  position: 22
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_singularity_cell
  - neoecoprototype:simplify_sky_stone_dust_cell
---

# L1 Singularity Cell

Once it sits in a drive it **produces 120 singularities every 30 seconds**. It needs no input and consumes nothing, and it is drawn from like any other storage cell - it is there for saves that want singularities before they build an <ItemLink id="ae2:condenser" />.

## How fast

One **batch** is one payout of 120: the stock - what this page and the config call the bank - jumps by that much each interval instead of trickling. The shipped interval is 600 ticks (30 s), which averages about 4 a second.

| Time in a drive | Ready |
|-----------------|-------|
| 30 s | 120 |
| 1 min | 240 |
| 5 min | 1,200 |
| 30 min | 7,200 |
| 1 hour | 14,400 |
| 24 hours | 345,600 |

| What you want it for | Needs | At the default rate |
|----------------------|-------|---------------------|
| This addon's three L1 interfaces | 6 | the first batch covers it (30 s) |
| one eco MEGA 4G matrix, or one infinite storage component | 128 | two batches, 60 s |
| eco's infinite storage unlock (64 components) | 8,192 | 69 batches, about 35 min |

## How far it can be tuned

Both keys live in the `singularity_cell` section of the server config and are read fresh each time:

| Key | Default | Range |
|-----|---------|-------|
| `ticks_per_batch` | 600 ticks (30 s) | 60 ticks (3 s) minimum |
| `amount_per_batch` | 120 | 1 - 9,223,372,036,854,775,807 (the top of a `long`); any value in between is accepted |

The stock figure itself is a `long`, and **9,223,372,036,854,775,807 is where it stops**: past that it saturates instead of wrapping, and since production is pinned at the same figure, whatever you draw afterwards stays drawn. The world clock running out saturates the same way rather than turning the stock negative.

## The contents are computed, not saved

Nothing accumulates inside the cell: the stack stores two values - the world tick it started at and how much has been taken - and every query is computed as (current tick - start tick) / ticks_per_batch x amount_per_batch - taken. Output therefore depends on the start tick, the current tick and the amount taken, and on nothing else. Four consequences follow. Raising `amount_per_batch` re-reads history and grows the stock at once, lowering it can erase what was stored. While the game is paused or the server is down world time does not advance, so the stock does not grow. The start tick is written the first time a drive or host queries the cell, so a cell in an inventory, a chest or as a dropped item has not started counting, while one that has started keeps growing when you take it out. Terminals read the server value; a client tooltip syncs once a second and can be up to a second behind.

## Limits

It holds one content type, so it cannot be partitioned in a cell workbench and cannot be stored inside another cell. It belongs to the L1 tier, so [the L1 storage system](storage_system.md) mounts it without a whitelist entry, and eco's higher tiers do too.

## Recipe

<RecipeFor id="neoecoprototype:simplify_singularity_cell" />

A disassembly recipe returns the four materials.

## The other member: sky stone dust cell

The same engine, the same tile and the same default pace (120 per 600 ticks) - it grows **sky stone dust** (`ae2:sky_dust`, AE2's own) instead of singularities. It has its own config section, `sky_stone_dust_cell`, with the same two keys and the same defaults: they are separate entries because the two resources sit far apart in cost, and tuning one cell must not move the other.

<RecipeFor id="neoecoprototype:simplify_sky_stone_dust_cell" />

Its bill differs from the singularity cell's only in the last ingredient: infinite matrix housing + 64M ECO storage component + green crystal lattice + one sky stone dust. A disassembly recipe returns the same four.
