---
navigation:
  title: L1 存储系统
  icon: neoecoprototype:simplify_storage_controller
  position: 10
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_storage_controller
  - neoecoprototype:simplify_drive
  - neoecoprototype:simplify_energy_cell
  - neoecoprototype:simplify_item_storage_cell_1k
  - neoecoprototype:simplify_item_storage_cell_16k
  - neoecoprototype:simplify_item_storage_cell_64k
  - neoecoprototype:simplify_item_storage_cell_1m
  - neoecoprototype:simplify_item_storage_cell_4m
  - neoecoprototype:simplify_fluid_storage_cell_1k
  - neoecoprototype:simplify_fluid_storage_cell_16k
  - neoecoprototype:simplify_fluid_storage_cell_64k
  - neoecoprototype:simplify_fluid_storage_cell_1m
  - neoecoprototype:simplify_fluid_storage_cell_4m
  - neoecoprototype:simplify_chemical_storage_cell_1k
  - neoecoprototype:simplify_chemical_storage_cell_16k
  - neoecoprototype:simplify_chemical_storage_cell_1m
  - neoecoprototype:simplify_chemical_storage_cell_4m
---

# L1 存储系统
L1 存储系统是一套可扩展的多方块——把存储矩阵挂进 ME 网络——待机总耗电是 eco L4 同级结构的 **1/8**。

兼容存储矩阵集中在 [支持的存储矩阵](matrix_types.md) 一页对照。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 部件

| 部件 | 空闲耗电 |
|------|----------|
| 存储主机 | 32.25 AE/t |
| 存储矩阵驱动器 | 每台 32 AE/t |
| 能量仓 | 0 |
| 存储接口 | 2 AE/t |
| 存储网络接口 | 2 AE/t |
| 存储通风口 | 4 AE/t |
| 存储外壳 | 2 AE/t |


## 搭建步骤

与eco一致，不再赘述。

## 通讯接口

默认自动搭建是接口，通讯接口请自行升级。


## 选哪种矩阵

| 要存什么 | 矩阵系列 | 需要的模组 |
|----------|----------|------------|
| 物品、流体、化学品（1K - 4M） | 物品 / 流体 / 化学品存储矩阵 | 化学品另需 Mekanism 与 Applied Mekanistics |
| 单种近乎无限 | [小宗存储矩阵](small_bulk_matrices.md) |
| omni | 全能 / 量子 / 复合全能矩阵（1K / 1M） | AE2 Omni Cells |
| 指定物品无限囤积 | 无限矩阵（`concrete`）/ KubeJS 无限矩阵 |
| FE 能量 | [FE存储矩阵](flux_storage.md) | AppliedFlux |
| Mek的乏核 | 乏核废料存储矩阵（1K / 1M） |

矩阵在 AE2 的元件工作台里分区。奇点矩阵不能分区也不能存储。

## eco存储矩阵

eco 更高等级的元件插得进去但不挂载，等级太低，不支持。除非服务端配置可修改这个规则（`additional_storage_cells`）。