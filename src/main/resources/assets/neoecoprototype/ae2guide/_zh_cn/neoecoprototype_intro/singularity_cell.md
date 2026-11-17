---
navigation:
  title: L1 奇点元件
  icon: neoecoprototype:simplify_singularity_cell
  position: 22
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_singularity_cell
  - neoecoprototype:simplify_sky_stone_dust_cell
---

# L1 奇点元件

插进驱动器之后，**每 30 秒产出 120 个奇点**：它不需要任何输入，也不消耗任何东西，取用方式与普通存储元件相同。

## 多快

一**批** = 一次到账的一份产出，默认 120 个：默认间隔 600 tick（30 秒），平均约每秒 4 个。

| 简单估计 | 可产出 |
|----------------|--------|
| 30 秒 | 120 |
| 1 分钟 | 240 |
| 5 分钟 | 1,200 |
| 30 分钟 | 7,200 |
| 1 小时 | 14,400 |
| 24 小时 | 345,600 |

| 用途 | 需要 | 默认速率下 |
|------|------|------------|
| 本模组三台 L1 接口 | 6 | 第一批就到（30 秒） |
| eco 一只 MEGA 4G 矩阵，或一个无限存储组件 | 128 | 两批，60 秒 |
| eco 的无限存储解锁（64 个组件） | 8,192 | 69 批，约 35 分钟 |

## 能调多快

服务端配置的 `singularity_cell` 段里两个键，每次现读：

| 键 | 默认 | 范围 |
|----|------|------|
| `ticks_per_batch` | 600 tick（30 秒） | 最短 60 tick（3 秒） |
| `amount_per_batch` | 120 | 1 到 9,223,372,036,854,775,807，随便填 |

库存数值本身是一个 long：**达到 long 上限 9,223,372,036,854,775,807 后饱和，不再往上走**。

## 库存是算出来的

元件内部不累加数量。需要注意修改 `amount_per_batch` 会连历史一起重算、库存立刻变大，调小可能把已存的清掉。

## 限制

只有一种内容物，因此不能在元件工作台里分区，也不能装进另一只存储单元。它属于 L1 这一级，[L1 存储系统](storage_system.md) 直接收，兼容eco 主机。

## 同一族的另一台：陨石粉矩阵

<ItemLink id="neoecoprototype:simplify_sky_stone_dust_cell" /> 是同一套引擎的第二台，产出的是 AE2 自己的陨石粉（`ae2:sky_dust`）：默认也是**每 30 秒产出 120 个**，同样什么都不吃，库存按世界时间自己长。

速度在服务端配置的 `sky_stone_dust_cell` 段里调，两个键与奇点那段同名、同默认值（`ticks_per_batch` 默认 600 tick，最短 60 tick；`amount_per_batch` 默认 120），但**两段互不影响**：改奇点的速度不会动到这只。

## 同一族的第三台：魔源矩阵

第三台产出的是魔源：默认同样**每 30 秒产出 120**，配置在服务端配置的 `source_cell` 段，键名、默认值与范围都和前两台一样，同样互相独立。

它**只在装了 Ars Energistique（`arseng`，以及它的前置 Ars Nouveau）时才会注册**；没装时这只物品不存在，也不会出现在创造栏里。
