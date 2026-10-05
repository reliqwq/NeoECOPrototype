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

The L1 Processor Assembler (shown in game as the **L1 Processor Assembler Room**) is a green variant of AE2's molecular assembler that accepts **processing patterns only**. Any pattern provider feeding one of these is enough to craft the AE2 processors and the items this mod adds.

## Components

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>


## Recipes

Three built-in recipes cover AE2's own processors:

| Processor | Materials |
|-----------|-----------|
| Logic | Silicon + Redstone + Gold Ingot |
| Calculation | Silicon + Redstone + Certus Quartz Crystal |
| Engineering | Silicon + Redstone + Diamond |

The assembler also accepts processor recipes derived from AE2's inscriber press recipes, so any mod that adds inscriber recipes works with no extra files.
Server admins can tune this in the server config: toggle derivation (`derive_processor_recipes_from_inscriber`) or exclude specific outputs (`disabled_processor_recipes`).

Insert speed and energy cards to accelerate it - five slots each, and AE2's own acceleration rule
applies unchanged: with five speed cards a cycle takes **2 ticks**.

Besides the processors, the assembler ships with recipes for the L1 Powered Interface (see
[L1 Powered Interface](powered_interface.md)), the Energized Computation Threading Core and the CE1R
expansion (see [L1 Computation System](computation_system.md)), three named dolls, and **pressing
snow blocks into snowballs** - four snow blocks make 4 snowballs. The Integrated Working Station does
the same job better: one snow block already makes **4** snowballs.

## Batched patterns

The assembler accepts multiplied pattern inputs: however many complete sets the provider pushes in one go, it finishes them in a **single** crafting cycle, so the output scales with the number of sets and the **cycle time does not change** - batching costs nothing, and only speed cards make it faster. A pattern can ask for 64 logic processors at once (64 redstone, 64 silicon, 64 gold ingot) and the assembler still runs one cycle for it.

How large a multiplier a pattern may carry is decided by the **nine grid slots**: each ingredient is split into stacks of its own size, and once those stacks overflow nine slots the pattern is refused - the materials go back to the network and the log says which rule stopped it. For the processors (three ingredients, sixty-four to a stack) that ceiling is **192x**. Only one stack leaves the machine per cycle; whatever the cycle cannot eject goes straight into the network instead of vanishing.

## Pattern provider

The green L1 pattern provider stores the assembler's processing patterns and pushes work into it, like AE2's pattern provider does for molecular assemblers. It holds 27 patterns to AE2's 9, and its recipe gives **2 of them per craft** - one crafting job, two providers.
