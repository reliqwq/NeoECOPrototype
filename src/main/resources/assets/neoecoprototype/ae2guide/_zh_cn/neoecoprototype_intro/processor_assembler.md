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

**L1 处理器装配室**相当于绿色外壳的<ItemLink id="ae2:molecular_assembler" />，专门给还没压印器的存档用：处理器三件套在这里直接用样板做出来。它只跑本模组的处理器装配配方，普通分子装配样板会被拒收， 那类活请交给分子装配室。它配下面的 L1 样板供应器使用。已经有 eco 的大型集成工作站的话，装上 [L1 处理器装配卡](processor_assembly_card.md) 就能在那台机器上跑这一族配方。

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>

装配室与供应器都在**合成台**做：装配室 = 1 台分子装配室 + 4 只特质机壳 + 4 个绿晶晶格；供应器 = 1 个 ME样板供应器 + 4 个石英玻璃 + 4 个绿晶晶格，一次出 2 台。

<Row>
  <RecipeFor id="neoecoprototype:simplify_stonecutting_assembler" />
  <RecipeFor id="neoecoprototype:simplify_pattern_provider" />
</Row>

## 出厂配方

处理器四件套（最后一条要求装 AE2 闪电科技；压印器里的其它 press 配方也会推导成装配配方，见下）：

| 产物 | 原料 |
|------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 运算处理器 | 硅 + 红石 + 赛特斯石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |
| 过载处理器（AE2 闪电科技） | 硅 + 红石 + 过载水晶 |

其余 8 条：

| 产物 | 原料 |
|------|------|
| [L1 供能接口](powered_interface.md) | 2 水晶谐振发电机 + 1 ME接口 + 1 ECO - SA 超导处理器 |
| ECOP - 结构扩容卡 | 内存卡 + 铝锭 + 绿晶晶格 |
| 盈能强化线程核心 | 2 ECO - CM4A 线程核心 + 1 ECO - C1 计算子结构外壳 |
| ECO - CE1R 闪存晶阵 | 2 ECO - CE1 闪存晶阵 + 1 ECO - L4 存储组件 |
| 雪球 / 细雪桶 | 4 雪块 → 4 雪球；3 雪球 + 1 桶 → 细雪桶 |
| reliqwq 的 Fumo 玩偶 | L1 存储主机 + C1 计算主机 + F1 合成主机 |
| Yang120 的 Fumo 玩偶 | eco 的 L4 存储主机 + C4 计算主机 + F4 合成主机 |
| kouooki 的 Fumo 玩偶 | 粉色染料 + 干海带 + 棕色蘑菇 |

硅、金锭、钻石都收标签，任意模组的对应物品都算。

压印器里其余的 press 配方也会现场推导成装配配方——别的模组给压印器加的配方不需要数据文件就能用。两个服务端配置键管这一条：

| 配置键 | 作用 |
|--------|------|
| `derive_processor_recipes_from_inscriber` | 关掉从压印器推导 |
| `disabled_processor_recipes` | 按输出排除某一种处理器 |

## 速度与批量

5 个卡槽只吃<ItemLink id="ae2:speed_card" />：不装卡一份要 100 刻，装满 5 张要 20 刻。

一次推送带几份完整材料就在**一个**周期里做完，产出按份数翻倍而耗时不变，所以想更快只能继续加卡。份数有两条上限，任何一条超出都整张拒收、原料退回网络，日志写明是哪一条卡的：

1. **服务端配置 `processor_assembler_max_runs_per_cycle`**（默认 64，范围 1~192）：一个周期最多跑几份。默认 64 就是一次弹出的一整组（每份产 1 个的处理器），出厂时不把余量往网络里送；调小收紧，填 1 就是一个周期只做一份；调大最多到 192，那就是配方本身的上限——九格摆 576 个物品，三原料每份 3 个。比它大的样板直接拒收，不是"只跑一部分"。
2. **九格工作台**：一批原料要按各自的整组数量摆进这 9 格里。9 格 × 每格 64 个 = 576 个物品，三原料的处理器每份 3 个，正好 192 份；比这更大的一定摆不下。

想在一个周期里多做几份，把配置往上调（最多到 192，再往上配置也收不下）；九格这条是物理上限，调配置也绕不过去。一轮只有一组产出送到机器前面，交不下的那部分直接进网络，不会消失。

## L1 样板供应器

**L1 样板供应器**是<ItemLink id="ae2:pattern_provider" />的 L1 版：样板槽位从 9 个变成 **27 个**，向相邻容器轮转推送原料的方式与原版一致。配方见本页上方。
