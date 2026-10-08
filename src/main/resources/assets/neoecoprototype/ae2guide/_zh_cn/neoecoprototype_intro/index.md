---
navigation:
  title: Neo ECO Prototype
  position: 200
---

# Neo ECO Prototype

Neo ECO Prototype 把 Neo ECO AE Extension 的后期内容带到 **L1 前期**，不用熬到终局也能用上。
三大系统（存储/计算/合成）的耗电仅为 eco L4 同级的 **1/8**。

先搭建 [L1 存储系统](storage_system.md)：一台 <ItemLink id="neoecoprototype:simplify_storage_controller" /> 带动任意数量的 <ItemLink id="neoecoprototype:simplify_drive" /> 驱动器与 <ItemLink id="neoecoprototype:simplify_energy_cell" /> 能量仓。要自动化处理器，加一台 [L1 处理器装配室](processor_assembler.md)；要大宗囤积少数几种资源，加 [小宗存储矩阵](small_bulk_matrices.md)。另外两种"存储"各有一页：[FE存储矩阵](flux_storage.md) 把 FE 留在网格里（需要 AppliedFlux），[L1 奇点元件](singularity_cell.md) 什么都不吃、自己长奇点。

需要更多合成吞吐时，[L1 计算系统](computation_system.md)与[合成系统](crafting_system.md)同样把 eco 的多方块机器带到了 L1。

前期网络供电与大批量io标记则交给两台自有接口：[L1 供能接口](powered_interface.md) 有 18 个标记、被动供电输出 200 AE/t；[盈能超导接口](superconductive_interface.md) 是它的上位版——同样的布局，单个标记可囤 8192 个物品（或 512,000 mB 流体），被动供电4000 AE/t。AE2 每个网格同时只允许一个被动发电源工作，并永远选择功率最高的那台，所以两者不会叠加。

整合包作者可以用 [KubeJS](kubejs.md) 脚本注册矩阵与配方，无需写 Java。

三合一联合体仍在早期开发中，暂未包含在本指南里。
