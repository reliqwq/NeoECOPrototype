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

**L1 合成系统**是 eco 的 <ItemLink id="neoecoae:crafting_system_l4" /> 的 L1 版本：把合成样板装进总线，工作核心就并行执行网络交来的合成任务，耗电是 eco L4 同级的 **1/8**。它管执行，计算系统管能给多少线程——**两边的数字各算各的，谁也不给对面加数**。上图即最小可运行结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_crafting_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

| 项目 | 数值 |
|------|------|
| 并行（每颗并行核心） | **16** |
| 开超频后 | **48** —— eco 是把档位并行与超频奖励**相加**（16 + 32），不是拿后者替换前者，所以别按"开了就变 32"去算 |
| 满配那台有多大 | 默认最长的那台是 **11 个工作核心 + 22 颗并行核心**，线程池开关关 352、开关开 1056 |
| 每刻能推多少件 | 每刻的量 = 一批的量 ÷ 一批要几刻。开关开时一批是 11 × 64 = **704** 件：不灌冷却 2 刻跑完 ⇒ **352 件/刻**，灌纯水 4 刻 ⇒ **176 件/刻**，上钠或冰晶又是 2 刻 ⇒ 352 件/刻。开关关掉时，所有档位都是 10 刻一批 |
| 与 eco 各档相比 | eco 的 L4 每颗并行核心给 **24**，L6 给 72，L9 给 256 —— L1 这一台是低的那一档，而且低主要低在结构容得下的核心数少 |
| 与计算侧的关系 | 各算各的：这一台每核心给 16 / 48，计算系统给线程数与它的加速器数 |

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

1. 放下主机，使其朝外，用外壳搭出周围框架。
2. 在主机**左后方**放合成网络接口（玩家能打开的那一块），它上方放流体输入仓、下方放流体输出仓。
3. 从主机**右侧**的外壳再往右开始，水平排列 F1 工作核心。
4. 在工作核心的上下各放一排并行核心，正后方放合成通风口。
5. 在通风口的上下各放一排智能样板总线。
6. 用外壳补齐剩余结构。

加长时，工作核心、并行核心、样板总线与通风口要一起加。不想逐块摆就用主机界面里的**自动搭建面板**：它给出的就是这套结构的形状权威，上面六步就是它排出来的顺序。

## 超频与冷却

超频与主动冷却的开关、冷却液等级要求都沿用 eco 那一套，规则看 eco 自己的合成系统页。这一档的数是每颗并行核心 **16**（不开超频）与 **48**（开超频）——eco 把两者相加（16 + 32）而不是互相替换，别按"开了就变 32"去算。还有一点在怪自己机器之前该先确认：**开关挂在网络上，不属于某一台主机**——同网的主机全部跟随 leader 那一台设置，而且台上有活的时候 eco 拒绝切换。一批要跑几刻同样是 eco 自己算的，不是我们填的数：它看这台机器的线程池相对批量还剩多少空余，每 5% 给一档；开了主动冷却之后，冷却液自己的档位再给这个结果封顶——这就是开关开的 L1 灌纯水会落在 4 刻一批、不灌冷却是 2 刻的原因。冷却液位在我们这里还有一层用处：合成主机的**机魂**读它来决定空闲耗电省多少（详见机魂那一节），所以冷却液满不满会改变待机数字，但不改变能不能超频——后者归 eco 的规则管。
