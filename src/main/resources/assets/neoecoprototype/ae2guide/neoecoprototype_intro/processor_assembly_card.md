---
navigation:
  title: L1 Processor Assembly Card
  icon: neoecoprototype:processor_assembly_workstation_card
  position: 31
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:processor_assembly_workstation_card
---

# L1 Processor Assembly Card

The **L1 Processor Assembly Card** is an upgrade module for the large integrated working station. With it installed, the station runs this mod's processor-assembly recipes the same way the [L1 Processor Assembler](processor_assembler.md) does: patterns go into the communication interface, and ingredients and results move through the ME network.

<ItemGrid>
  <ItemIcon id="neoecoprototype:processor_assembly_workstation_card" />
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:energized_computation_core" />
  <ItemIcon id="neoecoprototype:simplify_green_crystal_matrix" />
</ItemGrid>

## Installing

The station's controller has one **upgrade module** slot at the top right of its screen, and it holds one card at a time. Installing this card means eco's other module cards are not installed - one slot, one route at a time. The station's own recipes, and compatible recipes that need no module, keep working either way.

The card does **not** go into the L1 Processor Assembler: that machine's five upgrade slots take only <ItemLink id="ae2:speed_card" /> and energy cards, and it already carries the processor recipes itself.

While the card is installed, the controller takes our look, and this family appears in JEI / EMI under the "Large Integrated Working Station" category.

## Crafting

Like the other upgrade modules, the card is crafted **in a large integrated working station**:

<RecipeFor id="neoecoprototype:processor_assembly_workstation_card" />

One card costs 10000 AE.

## Encoding patterns

This family is ordered in **batches of 64**: one pattern execution runs our recipe 64 times. A pattern has to state the whole batch, ingredients and results alike.

| Recipe | One batch | Energy per batch |
|--------|----------:|-----------------:|
| Processor (1 per run) | 64 | 64 AE |
| Snowball (4 per run) | 256 | 64 AE |

The ingredients scale the same way. A 64x Logic Processor pattern, for instance, is 64 silicon + 64 redstone + 64 gold ingots for 64 logic processors.

- A pattern smaller than one batch - 1x or 32x - is not accepted, and its materials are never pulled from the network.
- Scaling the pattern up scales the ingredients and the energy together, so the price per item does not change: one processor is still 1 AE.
- Batch size does not slow a batch down: every batch is worked in 1 tick, and the station's overclock and parallelism decide how many run at once.
- The [L1 Processor Assembler](processor_assembler.md) is unaffected: it still takes 1x patterns and keeps its nine-slot batch rules.

## Common questions

| Symptom | Cause and fix |
| --- | --- |
| I cannot find our recipes | Look under the "Large Integrated Working Station" category in JEI / EMI; for the station to accept them, the card also has to be in the module slot |
| A pattern goes in but is never accepted | Check that the count is a multiple of 64 and the ingredients are scaled by the same multiple |
| I want another mod's module back | The slot holds one card at a time - take this one out first |
