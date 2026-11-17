---
navigation:
  title: Flash Ascension Drives
  icon: neoecoprototype:migration_drive_1_4
  position: 33
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:migration_drive_empty
  - neoecoprototype:migration_drive_1_4
  - neoecoprototype:migration_drive_4_6
  - neoecoprototype:migration_drive_6_9
  - neoecoprototype:migration_drive_creative
---

# Flash Ascension Drives

A Flash Ascension Drive upgrades a formed, fully expanded storage, computation, or crafting system in place. Hold the matching drive and sneak-right-click the machine's controller.

| Drive | Upgrade |
|---|---|
| <ItemImage id="neoecoprototype:migration_drive_1_4" scale="3" /> | L1 → L4 |
| <ItemImage id="neoecoprototype:migration_drive_4_6" scale="3" /> | L4 → L6 |
| <ItemImage id="neoecoprototype:migration_drive_6_9" scale="3" /> | L6 → L9 |
| <ItemImage id="neoecoprototype:migration_drive_creative" scale="3" /> | Creative drive: L1, L4, or L6 → L9 |

The machine must already be formed and at its maximum length. A drive for another rung, an incomplete machine, or a machine already at L9 is refused without consuming the drive. A successful upgrade consumes the drive.

Mounted storage cells and patterns stored in pattern buses are carried into the new machine. Interface settings are not preserved. Replaced blocks are not returned by default; enable `migration_drive.refund_replaced_blocks` in the server config to collect them in a backup drive. This setting never affects mounted cells or stored patterns.

The blank drive is crafted with the L1 Structure Expansion Card and a 1k ME Storage Component. Each rung drive is crafted at eco's Large Integrated Working Station and requires eight computation parallel cores, eight crafting parallel cores, and eleven ECO storage components of its tier. From the 4–6 rung onward, the previous rung drive is also required:

- **1–4 drive**: blank drive + 8 L4 computation parallel cores + 8 L4 crafting parallel cores + 11 16M ECO storage components.
- **4–6 drive**: 1–4 drive + 8 L6 computation parallel cores + 8 L6 crafting parallel cores + 11 64M ECO storage components.
- **6–9 drive**: 4–6 drive + 8 L9 computation parallel cores + 8 L9 crafting parallel cores + 11 256M ECO storage components.
- **Creative drive**: one each of the 1–4, 4–6, and 6–9 drives. See JEI for the recipes.
