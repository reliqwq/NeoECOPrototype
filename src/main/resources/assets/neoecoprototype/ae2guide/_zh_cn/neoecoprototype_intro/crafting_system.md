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
---

# L1 合成系统

L1 合成系统是 eco 合成系统的前期版本：一个并行执行样板的多方块，提升 ME 网络的样板吞吐。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

概念与 eco 合成系统一致，完整机制见 eco 的指南书。上图即最小可运行结构。

校对待办：L1 变体的吞吐与工人行为请在正式广泛发布前确认。
