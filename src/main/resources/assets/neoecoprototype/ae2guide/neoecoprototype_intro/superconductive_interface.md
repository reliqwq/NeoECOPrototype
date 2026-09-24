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

The stock amount is how much of one marker the interface keeps available for the machines around it.
AE2 caps it at a single stack - 64 items, 4000 mB of fluid - which forces a machine to keep asking
the network for more. This interface widens every type by the same factor:

| Marker holds | AE2 interface | This interface |
| --- | --- | --- |
| Items | 64 | **8192** |
| Fluid | 4,000 mB | **512,000 mB** |
| Chemical | 4,000 mB | **512,000 mB** |

Set it by clicking the amount of a marker, the same way as on an AE2 interface.

## Passive power

While it is powered and has a channel, it injects **4000 AE/t** into the grid. AE2 keeps only one
passive generator per grid and always picks the highest output, so a single one of these takes over
from every [L1 Powered Interface](powered_interface.md) on the network and suppresses them.

## Obtaining

Both forms are made in the Integrated Working Station from Neo ECO AE Extension. The viewer
below shows the workbench conversion between the two forms; station recipes are not something
this page can render.

<RecipeFor id="neoecoprototype:superconductive_interface" />

<RecipeFor id="neoecoprototype:cable_superconductive_interface" />
