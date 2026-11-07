---
navigation:
  title: L1 处理器装配卡
  icon: neoecoprototype:processor_assembly_workstation_card
  position: 31
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:processor_assembly_workstation_card
---

# L1 处理器装配卡

**L1 处理器装配卡**是大型集成工作站的升级模块。装上它，工作站就能像 [L1 处理器装配室](processor_assembler.md) 一样加工本模组的处理器装配配方：样板放进通讯接口，原料和产物都经由 ME 网络流转。

<ItemGrid>
  <ItemIcon id="neoecoprototype:processor_assembly_workstation_card" />
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:energized_computation_core" />
  <ItemIcon id="neoecoprototype:simplify_green_crystal_matrix" />
</ItemGrid>

## 安装

大型集成工作站主机界面右上角有一个**升级模块**槽，一次只能装一张。装上这张卡，eco 的其它模块卡就不在位——模块槽只有一个，一次只能选一条路线。工作站自身的配方和没有专用模块的兼容配方始终可用。

这张卡**不装进** L1 处理器装配室：那台机器的五个升级槽只收 <ItemLink id="ae2:speed_card" /> 与能量卡，处理器配方它本来就带。

装上后，控制器换成我们的外观，这一族配方出现在 JEI / EMI 的“大型集成工作站”分类里。

## 合成

和其它升级模块一样，这张卡在**大型集成工作站**中合成：

<RecipeFor id="neoecoprototype:processor_assembly_workstation_card" />

每张耗能 10000 AE。

## 样板怎么编码

这一族按 **64 份一批**下单：一次样板执行，我们的配方跑 64 次。样板要写整批的原料和产物。

| 配方 | 一批产出 | 一批耗能 |
|------|---------:|---------:|
| 处理器（每次 1 个） | 64 个 | 64 AE |
| 雪球（每次 4 个） | 256 个 | 64 AE |

原料按同一个倍数写。例如逻辑处理器的一批样板是 64 硅 + 64 红石 + 64 金锭，产出 64 个逻辑处理器。

- 不足一批的样板（1 倍、32 倍）不会被接收，材料也不会从网络里取走。
- 样板成倍放大时，原料与耗能一起放大，单件价格不变：一个处理器仍是 1 AE。
- 一批多大都按 1 tick 加工，同时做几份由工作站的超频与并行决定。
- [L1 处理器装配室](processor_assembler.md)不受影响：它照旧接 1 倍样板，九格批量的规矩也照旧。

## 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| 找不到我们的配方 | 在 JEI / EMI 的“大型集成工作站”分类里找；要让工作站接单，卡还得在模块槽里 |
| 样板放进去不接单 | 检查数量是不是 64 的整数倍，原料有没有按同一倍数写 |
| 想换回别的模组的模块 | 模块槽一次只装一张，先取出这张卡 |
