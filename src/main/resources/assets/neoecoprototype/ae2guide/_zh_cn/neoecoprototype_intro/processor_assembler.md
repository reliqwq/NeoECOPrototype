---
navigation:
  title: L1 处理器装配室
  icon: neoecoprototype:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:simplify_pattern_provider
---

# L1 处理器装配室

**L1 处理器装配室**相当于绿色外壳的<ItemLink id="ae2:molecular_assembler" />，只接受处理样板，每周期执行一份。它只跑下方列出的本模组处理器装配配方；压印器推导配方属于 **L1-EC9**，普通分子装配样板会被拒绝。如果已有 eco 的大型集成工作站，可装 [L1装配室集成工作站升级卡](processor_assembly_card.md) 在那里运行同一配方族。

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>

装配室与供应器都是**工作台合成配方**：装配室 = 1 台分子装配室 + 4 只特质机壳 + 4 个绿晶晶格；供应器 = 1 个 ME样板供应器 + 4 个石英玻璃 + 4 个绿晶晶格，一次出 2 台。

<Row>
  <RecipeFor id="neoecoprototype:simplify_stonecutting_assembler" />
  <RecipeFor id="neoecoprototype:simplify_pattern_provider" />
</Row>

## 出厂配方

五种处理器（过载处理器有闪电科技联动，见下）：

| 产物 | 原料 |
|------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 运算处理器 | 硅 + 红石 + 赛特斯石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |
| ECO - SA 超导处理器 | 盈能超导锭 + 硅 + 水晶晶格 |
| 过载处理器（AE2 闪电科技） | 硅 + 红石 + 过载水晶 |

其余 7 条：

| 产物 | 原料 |
|------|------|
| [L1 供能接口](powered_interface.md) | 2 水晶谐振发电机 + 1 ME接口 + 1 ECO - SA 超导处理器 |
| 盈能强化线程核心 | 2 ECO - CM4A 线程核心 + 1 ECO - C1 计算子结构外壳 |
| ECO - CE1R 闪存晶阵 | 2 ECO - CE1 闪存晶阵 + 1 ECO - L4 存储组件 |
| 雪球 / 细雪桶 | 4 雪块 → 4 雪球；3 雪球 + 1 桶 → 细雪桶 |
| reliqwq 的 Fumo 玩偶 | L1 存储主机 + C1 计算主机 + F1 合成主机 |
| Yang120 的 Fumo 玩偶 | eco 的 L4 存储主机 + C4 计算主机 + F4 合成主机 |
| kouooki 的 Fumo 玩偶 | 粉色染料 + 干海带 + 棕色蘑菇 |

硅、金锭、钻石接受标签。

压印器里的其他配方也会实时推导，无需另写数据文件；这些推导配方只在 **L1-EC9** 上运行。可在对应服务端配置文件配置：

| 配置键 | 作用 |
|--------|------|
| `derive_processor_recipes_from_inscriber` | 关掉从压印器推导 |
| `disabled_processor_recipes` | 按输出排除某一种处理器 |

## 速度与批量

5 个卡槽只吃<ItemLink id="ae2:speed_card" />：不装卡一份要 100 刻，装满 5 张要 20 刻。

**本机每周期固定执行一份**：要求一次做多份的样板会整张被拒收，原料退回网络，日志会指出是哪一条。想批量生产请使用下方的 L1-EC9；想缩短单份耗时只能继续加卡。

## L1-EC9 处理器装配室

**L1-EC9** 是装配室的进阶型号，用 1 台 L1 处理器装配室 + 2 个<ItemLink id="neoecoprototype:energized_computation_cell_4m" /> + 4 个绿晶晶格在工作台合成：

<RecipeFor id="neoecoprototype:ec9_processor_assembler" />

它运行同一配方族，但**按批量执行**：64x 样板会在一个周期内处理 64 份，上限由服务端配置 `processor_assembler_max_runs_per_cycle` 控制（默认 64，范围 1~192）。它也是唯一运行**压印器推导配方**的机器，其他 mod 加进 AE2 压印器的配方无需数据文件即可使用。

**ECOP - 结构扩容卡**也由它制作：内存卡 + 铝锭 + 绿晶晶格。L1 处理器装配室和 L1 装配室集成工作站升级卡都会拒绝这条配方。其余卡槽、网络连接和流体入网方式与 L1 相同；每轮一组产出送到机器前面，剩余部分进入网络，不会消失。

## L1 样板供应器

**L1 样板供应器**是<ItemLink id="ae2:pattern_provider" />的 L1 版：样板槽位从 9 个变成 **27 个**，向相邻容器轮转推送原料的方式与原版一致。配方见本页上方。
