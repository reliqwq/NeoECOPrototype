---
navigation:
  title: L1 Crafting System
  icon: neoecoprototype:simplify_crafting_system
  position: 60
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_crafting_system
  - neoecoprototype:simplify_crafting_casing
  - neoecoprototype:simplify_crafting_parallel_core
  - neoecoprototype:simplify_crafting_pattern_bus
  - neoecoprototype:simplify_crafting_worker
  - neoecoprototype:simplify_crafting_vent
  - neoecoprototype:simplify_crafting_interface
  - neoecoprototype:simplify_crafting_network_interface
---

# L1 Crafting System

The L1 Crafting System is the early-game version of eco's Crafting System: a multiblock that executes patterns in parallel, raising pattern throughput for the ME network.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## Components

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_crafting_system" />
  <ItemIcon id="neoecoprototype:simplify_crafting_casing" />
  <ItemIcon id="neoecoprototype:simplify_crafting_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_crafting_pattern_bus" />
  <ItemIcon id="neoecoprototype:simplify_crafting_worker" />
  <ItemIcon id="neoecoprototype:simplify_crafting_vent" />
  <ItemIcon id="neoecoprototype:simplify_crafting_interface" />
  <ItemIcon id="neoecoprototype:simplify_crafting_network_interface" />
</ItemGrid>


The concept matches eco's Crafting System - see eco's guidebook for the full mechanics. The L1 variant above is the minimal working structure.

Proofread note: confirm the intended throughput/worker behavior for the L1 variant before wide release.
