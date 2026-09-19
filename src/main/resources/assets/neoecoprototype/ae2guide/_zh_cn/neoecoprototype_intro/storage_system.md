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

L1 存储系统是一个紧凑多方块：一台 <ItemLink id="neoecoprototype:simplify_storage_controller" /> 主机，加上任意数量的 <ItemLink id="neoecoprototype:simplify_drive" /> 驱动器与 <ItemLink id="neoecoprototype:simplify_energy_cell" /> 能量仓。驱动器装载存储矩阵并挂载进 ME 网络。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_storage_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 先把结构搭起来

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_storage_controller" />
  <ItemIcon id="neoecoprototype:simplify_drive" />
  <ItemIcon id="neoecoprototype:simplify_energy_cell" />
  <ItemIcon id="neoecoprototype:simplify_storage_interface" />
  <ItemIcon id="neoecoprototype:simplify_storage_network_interface" />
  <ItemIcon id="neoecoprototype:simplify_storage_vent" />
  <ItemIcon id="neoecoprototype:simplify_storage_casing" />
</ItemGrid>

存储主机决定整套结构的等级与操作面；存储矩阵驱动器用于放置存储矩阵；能量仓提供能量缓冲；存储接口负责盘内资源的对外供给；存储网络接口负责接入 ME 网络；存储通风口负责热量管理；存储外壳补齐结构框架。

## 搭建步骤

1. 放置存储主机，正面朝外（这一面是操作面）。
2. 放置存储网络接口，接入 ME 网络。
3. 驱动器沿主机侧面水平排列。
4. 存储接口与通风口按结构位置安放。
5. 用存储外壳补齐整个框架。

逐块摆放容易出错，直接用主机界面里的**自动搭建面板**：先预览结构，再一键补齐。



## 选择矩阵

| 要存什么 | 矩阵家族 | 需要的模组 |
|----------|----------|------------|
| 物品（1K - 4M） | 物品存储矩阵 | - |
| 流体（1K - 4M） | 流体存储矩阵 | - |
| Mekanism 化学品（1K - 4M） | 化学品存储矩阵 | Mekanism + 应用能源修炼 |
| 多种 AE 资源、类型无限 | 全能 / 量子矩阵 | AE2 Omni Cells |
| 指定物品无限囤积 | 无限混凝土矩阵 / KubeJS 无限矩阵 | - |

所有矩阵都可分区：放进 AE2 元件工作台即可标记接受的资源。

## 小宗矩阵

要大宗囤少数几种资源，看[小宗存储矩阵](small_bulk_matrices.md)——3 种类型，单类型容量近乎无限。

## 外部元件

L1 驱动器默认挂载 L1 原生元件与小宗盘。更高等级的 eco 元件（包括 eco 自家的 MegaCells 长桶元件）可以插入，但不会挂载，除非服务器管理员在服务端配置里将其加白（`additional_storage_cells`）。
