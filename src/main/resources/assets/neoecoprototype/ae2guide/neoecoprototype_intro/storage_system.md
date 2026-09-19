---
navigation:
  title: L1 Storage System
  icon: neoecoprototype:simplify_storage_controller
  position: 10
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_storage_controller
  - neoecoprototype:simplify_drive
  - neoecoprototype:simplify_energy_cell
  - neoecoprototype:simplify_item_storage_cell_1k
  - neoecoprototype:simplify_item_storage_cell_16k
  - neoecoprototype:simplify_item_storage_cell_64k
  - neoecoprototype:simplify_item_storage_cell_1m
  - neoecoprototype:simplify_item_storage_cell_4m
  - neoecoprototype:simplify_fluid_storage_cell_1k
  - neoecoprototype:simplify_fluid_storage_cell_16k
  - neoecoprototype:simplify_fluid_storage_cell_64k
  - neoecoprototype:simplify_fluid_storage_cell_1m
  - neoecoprototype:simplify_fluid_storage_cell_4m
  - neoecoprototype:simplify_chemical_storage_cell_1k
  - neoecoprototype:simplify_chemical_storage_cell_16k
  - neoecoprototype:simplify_chemical_storage_cell_1m
  - neoecoprototype:simplify_chemical_storage_cell_4m
---

# L1 Storage System

The L1 storage system is a compact multiblock: a <ItemLink id="neoecoprototype:simplify_storage_controller" /> plus any number of <ItemLink id="neoecoprototype:simplify_drive" /> drives and <ItemLink id="neoecoprototype:simplify_energy_cell" /> energy cells. Drives hold your storage matrices and mount them into the ME network.

## Choosing a matrix

| What you store | Matrix family | Requires |
|----------------|---------------|----------|
| Items (1K - 4M) | Item storage matrix | - |
| Fluids (1K - 4M) | Fluid storage matrix | - |
| Mekanism chemicals (1K - 4M) | Chemical storage matrix | Mekanism + Applied Mekanistics |
| Mixed AE resources, unlimited types | Universal / Quantum matrix | AE2 Omni Cells |
| A fixed item, endlessly | Infinite concrete matrix / KubeJS infinite matrix | - |

All matrices are partitionable: craft them in the AE2 cell workbench to whitelist what they accept.

## Small bulk matrices

For bulk storage of a few resources, see [Small Bulk Matrices](small_bulk_matrices.md) - three types with effectively unlimited capacity per type.

## External cells

L1 drives mount native L1 cells and small bulk cells by default. Higher-tier eco cells (including eco's own MegaCells long-bulk cell) are insertable but do not mount unless a server admin whitelists them in the server config (`additional_storage_cells`).
