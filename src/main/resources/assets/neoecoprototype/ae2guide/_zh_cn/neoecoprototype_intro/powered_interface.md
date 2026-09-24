---
navigation:
  title: L1 供能接口
  icon: neoecoprototype:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_powered_me_interface
  - neoecoprototype:cable_powered_me_interface
---

# L1 供能接口

<Row>
  <BlockImage id="neoecoprototype:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_powered_me_interface" />
</ItemGrid>

一台布局翻倍、同时会向电网供电的 AE2 接口。它的行为与原版接口完全一致——按标记向网络索取缺的物资、
把库存供给旁边的机器——所以凡是接受 AE2 接口的地方都能用它。

## 翻倍的布局

- 标记槽 18 个（原版 9 个）
- 库存槽 18 个（原版 9 个）

除此之外全部沿用 AE2 自己的接口逻辑：前面几行配置"要留什么"，剩下的格子放它实际收到的物资。

## 被动发电

只要它通电且有频道，就以 **200 AE/t** 向电网注入电力。AE2 每个网格同时只保留一个被动发电源，并选择
输出功率最高的那台，因此一旦网络里出现更强的发电源，这台机器就会停止贡献，直到重新被选中。
详见 [Neo ECO Prototype 首页](index.md)。

## 获取

在处理器装配室里组装，装配室接受任意顺序的原料：

<RecipeFor id="neoecoprototype:simplify_powered_me_interface" />

线缆部件形态的行为完全相同，在合成台上合成：

<RecipeFor id="neoecoprototype:cable_powered_me_interface" />

两种形态之间可以在合成栏里无损互转。

## 用途

合成一台[盈能超导接口](superconductive_interface.md)需要四台它。
