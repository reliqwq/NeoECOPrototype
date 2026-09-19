---
navigation:
  title: L1 计算系统
  icon: neoecoprototype:simplify_computation_system
  position: 50
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_computation_system
  - neoecoprototype:simplify_computation_casing
  - neoecoprototype:simplify_computation_parallel_core
  - neoecoprototype:simplify_computation_threading_core
  - neoecoprototype:simplify_computation_cooling_controller
  - neoecoprototype:simplify_computation_drive
  - neoecoprototype:simplify_computation_interface
  - neoecoprototype:simplify_computation_network_interface
---

# L1 计算系统

L1 计算系统是 eco 计算系统的前期版本：一个为 ME 网络提供并行合成线程的多方块合成 CPU 集群。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 所需组件

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_computation_system" />
  <ItemIcon id="neoecoprototype:simplify_computation_casing" />
  <ItemIcon id="neoecoprototype:simplify_computation_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_threading_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_cooling_controller" />
  <ItemIcon id="neoecoprototype:simplify_computation_drive" />
  <ItemIcon id="neoecoprototype:simplify_computation_interface" />
  <ItemIcon id="neoecoprototype:simplify_computation_network_interface" />
</ItemGrid>


概念与 eco 计算系统一致：基础提供 **1 条合成线程**（eco L4 的 1/4），可用并行核心与线程核心按 eco 同款规则扩展。上图即最小可运行结构。
