---
navigation:
  title: ECOP Network Subsystem Channel Core
  icon: neoecoprototype:subsystem_channel_module
  position: 26
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:subsystem_channel_module
  - neoecoprototype:wireless_link_card
  - neoecoprototype:wireless_unlink_card
---

# ECOP Network Subsystem Channel Core

<Row>
  <BlockImage id="neoecoprototype:subsystem_channel_module" scale="4"></BlockImage>
</Row>

The **ECOP Network Subsystem Channel Core** is an AE2 device that hands out **six outlets of 32 channels each** to networks
that are not physically connected to it. A wireless link joins one outlet to a machine or a dense cable bus
somewhere else, and everything behind that square joins your network as if a cable ran to it.

## What it is for

An <ItemLink id="ae2:controller" /> pays 32 channels per face, and a dense cable carries 32. When a base grows
past that, the usual answer is another controller or a longer cable run. The module is the third answer: put one
next to your controller, and it gives you six more 32-channel faces that reach anywhere in the world.

Six outlets, 32 channels each - **192 channels** from one block.

## Building it

1. Place the module so it touches your controller or its dense cable. It has to be on a powered network with a
   controller for the outlets to be worth anything: without one, AE2 gives the whole network ad-hoc channels
   and the outlets do not count separately.
2. Mark a machine or a dense cable bus with the <ItemLink id="neoecoprototype:wireless_link_card" /> - right-click
   the square once to mark it.
3. Right-click the module with the same card. The two squares are joined and the module opens an outlet for it.

A **dense cable bus** is the better far end: it is a 32-channel face of its own, so the link's width is not
capped by a machine. A machine works too and enters at that machine's own width.

## Reading the panel

Right-click the module to open it. The three columns are one reading in order:

| Column | What it shows |
|--------|---------------|
| Left | The six outlets. The chip is the outlet, the lamp is its state: green = channels left, blue = carrying a network, red = its 32 are used up. Click a row to choose it. |
| Middle | The whole module: `used/192`, what is left, the dimension, and the outlet selector. Hover the first reading for the module's state - **idle** (fewer than six outlets carry a network), **healthy** (all six do), **congested** (all 192 are spent). |
| Right | The links on the chosen outlet. Each row is the far block's icon and its position; hover for the whole name. |

The three buttons under the middle column work on the selected row: highlight both ends, re-read the panel, and
unlink.

## Rules worth knowing

- **A module is a hub, not a wireless hop.** It can link to a machine or a dense cable bus, but two modules cannot
  link to each other: that would let their outlet budgets be chained over and over.
- **A link that would close a loop is refused.** AE2 merges two networks the moment an edge joins them, so the
  edge that would close a triangle finds both ends already on one network and is turned away.
- **Auto-connect on placement.** If a marked <ItemLink id="neoecoprototype:wireless_link_card" /> is anywhere in
  your hotbar when you place a network device, the link is made automatically after AE2 builds that device's
  node. If two different marked cards are in the hotbar, nothing is guessed - keep just the card for the network
  you want to join.
- **Jade shows the link.** Look at either connected device or cable bus: one line per state, with `×N` after it
  when several links are in that state. The remote square is not named - the card's manager lists that.
- **An outlet shares when it can.** A network that fits in what an outlet has left is packed onto it; one that
  does not gets an outlet of its own. A port that reads `24/32` will still take a network needing eight.
- **The far square needs a node.** A cable bus with no cable in it never gets one - plug a cable into the bus.
  A block placed a second ago may need one more tick to build its node before auto-connect can finish.
- **Each link draws power.** `wireless_ae_per_link_per_tick` in the server config, 50 AE/t by default, billed
  against the joined grid.
