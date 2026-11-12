---
navigation:
  title: L1 Processor Assembler
  icon: neoecoprototype:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:simplify_pattern_provider
---

# L1 Processor Assembler

The **L1 Processor Assembler** is a green-shell version of the <ItemLink id="ae2:molecular_assembler" /> built for saves that do not have an inscriber yet: it crafts the three processors straight from patterns. It runs only this mod's processor assembly recipes; ordinary molecular assembly patterns are refused, and that work belongs to a Molecular Assembler. It is used with the L1 Pattern Provider below. If you already own eco's large integrated working station, the [L1 Processor Assembly Card](processor_assembly_card.md) runs this same family there.

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>

Both are made in a **crafting table**: the assembler is 1 Molecular Assembler + 4 Trait Casings + 4 Green Crystal Matrices, and the provider is 1 ME Pattern Provider + 4 Quartz Glass + 4 Green Crystal Matrices, giving **2** per craft.

<Row>
  <RecipeFor id="neoecoprototype:simplify_stonecutting_assembler" />
  <RecipeFor id="neoecoprototype:simplify_pattern_provider" />
</Row>

## Built-in recipes

The four processors (the last one needs AE2 Lightning Tech; other press recipes in the inscriber are also derived, see below):

| Output | Ingredients |
|--------|-------------|
| Logic Processor | silicon + redstone dust + gold ingot |
| Calculation Processor | silicon + redstone dust + certus quartz crystal |
| Engineering Processor | silicon + redstone dust + diamond |
| Overload Processor (AE2 Lightning Tech) | silicon + redstone dust + Overload Crystal |

The other eight:

| Output | Ingredients |
|--------|-------------|
| [L1 Powered ME Interface](powered_interface.md) | 2 Crystal Resonance Generators + 1 ME Interface + 1 Superconducting Processor |
| ECOP - Structure Expansion Card | Memory Card + Aluminum Ingot + Green Crystal Matrix |
| Energized Reinforced Threading Core | 2 ECO - CM4A Threading Cores + 1 ECO - C1 Computation Substructure Casing |
| ECO - CE1R Flash Crystal Array | 2 ECO - CE1 Flash Crystal Arrays + 1 ECO - L4 Storage Component |
| Snowball / powder snow bucket | 4 snow blocks → 4 snowballs; 3 snowballs + 1 bucket → powder snow bucket |
| reliqwq's Fumo Doll | the L1 storage host + C1 computation host + F1 crafting host |
| Yang120's Fumo Doll | eco's L4 storage host + C4 computation host + F4 crafting host |
| kouooki's Fumo Doll | pink dye + dried kelp + brown mushroom |

Silicon, gold and diamond are taken by tag, so any mod's version counts.

Other press recipes in the inscriber are also derived live, so a recipe another mod adds to the inscriber works with no data file. Two server config keys govern that:

| Config key | What it does |
|------------|--------------|
| `derive_processor_recipes_from_inscriber` | switches the inscriber derivation off |
| `disabled_processor_recipes` | excludes processors by output |

## Speed and batching

5 card slots take nothing but the <ItemLink id="ae2:speed_card" />: one craft costs 100 ticks empty and 20 ticks with five cards in.

However many complete sets a push carries, they finish in a **single** cycle - output scales with the multiplier, time does not - so more cards are the only way to go faster. Two limits bound how many sets a pattern may ask for, and a pattern over either one is refused whole: the materials go back to the network and the log names the offending line.

1. **`processor_assembler_max_runs_per_cycle`** (server config, default 64, range 1-192): the most sets one cycle may process. The default 64 is one full stack of a one-per-craft processor - what the cycle ejects outright, with no surplus sent to the network; lowering it tightens the machine (1 means one set per cycle), raising it widens the batch up to 192, the ceiling the recipes themselves set (the grid holds 576 items; a three-ingredient processor costs three per run). A larger pattern is refused - refused, not partly run.
2. **The nine-slot grid**: one batch has to fit those nine slots at its own stack sizes. Nine slots of 64 items hold 576 items, which is 192 sets of a three-ingredient processor; anything bigger cannot fit.

Raise the config to process more per cycle, up to 192 - the key will not take more; the nine-slot limit is physical and a config change cannot lift it. One stack of the output reaches the machine per round; the rest is inserted into the network rather than lost.

## L1 Pattern Provider

The **L1 Pattern Provider** is the L1 version of the <ItemLink id="ae2:pattern_provider" />: **27 pattern slots** instead of 9, and it round-robins ingredients into adjacent inventories exactly as vanilla does. Its recipe is at the top of this page.
