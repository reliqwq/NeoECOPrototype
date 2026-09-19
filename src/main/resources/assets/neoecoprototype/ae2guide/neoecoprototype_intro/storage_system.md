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

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## Build the structure

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_storage_controller" />
  <ItemIcon id="neoecoprototype:simplify_drive" />
  <ItemIcon id="neoecoprototype:simplify_energy_cell" />
  <ItemIcon id="neoecoprototype:simplify_storage_interface" />
  <ItemIcon id="neoecoprototype:simplify_storage_network_interface" />
  <ItemIcon id="neoecoprototype:simplify_storage_vent" />
  <ItemIcon id="neoecoprototype:simplify_storage_casing" />
</ItemGrid>

The storage controller sets the tier and the interaction face; storage matrix drives hold your storage matrices; energy cells buffer power; the storage interface supplies cell content outward; the storage network interface connects the system to the ME network; the storage vent handles heat; storage casings fill the frame.

## Build order

1. Place the storage controller, front facing outward (this is the interaction face).
2. Place the storage network interface to join the ME network.
3. Line the drives along the controller's side.
4. Fit the storage interface and vents where the shell calls for them.
5. Fill the remaining frame with storage casings.

Placing block by block is error-prone - use the **auto-build panel** in the host UI: preview the structure first, then fill it in one click.

Proofread note: the placement order is written from the block set, not verified block by block - the auto-build preview is authoritative.


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
