---
navigation:
  title: Neo ECO Prototype
  position: 200
---

# Neo ECO Prototype

Neo ECO Prototype 把 Neo ECO AE Extension 的后期内容带到 **L1 前期**，不用熬到终局也能用上。

先搭建 [L1 存储系统](storage_system.md)：一台 <ItemLink id="neoecoprototype:simplify_storage_controller" /> 带动任意数量的 <ItemLink id="neoecoprototype:simplify_drive" /> 驱动器与 <ItemLink id="neoecoprototype:simplify_energy_cell" /> 能量仓。要自动化处理器，加一台 [L1 处理器装配室](processor_assembler.md)；要大宗囤积少数几种资源，加 [小宗存储矩阵](small_bulk_matrices.md)。

需要更多合成吞吐时，[L1 计算系统](computation_system.md)与[合成系统](crafting_system.md)同样把 eco 的多方块机器带到了 L1。

整合包作者可以用 [KubeJS](kubejs.md) 脚本注册矩阵与配方，无需写 Java。

三合一联合体仍在开发中，暂未包含在本指南里。
