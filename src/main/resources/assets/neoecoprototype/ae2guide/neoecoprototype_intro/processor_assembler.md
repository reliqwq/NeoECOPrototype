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

The L1 Processor Assembler (shown in game as the **L1 Processor Assembler Room**) is a green variant of AE2's molecular assembler that crafts processors from AE2 processing patterns. Pair it with a <ItemLink id="neoecoprototype:simplify_pattern_provider" /> and request processors from your network as usual.

## Recipes

Three built-in recipes cover the AE2 processors:

| Processor | Materials |
|-----------|-----------|
| Logic | Silicon + Redstone + Gold Ingot |
| Calculation | Silicon + Redstone + Certus Quartz Crystal |
| Engineering | Silicon + Redstone + Diamond |

The assembler also accepts processor recipes derived from AE2's inscriber press recipes, so any mod that adds inscriber recipes works with no extra files. Server admins can toggle derivation or exclude specific outputs in the server config (`derive_processor_recipes_from_inscriber`, `disabled_processor_recipes`).

Insert speed and energy cards to accelerate it - five slots each.

## Pattern provider

The green pattern provider stores the assembler's processing patterns and pushes work into it, like AE2's pattern provider does for molecular assemblers.
