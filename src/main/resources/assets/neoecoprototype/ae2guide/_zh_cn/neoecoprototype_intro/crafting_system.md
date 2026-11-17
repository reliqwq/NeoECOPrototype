---
navigation:
  title: L1 合成系统
  icon: neoecoprototype:simplify_crafting_system
  position: 60
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_crafting_system
  - neoecoprototype:simplify_crafting_casing
  - neoecoprototype:simplify_crafting_parallel_core
  - neoecoprototype:simplify_crafting_pattern_bus
  - neoecoprototype:simplify_crafting_worker
  - neoecoprototype:simplify_crafting_vent
  - neoecoprototype:simplify_crafting_interface
  - neoecoprototype:simplify_crafting_network_interface
  - neoecoprototype:simplify_fluid_input_hatch
  - neoecoprototype:simplify_fluid_output_hatch
---

# L1 合成系统

**L1 合成系统**是 eco 的 <ItemLink id="neoecoae:crafting_system_l4" /> 的 L1 版本：容纳并处理合成样板。耗电是 eco L4 同级的 **1/8**。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 项目 | 数值 |
|------|------|
| 满配 |  **11 个工作核心 + 22 颗并行核心**，线程默认352、超频 1056 |
| 每次批量 | 水冷却剂 4 刻执行一批 ⇒ **1056 - 352 件/批**，高等级冷却剂缩短每批所需时间 |
| 与 eco c4相比 | 约为1/2 |

## 部件

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_crafting_system" />
  <ItemIcon id="neoecoprototype:simplify_crafting_casing" />
  <ItemIcon id="neoecoprototype:simplify_crafting_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_crafting_pattern_bus" />
  <ItemIcon id="neoecoprototype:simplify_crafting_worker" />
  <ItemIcon id="neoecoprototype:simplify_crafting_vent" />
  <ItemIcon id="neoecoprototype:simplify_crafting_interface" />
  <ItemIcon id="neoecoprototype:simplify_crafting_network_interface" />
  <ItemIcon id="neoecoprototype:simplify_fluid_input_hatch" />
  <ItemIcon id="neoecoprototype:simplify_fluid_output_hatch" />
</ItemGrid>

| 部件 | 空闲耗电 |
|------|----------|
| 合成主机 | 4 AE/t |
| F1 工作核心 | 4 AE/t |
| 并行核心 | 4 AE/t |
| 智能样板总线 | 2 AE/t |
| 合成通风口 | 4 AE/t |
| 合成流体输入仓 | 2 AE/t |
| 合成流体输出仓 | 2 AE/t |
| 合成接口 | 2 AE/t |
| 合成网络接口 | 2 AE/t |
| 合成外壳 | 2 AE/t |

## 搭建步骤

与eco一致。

## 超频与冷却

与eco一致，水作为冷却剂时默认满配并行数为176/tick
