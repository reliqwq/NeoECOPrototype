---
navigation:
  title: Energized Superconductive Interface
  icon: neoecoprototype:superconductive_interface
  position: 90
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:superconductive_interface
  - neoecoprototype:cable_superconductive_interface
---

# Energized Superconductive Interface

<Row>
  <BlockImage id="neoecoprototype:superconductive_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_superconductive_interface" />
</ItemGrid>

The top of the two interfaces this mod adds: the same doubled
[L1 Powered Interface](powered_interface.md) layout, but with a far larger stock amount per
marker and twenty times the power output.

## Doubled layout

- 18 marker slots instead of 9
- 18 storage slots instead of 9

## Stock amount

| Marker holds | AE2 interface | This interface |
| --- | --- | --- |
| Items | 64 | **8192** |
| Fluid | 4,000 mB | **512,000 mB** |
| Chemical | 4,000 mB | **512,000 mB** |

Set it the same way as on an AE2 interface.

## Passive power

While it is powered and has a channel, it injects **4000 AE/t** into the grid. AE2 keeps only one
passive generator per grid and always picks the highest output, so a single one of these takes over
from every [L1 Powered Interface](powered_interface.md) on the network and suppresses them.

## Obtaining

The block form is assembled in the Integrated Working Station from Neo ECO AE Extension, from
**1 Cryotheum Crystal, 4 L1 Powered Interfaces, 9 Energized Superconductive Ingots and 4
Superconducting Processors**, at a cost of 10,000 AE. (That recipe cannot be drawn on this page
either - the guidebook only renders crafting-table recipes.)

**The cable-mounted form has no recipe of its own and is not a station recipe at all.** It is
converted out of the block form in a crafting grid:

<RecipeFor id="neoecoprototype:cable_superconductive_interface" />

Turning it back into the block form works the same way, and both directions are lossless.
