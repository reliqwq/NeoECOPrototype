---
navigation:
  title: Neo ECO Prototype
  position: 200
---

# Neo ECO Prototype

Neo ECO Prototype brings the late-game capabilities of the Neo ECO AE Extension down to the **L1 tier**, so you can access them in the early game instead of after the endgame grind.
The three L1 systems (storage/computation/crafting) draw one eighth of the power of their eco L4 counterparts.

Start by building the [L1 Storage System](storage_system.md): one <ItemLink id="neoecoprototype:simplify_storage_controller" /> hosts your drives and grows with your matrices. When you are ready to automate processors, add the [L1 Processor Assembler](processor_assembler.md). For bulk storage of a few carefully chosen resources, add the [Small Bulk Matrices](small_bulk_matrices.md). Storage of a different kind comes in two more pages: [Flux Storage Matrices](flux_storage.md) keep FE inside the grid (they need AppliedFlux), and the [L1 Singularity Cell](singularity_cell.md) grows singularities by itself.

For the early game, power and bulk marking go through two interfaces of our own: the [L1 Powered Interface](powered_interface.md) keeps 18 markers and feeds the grid 200 AE/t, and the [Energized Superconductive Interface](superconductive_interface.md) is its upgrade - the same layout, but 8192 items (or 512,000 mB of fluid) per marker and 4000 AE/t. AE2 lets only one passive generator run per grid and always picks the strongest one, so these do not add up.

Pack makers can script matrices and recipes with [KubeJS](kubejs.md) - no Java required.

The Trinity unified storage/crafting system is still in early development and is not covered in this guide yet.
