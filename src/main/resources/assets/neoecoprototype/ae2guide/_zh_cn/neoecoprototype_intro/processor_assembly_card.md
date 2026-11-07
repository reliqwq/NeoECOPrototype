---
navigation:
  title: L1 处理器装配卡
  icon: neoecoprototype:processor_assembly_workstation_card
  position: 31
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:processor_assembly_workstation_card
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:energized_computation_core
  - neoecoprototype:simplify_green_crystal_matrix
---

# L1 处理器装配卡

**L1 处理器装配卡**是 eco 的<ItemLink id="neoecoae:large_integrated_working_station" />的升级模块。把它插进工作站的模块槽，那台工作站就能跑本模组的处理器装配配方——和 [L1 处理器装配室](processor_assembler.md)完全同一族。

<ItemGrid>
  <ItemIcon id="neoecoprototype:processor_assembly_workstation_card" />
</ItemGrid>

## 插在哪儿

工作站只有**一个**模块槽，在界面的右上角，一次只能装一张卡：装上我们的卡，eco 的其它模块卡就不在位了。工作站自己的配方、以及不需要专用模块的兼容配方照常可用；属于别的模块卡的那一族配方，则要换回那张卡。

这张卡**不装进** L1 处理器装配室：那台机器的五个升级槽只收<ItemLink id="ae2:speed_card" />与能量卡。

## 怎么做

卡片在 **eco 的大型集成工作站里**做——不是合成台，也不是我们的装配室：

| 原料 | 数量 |
|------|------|
| L1 处理器装配室 | 8 |
| 盈能强化计算机核心 | 1 |
| 绿晶晶格 | 32 |

每张耗能 10000 AE。工作站的配方图贴不到这一页上，所以原料写在表里。

这张卡之所以按工作站的配方形状定价：我们自己的处理器装配配方类型既没有逐种原料数量，也没有耗能字段，写在那边就只剩每种一份，八台装配室与 32 块晶格会塌成各一个。

## 样板怎么编码

一次样板执行 = 我们的配方跑 **64 次**，而样板只有在产出恰好是这一整批的整数倍时才会被匹配。所以按 64 次编码：

| 配方 | 64 次产出 | 每批耗能 |
|------|-----------|----------|
| 处理器（每次 1 个） | 64 个处理器 | 64 AE |
| 雪球（每次 4 个） | 256 个雪球 | 64 AE |

原料要按同一个倍数写：一条 64 倍逻辑处理器样板 = 64 硅 + 64 红石 + 64 金锭，换 64 个逻辑处理器。

- 比一整批小的样板（比如 1 倍、32 倍）不会被工作站接单，材料也不会从网络里取走。
- 样板翻倍，材料与耗能一起翻倍，所以单件价格不变：一个处理器仍是 1 AE。
- 批量大小不会让单批变慢，速度由工作站自己的超频决定。
- [L1 处理器装配室](processor_assembler.md)不受影响——它照旧接 1 倍样板，九格批量的规矩也照旧。
