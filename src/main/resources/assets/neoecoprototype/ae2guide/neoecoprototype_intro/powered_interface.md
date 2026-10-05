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

Everything else is AE2's own interface logic, and this page will not repeat it.

## Passive power

While it is powered and has a channel, it injects **200 AE/t** into the grid. AE2 keeps only one
passive generator per grid and picks the one with the highest output, so as soon as something
stronger is on the same network this machine stops contributing until it is chosen again.
That makes it a practical way to supply a subnet on its own: AE2's crystal resonance generator is
subject to the same one-per-grid rule, and at AE2's default **20 AE/t** - a tenth of this one - a
subnet that has an interface here does not need one.

## Obtaining

The block form is assembled in the processor assembly room from **2 crystal resonance generators,
1 AE2 interface and 1 superconducting processor**. Any pattern provider will push those ingredients
in, in any order. (The page cannot draw the assembly room's recipe - the guidebook only renders
crafting-table recipes - so the ingredients are written out here; the grid itself is in JEI.)

**The cable-mounted form has no recipe of its own.** It is converted out of the block form in a
crafting grid:

<RecipeFor id="neoecoprototype:cable_powered_me_interface" />

Turning the cable-mounted form back into the block form works the same way: the conversion is
lossless, and it runs in either direction.

## Further use

Four of them are the base of the
[Energized Superconductive Interface](superconductive_interface.md).
