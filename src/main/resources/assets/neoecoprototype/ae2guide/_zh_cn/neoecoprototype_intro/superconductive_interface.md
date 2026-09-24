---
navigation:
  title: 盈能超导接口
  icon: neoecoprototype:superconductive_interface
  position: 90
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:superconductive_interface
  - neoecoprototype:cable_superconductive_interface
---

# 盈能超导接口

<Row>
  <BlockImage id="neoecoprototype:superconductive_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_superconductive_interface" />
</ItemGrid>

本模组两条接口里的高端那台：与 [L1 供能接口](powered_interface.md) 相同的翻倍布局，但单个标记可囤
的量大得多，输出功率是它的二十倍。

## 翻倍的布局

- 标记槽 18 个（原版 9 个）
- 库存槽 18 个（原版 9 个）

## 存储量

"存储量"指一个标记会为旁边的机器留住多少物资。AE2 把它限制在一组之内——物品 64 个、流体 4000 mB——
于是机器得反复向网络追加索取。这台接口把所有类型都按同一倍数放大：

| 单个标记可囤 | AE2 接口 | 本接口 |
| --- | --- | --- |
| 物品 | 64 | **8192** |
| 流体 | 4,000 mB | **512,000 mB** |
| 化学品 | 4,000 mB | **512,000 mB** |

设置方式和 AE2 接口一样：点击标记上的数量。

## 被动发电

只要它通电且有频道，就以 **4000 AE/t** 向电网注入电力。AE2 每个网格同时只保留输出功率最高的那台被动
发电源，所以一台它就能接管整个网络，并把所有 [L1 供能接口](powered_interface.md) 抑制掉。
发电规则见 [Neo ECO Prototype 首页](index.md)。

## 获取

方块与线缆部件两种形态都在 Neo ECO AE Extension 的集成工作站里合成。下面这个配方图显示的是
两种形态之间在合成栏里的无损互转；工作站的配方这一页渲染不出来。

<RecipeFor id="neoecoprototype:superconductive_interface" />

<RecipeFor id="neoecoprototype:cable_superconductive_interface" />
