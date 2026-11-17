---
navigation:
  title: 支持的存储矩阵
  icon: neoecoprototype:simplify_item_storage_cell_1m
  position: 11
  parent: neoecoprototype_intro/index.md
---

# 支持的存储矩阵

这一页把本模组能注册进 [L1 存储系统](storage_system.md) 的矩阵放在一处对照：**先按要存的资源选系列，再比容量、类型上限与需要的模组**。每一族的细节在各自的页里。

## 目前支持的

| 要存储的资源 | 矩阵系列 | 需要的模组 |
|--------------|----------|------------|
| 物品 | 常规物品矩阵 | — |
| 流体 | 常规流体矩阵 | — |
| Mekanism 化学品 | 化学品矩阵 | Mekanism + Applied Mekanistics |
| MEGA| 小宗存储矩阵 | MEGA Cells（化学品小宗另需 Applied Mekanistics） |
| Forge Energy（FE） | FE 存储矩阵 | AppliedFlux |
| 新生魔艺魔源 | 魔源矩阵 | Ars Énergistique（它自己的硬依赖是新生魔艺） |
| Mek乏核 | 乏核废料存储矩阵 | AE2 Omni Cells + Mekanism + Applied Mekanistics |
| omni | 全能 / 复合全能 / 量子全能 | AE2 Omni Cells |
| 容量不限 | 无限存储矩阵 | — |
| 自己增加 | 奇点元件、陨石粉元件、魔源单元、自产矩阵 | 魔源单元要 Ars Énergistique；自产那一档要 KubeJS |

## 容量、类型上限与待机

| 系列 | 档位 | 类型上限 | 待机耗电 |
|------|------|----------|----------|
| 常规物品 / 流体 | 1K、16K、64K、1M、4M | 256 | 见 [L1 存储系统](storage_system.md) |
| 化学品 | 1K、16K、64K、1M、4M | 25 | 同上 |
| 小宗 | 单种海量，字节数不限 | 标记 3 种，装扩容卡后 10 种 | 同上 |
| FE | 1K、1M、4M（按字节计，1 字节 = 1 Mi FE） | 1 种 | 见 [FE 存储矩阵](flux_storage.md) |
| 魔源 | 1K、16K、64K、1M、4M | 256 | 同上 |
| 乏核废料 | 1K、1M | 1 种 | 8 AE/t |
| 全能 | 1K、1M | 512 | 8 AE/t |
| 复合全能 | 1K、1M | 1024 | 8 AE/t |
| 量子全能 | 1K、1M | 不限类型 | 8 AE/t |
| 无限存储矩阵 | 容量不限 | 16 | 8 AE/t |
| 奇点元件 | 容量不限 | 1 | 1 AE/t |
| 陨石粉元件 | 容量不限 | 1 | 1 AE/t |
| 魔源单元 | 容量不限 | 1 | 1 AE/t |
| 自产矩阵（KubeJS） | 容量不限 | 1 | 1 AE/t |

- 量子 额外容量1M （1572864）。
- 混装一族（全能、复合全能、量子全能）是omni兼容。
- 小宗为ecomega兼容。

详见 [小宗存储矩阵](small_bulk_matrices.md)。
- 奇点元件、陨石粉元件、魔源单元与自产矩阵**什么都不吃**：往它们推东西一律拒收，库存按世界时间自己长。

详见 [L1 奇点元件](singularity_cell.md)。

## 哪些插得进我们这台 L1 驱动器

我们 L1 的等级数是 0（最低档），所以 eco 的 L4/L6/L9 兼容矩阵插进我们这台会被拒收。反过来 eco 能接受 L1 矩阵。

## 用 KubeJS 加自己的矩阵

整合包可以注册新档位、新材质，也可以造**自产矩阵**。见 [KubeJS 支持](kubejs.md)。
