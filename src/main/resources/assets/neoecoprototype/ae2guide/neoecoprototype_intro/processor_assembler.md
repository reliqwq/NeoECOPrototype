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

## Batched patterns

The assembler accepts multiplied pattern inputs: however many complete sets the provider pushes in one go, it finishes them in a **single** crafting cycle, so the output doubles with the number of sets. The result is capped at one stack (64 items) and the **cycle time does not change** - batching costs nothing, and only speed cards make it faster.
So a pattern can ask for 64 logic processors at once - 64 redstone, 64 silicon, 64 gold ingot - and the assembler still runs one cycle for it. The one hard ceiling is the pattern's own output: it cannot exceed the 64-item output slot.

## Pattern provider

The green L1 pattern provider stores the assembler's processing patterns and pushes work into it, like AE2's pattern provider does for molecular assemblers. It holds 27 patterns to AE2's 9, and its recipe gives **2 of them per craft** - one crafting job, two providers.
