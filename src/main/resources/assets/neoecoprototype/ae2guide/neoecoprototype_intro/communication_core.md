---
navigation:
  title: Communication Core
  icon: neoecoprototype:communication_base
  position: 91
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:communication_base
  - neoecoprototype:communication_core
  - neoecoprototype:communication_range_module
  - neoecoprototype:communication_efficient_module
---

# Communication Core

<Row>
  <BlockImage id="neoecoprototype:communication_base" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:communication_core" />
  <ItemIcon id="neoecoprototype:communication_range_module" />
  <ItemIcon id="neoecoprototype:communication_efficient_module" />
</ItemGrid>

The wireless family's mid-game hub: a 1xN tower that carries **many** wireless links on one machine, while the [link cards](subsystem_channel_module.md) stay the early point-to-point tool. The two coexist - a card joins any two devices anywhere, the tower is the thing you build once and keep feeding devices into.

## Building the tower

The **Communication Core Base** is the controller; right-click it once to see the panel, and **sneak-right-click** it with an empty hand to raise the column (the preview follows the build length you pick, up to **9 blocks** tall with the shipped config). Stack **Communication Core Sections** on top by hand, and swap any section for a module later - breaking a section unforms the tower until you rebuild the column.

| Block | What it does | Per tower |
|-------|--------------|-----------|
| Base | The controller; every link and fee lives here | 1 |
| Section | +4 devices of capacity, +10 AE/t of idle draw | up to 8 |
| Range Module | +8 blocks of radius | 2 |
| Efficient Module | Idle draw x0.6 | 1 |

## Enrolling devices

Sneak-right-click the **formed** tower with a [wireless link card](subsystem_channel_module.md) to mark it, then tap each device the same way you would for a card-to-card link - the mark survives every join, so one card wires a whole room to the tower. An unformed tower refuses with "not formed"; a device outside the radius refuses with "out of range".

## What it costs

A formed tower draws **10 AE/t per standing block** from its own grid (an Efficient Module multiplies that by 0.6), and every live link bills the usual per-link fee on top. Height also buys reach: a tower starts at **16 blocks** of radius, +8 per Range Module, and the global `wireless_max_range_blocks` and booster cards can only add to that, never shrink it.

The tower's outlets are the same virtual dense ports the [ECOP Network Subsystem Channel Core](subsystem_channel_module.md) uses - six of them, 32 channels each - so channels, not the device count, are usually what runs out first. Two towers cannot link to each other, the same way two channel cores cannot.
