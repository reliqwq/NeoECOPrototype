---
navigation:
  title: L1 Computation System
  icon: neoecoprototype:simplify_computation_system
  position: 50
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_computation_system
  - neoecoprototype:simplify_computation_casing
  - neoecoprototype:simplify_computation_parallel_core
  - neoecoprototype:simplify_computation_threading_core
  - neoecoprototype:simplify_computation_cooling_controller
  - neoecoprototype:simplify_computation_drive
  - neoecoprototype:simplify_computation_interface
  - neoecoprototype:simplify_computation_network_interface
---

# L1 Computation System

The L1 Computation System is the early-game version of eco's Computation System: a multiblock crafting CPU cluster that provides parallel crafting threads for the ME network.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## Components

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_computation_system" />
  <ItemIcon id="neoecoprototype:simplify_computation_casing" />
  <ItemIcon id="neoecoprototype:simplify_computation_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_threading_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_cooling_controller" />
  <ItemIcon id="neoecoprototype:simplify_computation_drive" />
  <ItemIcon id="neoecoprototype:simplify_computation_interface" />
  <ItemIcon id="neoecoprototype:simplify_computation_network_interface" />
  <ItemIcon id="neoecoprototype:energized_computation_core" />
  <ItemIcon id="neoecoprototype:energized_computation_threading_core" />
  <ItemIcon id="neoecoprototype:energized_computation_cell_4m" />
</ItemGrid>


The concept matches eco's Computation System: the base provides **1 crafting thread** (a quarter of eco's L4), extendable with parallel and threading cores under eco's rules. The L1 variant above is the minimal working structure.

## Energized members

Two extra parts lift an L1 subsystem above what eco's own members give it:

- **Energized Computation Core** (`energized_computation_core`) adds **1024 co-processors**. It fits a
  parallel core column or a shell casing cell. In the shell it stops drawing its own model once the
  structure forms, the way every eco shell member does: formed casings are invisible and no longer
  cull their neighbours, so a member that kept drawing there would be seen from both sides at once.
  The face is painted back by a renderer instead.
- **Energized Computation Threading Core** (`energized_computation_threading_core`) adds **16 crafting
  threads**, and is only accepted in the cell nearest the controller. Anywhere else that line fails to
  validate and the whole structure stays unformed. The threads are real: eco sizes its CPU array from
  this number, so it is concurrency the CPUs actually run on.

All three energized computation items (both cores and the `energized_computation_cell_4m` drive cell)
are **creative only for now** - they have no crafting recipe yet.

## L1 numbers are configurable

The `l1_computation` section of `neoecoprototype-server.toml` applies to the L1 tier alone; higher
tiers keep eco's own values.

| Option | Default | What it sets |
| --- | --- | --- |
| `cpu_threads` | 2 | crafting threads of a plain L1 subsystem |
| `cpu_accelerators` | 24 | co-processors per plain parallel core |
| `cpu_total_bytes` | 1572864 | the tier's nominal crafting storage, 1.5x the old 1 MiB |

Re-enter the world after editing. Threads are allocated when the cluster is built, so a structure that
is already standing has to be broken and formed again to pick up a new value.
