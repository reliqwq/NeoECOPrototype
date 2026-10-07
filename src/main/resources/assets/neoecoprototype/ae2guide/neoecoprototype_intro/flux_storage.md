---
navigation:
  title: Flux Storage Matrices
  icon: neoecoprototype:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_fe_cell_housing
  - neoecoprototype:simplify_fe_storage_cell_1m
  - neoecoprototype:simplify_fe_storage_cell_4m
---

# Flux Storage Matrices

These matrices hold **energy** as network content: feed FE into the grid and it banks it, pull FE back out wherever you have an interface. They are the L1 rung under eco's own FE matrices.

This family only exists when **AppliedFlux** (mod id `appflux`) is installed - the mod brings GuideME and Glodium with it. Without AppliedFlux none of these three items are registered: no creative entry, no recipe, nothing in the world.

## Capacity is counted in bytes, not in FE

Like every other AE2 storage cell, a flux cell is sized in **bytes**, and AppliedFlux decides how much FE a byte is worth: its `flux_cell.amount` config, **1,048,576 FE per byte** out of the box.

| Matrix | Bytes | FE at the default rate |
| --- | --- | --- |
| L1 1M flux matrix | 1,048,576 | about 1.1 trillion FE |
| L1 4M flux matrix | 4,194,304 | about 4.4 trillion FE |

Those are the same byte counts as AppliedFlux's own `fe_1m_cell` and `fe_4m_cell`, so our matrices sit level with them rather than under them. Raise `flux_cell.amount` in AppliedFlux's config and every flux cell in the game - ours included - gets roomier for the same materials.

## Building one

Craft the housing from a green crystal matrix, redstone and AppliedFlux's hardened insulating resin, then combine it with our storage component of the matching size (1M or 4M). A disassembly recipe gives both halves back.

## Where it mounts

Both matrices report the L1 tier, so an [L1 Storage System](storage_system.md) host takes them without any whitelist entry, and eco's higher-tier hosts take them too. Their hover text shows the stored FE through eco's own flux-cell line, and the byte figure beside it.
