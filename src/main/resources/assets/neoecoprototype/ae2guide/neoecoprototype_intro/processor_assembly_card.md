---
navigation:
  title: L1 Processor Assembly Card
  icon: neoecoprototype:processor_assembly_workstation_card
  position: 31
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:processor_assembly_workstation_card
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:energized_computation_core
  - neoecoprototype:simplify_green_crystal_matrix
---

# L1 Processor Assembly Card

The **L1 Processor Assembly Card** is an upgrade module for eco's <ItemLink id="neoecoae:large_integrated_working_station" />. With it in the station's module slot, that station also runs this mod's processor-assembly recipes - the same family the [L1 Processor Assembler](processor_assembler.md) runs.

<ItemGrid>
  <ItemIcon id="neoecoprototype:processor_assembly_workstation_card" />
</ItemGrid>

## Where it goes

The station has **one** module slot, at the top right of its screen, and it holds one card at a time: installing this card means eco's other module cards are not installed. The station's own recipes, and compatible recipes that need no module, keep working either way; a family that belongs to another module card needs that card instead.

The card does **not** go into the L1 Processor Assembler: that machine's five upgrade slots accept only <ItemLink id="ae2:speed_card" /> and energy cards.

## How it is made

The card is crafted **in eco's large integrated working station** - not at a crafting table, and not in our assembler:

| Ingredient | Count |
|------------|-------|
| L1 Processor Assembler | 8 |
| Energized Reinforced Computation Core | 1 |
| Green Crystal Matrix | 32 |

One card costs 10000 AE. The station's recipe screen cannot be drawn on this page, so the materials are in the table.

The card is priced in the station's recipe shape on purpose: our own processor-assembly recipe type states neither per-ingredient counts nor an energy cost, so a card written there would have cost one unit of each ingredient.

## Encoding patterns

One pattern execution runs our recipe **64 times**, and a pattern is matched only when its output is an exact multiple of that batch. Encode it at 64 runs:

| Recipe | 64 runs produce | Energy per batch |
|--------|-----------------|------------------|
| Processor (1 per run) | 64 processors | 64 AE |
| Snowball (4 per run) | 256 snowballs | 64 AE |

The inputs have to be encoded at the same multiple: a 64x Logic Processor pattern is 64 silicon + 64 redstone + 64 gold ingots for 64 processors.

- A pattern smaller than one batch - 1x or 32x, say - is not accepted by the station, and its materials are never pulled out of the network.
- Doubling the pattern doubles both the materials and the energy, so the price per item does not change: one processor is still 1 AE.
- The batch size does not slow a batch down; the station's own overclock decides the pace.
- The [L1 Processor Assembler](processor_assembler.md) is not affected - it still accepts 1x patterns and keeps its own nine-slot batch rules.
