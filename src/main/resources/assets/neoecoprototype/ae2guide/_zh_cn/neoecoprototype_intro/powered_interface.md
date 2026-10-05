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

一台布局翻倍拥有更多配置槽位、同时会向电网供电的 AE2 接口。它的行为与原版接口完全一致——按标记向网络索取缺的物资、
把库存供给旁边的机器——所以凡是接受 AE2 接口的地方都能用它。

## 翻倍的布局

- 标记槽 18 个（原版 9 个）
- 库存槽 18 个（原版 9 个）

除此之外全部沿用 AE2 自己的接口逻辑，这里不再赘述。

## 被动发电

只要它通电且有频道，就以 **200 AE/t** 向电网注入电力。AE2 每个网格同时只保留一个被动发电源，并选择
输出功率最高的那台，因此一旦网络里出现更强的发电源，这台机器就会停止贡献，直到重新被选中。
因此可以作为高效的子网供电手段，无需忍受水晶谐振发电机的低效（AE2 的默认只有 20 AE/t，是我们这台的十分之一）与它对主网络的更新。
详见 [Neo ECO Prototype 首页](index.md)。

## 获取

方块版在处理器装配室里组装：**2 台 AE2 的水晶谐振发电机、1 个 AE2 接口、1 枚超导处理器**。装配室接受
样板供应器推进来的原料，任意顺序都行。（装配室的配方图这一页画不出来——指南只渲染原版合成台那一类
配方，所以原料写在文字里，具体格子去 JEI 看。）

**线缆部件形态没有自己的合成配方**，只能由方块版在合成栏里转出来：

<RecipeFor id="neoecoprototype:cable_powered_me_interface" />

反过来把线缆部件版转回方块版也一样无损，两个方向都能转。

## 后续用途

合成一台[盈能超导接口](superconductive_interface.md)需要四台它。
