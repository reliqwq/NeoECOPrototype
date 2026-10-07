---
navigation:
  title: L1 Singularity Cell
  icon: neoecoprototype:simplify_singularity_cell
  position: 22
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_singularity_cell
---

# L1 Singularity Cell

A storage cell that **grows singularities on its own and takes nothing in**. Put it in a drive, leave it alone, and its stock rises with world time; pull from it through the network like from any other cell.

## How fast

Out of the box it makes one batch of **120 singularities every 600 ticks** - 30 seconds, so about 4 per second. Both numbers are in the server config under `singularity_cell`:

| Key | Default | What it is clamped to |
| --- | --- | --- |
| `ticks_per_batch` | 600 | at least 60 ticks (3 seconds) |
| `amount_per_batch` | 120 | at most 262,144 |

There is **no bank ceiling**: the stock counts up in a long and is only bounded by that. What the numbers are worth in practice:

| Wants | Cost in singularities | Time at the default rate |
| --- | --- | --- |
| our three L1 interfaces | 6 | covered by the first batch |
| one eco MEGA 4G matrix, or one infinite storage component | 128 | 32 seconds |
| eco's infinite storage unlock (64 components) | 8,192 | about 34 minutes |

At the maximum settings (262,144 per 3 seconds) a single cell outruns anything the game can spend, so treat those as pack-maker territory.

## The stock is calculated, not stored

Nothing accumulates in the background: the cell keeps only the tick it started from and how much has been drawn, and works out the rest whenever the network asks. Three consequences worth knowing:

- **Changing the config re-reads the past.** Raising `amount_per_batch` instantly makes the bank bigger; lowering it can empty what you had saved up.
- **Time only passes when the server ticks.** A paused or stopped server produces nothing, and a world whose clock is behind the tick the cell was stamped at simply reads as empty until it catches up.
- **Being carried starts it.** The cell takes its start tick as soon as it is on a player - main rows, hotbar and offhand all count - so it does not have to be installed first. A cell lying on the ground or left in a chest waits until it is picked up or put in a drive.

The stock line reads on a multiplayer client too: the world tick is sent to clients once a second, so a number in hand can be at most a second behind. What stays missing there is the content preview image, which cannot reach a world - the figure itself still reads. Terminals read the server's value and are always right.

## Where it mounts

It reports the L1 tier, so an [L1 Storage System](storage_system.md) host takes it without a whitelist entry, and eco's higher hosts take it too. It holds exactly one item type, cannot be partitioned in the cell workbench (nothing to filter), and refuses to be stored inside another storage cell.

## Building one

Shapeless: eco's **infinite storage component** (the same part that unlocks eco's own infinite storage), one green crystal matrix, and one singularity as the seed. A separate disassembly recipe gives all three back.
