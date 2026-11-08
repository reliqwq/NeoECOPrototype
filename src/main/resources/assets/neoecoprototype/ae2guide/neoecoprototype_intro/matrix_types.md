---
navigation:
  title: Supported storage matrices
  icon: neoecoprototype:simplify_item_storage_cell_1m
  position: 11
  parent: neoecoprototype_intro/index.md
---

# Supported storage matrices

One page comparing every matrix this addon registers for the [L1 storage system](storage_system.md): **pick the family by what you need to store, then compare capacity, type limit and the mod it requires.** Each family has its own page for the details.

## First: what are you storing

| Resource to store | Matrix family | Requires |
|-------------------|---------------|----------|
| Items | Standard item matrix | — |
| Fluids | Standard fluid matrix | — |
| Mekanism chemicals | Chemical matrix | Mekanism + Applied Mekanistics |
| One resource, in bulk | Small bulk matrix | MEGA Cells (chemical small bulk also needs Applied Mekanistics) |
| Forge Energy (FE) | FE storage matrix | AppliedFlux |
| Ars Nouveau source | Source matrix | Ars Énergistique (which requires Ars Nouveau) |
| Mekanism spent nuclear waste | Spent nuclear waste matrix | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| A mix of AE resources | Universal / complex universal / quantum universal | AE2 Omni Cells |
| One resource, unlimited room | Infinite storage matrix | — |
| Nothing stored - it produces | Singularity cell, sky stone dust cell, source cell, growing cell | Source cell needs Ars Énergistique; growing cells need KubeJS |

## Capacity, type limit and idle draw

| Family | Tiers | Type limit | Idle draw |
|--------|-------|------------|-----------|
| Standard item / fluid | 1K, 16K, 64K, 1M, 4M | 256 | see [L1 storage system](storage_system.md) |
| Chemical | 1K, 16K, 64K, 1M, 4M | 25 | same |
| Small bulk | unbounded bytes | 3 marked, 10 with the expansion card | same |
| FE | 1K, 1M, 4M (bytes; 1 byte = 1 Mi FE) | 1 | see [FE storage matrices](flux_storage.md) |
| Source | 1K, 16K, 64K, 1M, 4M | 256 | same |
| Spent nuclear waste | 1K, 1M | 1 | 8 AE/t |
| Universal | 1K, 1M | 512 | 8 AE/t |
| Complex universal | 1K, 1M | 1024 | 8 AE/t |
| Quantum universal | 1K, 1M | unlimited types | 8 AE/t |
| Infinite storage matrix | unbounded | 16 | 8 AE/t |
| Singularity cell | unbounded | 1 | 1 AE/t |
| Sky stone dust cell | unbounded | 1 | 1 AE/t |
| Source cell | unbounded | 1 | 1 AE/t |
| Growing cell (KubeJS) | unbounded | 1 | 1 AE/t |

- The 1K / 16K / 64K figures count **bytes**, not resources: how much of one thing fits is bytes divided by the bytes-per-type of that tier. Those three rungs sit **25% above their name** - 1280, 20480 and 81920 bytes - so AE2's own 1K cell (1024 bytes) holds less than ours under the same label. The 1M and 4M rungs are level with AE2, apart from the quantum 1M at 1572864.
- The mixed families (universal, complex universal, quantum universal) accept every AE resource type registered in the game - items, fluids, energy and chemicals can share one matrix. The tiers are the same across the three; only the type limit column differs.
- A small bulk matrix reports unbounded bytes, but what really limits it is its marking slots: it holds only the types you mark, and each marked type still needs its chain forms available. See [small bulk matrices](small_bulk_matrices.md).
- The singularity cell, the sky stone dust cell, the source cell and a growing cell **take nothing**: anything pushed at them is refused, and their stock is derived from the world clock. See [the L1 singularity cell](singularity_cell.md).

## What fits in our L1 drive

The drive compares tiers and **refuses anything ranked above itself**. Our L1 sits at tier 0, so eco's L4/L6/L9 compat matrices are refused here - this tier is priced below quantum on purpose and is not meant to host late-game matrices. Whether eco's own drives accept ours is its rule to decide, and we have no reading that guarantees it.

## Adding your own with KubeJS

A pack can register new tiers and materials, and can build a **growing cell** (`.grows(perBatch, ticksPerBatch)`: it hands out the bound item, fluid or chemical - from any mod - once per interval). See [KubeJS support](kubejs.md).
