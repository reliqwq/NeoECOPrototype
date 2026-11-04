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
  - neoecoprototype:simplify_fluid_input_hatch
  - neoecoprototype:simplify_fluid_output_hatch
---

# L1 Crafting System

The **L1 Crafting System** is the L1 version of eco's <ItemLink id="neoecoae:crafting_system_l4" />: put patterns in the bus and the worker cores execute the jobs the network hands over. It idles at **1/8** of eco's L4 equivalent. This machine executes; the computation system decides how many threads exist - **the two numbers are counted separately and neither adds to the other**. The image above is the smallest working shape.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| Aspect | Value |
|--------|-------|
| Parallelism, per parallel core | **16** |
| With the overclock switch on | **48** per core - eco adds the tier's overclock bonus to the base rather than swapping it (16 + 32), so never read the second number as "32 instead of 16" |
| A full machine | at the default maximum length the frame holds **11 workers and 22 parallel cores**, so its thread pool is 352 with the switch off and 1056 with it on |
| Crafts per tick | throughput is the batch divided by the ticks it takes. Switched on, one batch is 11 × 64 = **704** crafts, which runs at **352 per tick** (2 ticks) with no coolant, **176 per tick** on water (4 ticks), and back to 352 on sodium or cryotheum (2 ticks). With the switch off every tier takes 10 ticks per batch. |
| Compared with eco's tiers | eco's L4 host gives **24** per parallel core, L6 gives 72 and L9 gives 256 - an L1 host is the modest one, and it is modest mostly because the shape accepts fewer cores |
| Against the computation side | counted separately: this machine supplies 16 / 48 per core, the computation system supplies threads and their accelerators |

## Parts

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_crafting_system" />
  <ItemIcon id="neoecoprototype:simplify_crafting_casing" />
  <ItemIcon id="neoecoprototype:simplify_crafting_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_crafting_pattern_bus" />
  <ItemIcon id="neoecoprototype:simplify_crafting_worker" />
  <ItemIcon id="neoecoprototype:simplify_crafting_vent" />
  <ItemIcon id="neoecoprototype:simplify_crafting_interface" />
  <ItemIcon id="neoecoprototype:simplify_crafting_network_interface" />
  <ItemIcon id="neoecoprototype:simplify_fluid_input_hatch" />
  <ItemIcon id="neoecoprototype:simplify_fluid_output_hatch" />
</ItemGrid>

| Part | Holds | What it does | Idle draw | What its absence costs |
|------|-------|--------------|-----------|------------------------|
| Crafting host | - | Fixes the tier at L1; its front face is the control face | 4 AE/t | The structure never forms |
| F1 Worker Core | - | The slot that actually performs a craft | 4 AE/t | One lane less |
| Parallel core | - | Supplies parallel lanes | 4 AE/t | Fewer lanes |
| F1 smart pattern bus | crafting patterns | Where patterns live; the network reads patterns from here | 2 AE/t | That column has nothing to run |
| Crafting vent | - | Appearance and an idle draw, that is all - **there is no heat or shutdown mechanic** | 4 AE/t | 4 AE/t less, no function lost |
| F1 crafting fluid input hatch | coolant | Fixed slot: **takes in the fluid active cooling needs** - not recipe fluids | 2 AE/t | The structure never forms |
| F1 crafting fluid output hatch | - | Fixed slot: drains the spent coolant out | 2 AE/t | The structure never forms |
| Crafting interface | - | Fixed slot: invisible once formed, hands the cluster to outside ME cables | 2 AE/t | The structure never forms |
| Crafting network interface | - | The one the player opens, with eco's interface UI | 2 AE/t | No GUI; nothing else changes |
| Crafting casing | - | Fills out the frame | 2 AE/t | The structure never forms |

## Building it

1. Place the host facing out, then frame the rest with casings.
2. Put the crafting network interface - the one you can open - **behind and to the left of the host**, with the fluid input hatch above it and the output hatch below.
3. Starting one block right of the casing on the host's right side, lay F1 Worker Cores horizontally.
4. Put a row of parallel cores above and below those workers, and the crafting vent directly behind them.
5. Put a row of smart pattern buses above and below that vent.
6. Fill the remaining positions with casings.

Lengthening the structure means adding workers, parallel cores, pattern buses and vents together. If you would rather not place blocks one by one, use the **auto-build panel** in the host's GUI - what it plans is the authoritative shape, and those six steps are the order it lays them out.

## Overclocking and cooling

The overclock and active-cooling switches, and what coolant tier they demand, are eco's own rules - see eco's crafting system page. This tier's numbers are **16** per parallel core with the switch off and **48** with it on, because eco sums the tier's parallelism and its overclock bonus (16 + 32) instead of trading one for the other. One thing worth knowing before you blame a build: the switch lives on the network, not on one machine - the leader host's setting is copied to every host sharing it, and eco refuses to flip it while a job is running. How long a batch takes is eco's arithmetic too, not a number we set: it reads the slack between the machine's thread pool and its batch depth, in 5% steps, and active cooling caps that result by the coolant's own tier - water is the reason a switched-on L1 host settles at 4 ticks per batch instead of 2. The coolant level has one extra reader on our side: the crafting host's **machine mind** uses it to decide how much idle power to save, so a fuller tank changes the standby figure, not whether overclocking is allowed.
