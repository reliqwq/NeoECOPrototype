---
navigation:
  title: Plushie Doll
  icon: neoecoprototype:fumo_reliqwq
  position: 70
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:fumo_reliqwq
---

# Plushie Doll

The doll is a seated plushie that fills its block. Its face, shirt and sleeves are taken straight from a player's skin, so every doll looks like the person it belongs to.

A placed doll block glows: it lights its surroundings at light level 15, the brightest level in the game.

## Crafting

Craft it in the [L1 Processor Assembler](processor_assembler.md) from a set of three subsystem hosts.

Encode a processing pattern with the three hosts in the material slots and the doll as the output, then put that pattern in the assembler.

| Doll | Materials |
|------|-----------|
| reliqwq doll | L1 storage + L1 computation + L1 crafting system controller |
| Yang120 doll | L4 storage + L4 computation + L4 crafting system controller |
| kouooki doll | pink dye + dried kelp + brown mushroom |

## Getting any player's doll

In creative mode or as OP, take a doll of any player directly:

```
/prototypefumo <player name>
```

The name is resolved through the same profile lookup vanilla player heads use, so the player does not have to be online.

An unknown name falls back to the placeholder skin instead of failing.

The command can be switched off with the server config option `fumo_command_enabled`, which defaults to true.

## Wearing it

The doll fits in the helmet slot:

- **Every doll**: Night Vision, 30 seconds, refreshed automatically while worn.
- **The named dolls (reliqwq, Yang120, kouooki)**: bonus armour while worn on the head, and a green name. reliqwq and Yang120 give +4 armour / +2 toughness; kouooki gives +1 armour / +5 toughness.

Right-click places the block, so to wear the doll drag it into the helmet slot.
