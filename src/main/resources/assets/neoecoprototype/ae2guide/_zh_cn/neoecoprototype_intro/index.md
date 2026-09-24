---
navigation:
  title: Neo ECO Prototype
  position: 200
---

# Neo ECO Prototype

Neo ECO Prototype 把 Neo ECO AE Extension 的后期内容带到 **L1 前期**，不用熬到终局也能用上。
三大系统（存储/计算/合成）的耗电仅为 eco L4 同级的 **1/8**。

先搭建 [L1 存储系统](storage_system.md)：一台 <ItemLink id="neoecoprototype:simplify_storage_controller" /> 带动任意数量的 <ItemLink id="neoecoprototype:simplify_drive" /> 驱动器与 <ItemLink id="neoecoprototype:simplify_energy_cell" /> 能量仓。要自动化处理器，加一台 [L1 处理器装配室](processor_assembler.md)；要大宗囤积少数几种资源，加 [小宗存储矩阵](small_bulk_matrices.md)。

需要更多合成吞吐时，[L1 计算系统](computation_system.md)与[合成系统](crafting_system.md)同样把 eco 的多方块机器带到了 L1。

网络供电与大批量标记则交给两台自有接口：[L1 供能接口](powered_interface.md) 有 18 个标记、被动输出 200 AE/t；[盈能超导接口](superconductive_interface.md) 是它的上位版——同样的布局，单个标记可囤 8192 个物品（或 512,000 mB 流体），输出 4000 AE/t。AE2 每个网格同时只允许一个被动发电源工作，并永远选择功率最高的那台，所以两者不会叠加。

想给网络里的人留个纪念，[玩偶](fumo_doll.md) 能用三件套主机在装配室里合成，也可以用 `/prototypefumo <玩家名>` 取任何人的皮肤。

整合包作者可以用 [KubeJS](kubejs.md) 脚本注册矩阵与配方，无需写 Java。

三合一联合体仍在开发中，暂未包含在本指南里。
