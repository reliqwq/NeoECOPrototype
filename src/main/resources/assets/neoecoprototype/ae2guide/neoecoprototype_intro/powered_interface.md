---
navigation:
  title: L1 Powered Interface
  icon: neoecoprototype:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_powered_me_interface
  - neoecoprototype:cable_powered_me_interface
---

# L1 Powered Interface

<Row>
  <BlockImage id="neoecoprototype:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_powered_me_interface" />
</ItemGrid>

An AE2 interface with a doubled layout that also feeds power into the grid. It behaves exactly like
the stock interface - it keeps a set of markers, requests what is missing from the network and
supplies the machines next to it - so anything that accepts an interface accepts this one.

## Doubled layout

- 18 marker slots instead of 9
- 18 storage slots instead of 9

Everything else is AE2's own interface logic: the first rows configure what should be kept, the
remaining slots hold what the interface actually received.

## Passive power

While it is powered and has a channel, it injects **200 AE/t** into the grid. AE2 keeps only one
passive generator per grid and picks the one with the highest output, so as soon as something
stronger is on the same network this machine stops contributing until it is chosen again.

## Obtaining

It is assembled in the processor assembly room, which accepts the ingredients in any order:

<RecipeFor id="neoecoprototype:simplify_powered_me_interface" />

The cable-mounted form behaves identically and is worked out on a crafting table:

<RecipeFor id="neoecoprototype:cable_powered_me_interface" />

The two forms convert into each other losslessly in a crafting grid.

## Used for

Four of them are the base of the
[Energized Superconductive Interface](superconductive_interface.md).
