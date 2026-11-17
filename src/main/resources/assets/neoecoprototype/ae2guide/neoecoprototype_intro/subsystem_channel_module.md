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

## Introduction

ECOP provides a quick and convenient wireless solution. Connect the **ECOP Network Subsystem Channel Core** (the
**Wireless Communication Core** for short) to an ME controller network, then use a
<ItemLink id="neoecoprototype:wireless_link_card" /> to bind devices. It provides six wireless outlets, each with
the channel capacity of one ME controller face: **6 × 32 = 192 channels**.

## Building it

1. Place the Wireless Communication Core so it touches the ME controller or its dense cable, joining the
   controller network.
2. Hold the <ItemLink id="neoecoprototype:wireless_link_card" /> and sneak-right-click the core to mark it.
3. Sneak-right-click any network device to establish the wireless link.

A dense cable bus is the better far end: it carries 32 channels, while a machine endpoint is limited by that
machine's own channel capacity.

## Panel overview

Right-click the Wireless Communication Core to open its panel:

| Column | Purpose |
|--------|---------|
| Left - Select a node | Select an outlet and inspect the channel usage of its current network. |
| Middle - Overview | See the core's total channel usage, remaining channels, and dimension. |
| Right - Precise controls | Highlight both ends of the selected link, refresh channel status, or disconnect the selected device. |

## Rules to know

- **Auto-connect on placement.** If a marked <ItemLink id="neoecoprototype:wireless_link_card" /> is in your hotbar,
  a placed network device automatically connects to its marked outlet after AE2 creates its node. When Curios is
  installed, a card in an accessory slot also triggers auto-connect. If cards marked for different networks are
  present, the target is not guessed.
- **Distance limit.** The default range is 50 blocks. The core's right-hand upgrade slots accept
  <ItemLink id="ae2:wireless_booster" /> (a wireless booster): each doubles the range to 100, 200, then 400 blocks;
  four boosters remove the limit. Changing the range does not break links already established.
- **Power use.** Each active link draws 50 AE/t by default from the joined network.
- **The core is a hub, not a wireless hop.** It can link to a machine or a dense cable bus, but two cores cannot
  link to each other.
- **Loops are refused.** AE2 merges networks when a link is made, so a link that would close a loop is rejected.
- **The far square needs a node.** A cable bus without a cable has no node. A newly placed device may need another
  tick before AE2 creates its node and auto-connect can finish.
- **Outlets share when they can.** A network that fits in an outlet's remaining capacity shares it; one that does
  not gets its own outlet.
