# L1 奇点元件的速率与上限（现量口径）

这一页只放三个东西：单位怎么换算、按默认档这些数字等于多久、以及"上限"这个词现在还剩多少意义。全部数值来自 2026-10-07 现量的配方需求与配置默认值；改任何一条时**这页与代码要一起改**。

## 单位

`1 tick = 1/20 秒`。默认间隔 `ticks_per_batch = 600` ⇒ **30 秒一批**，不是 600 秒。这里出过一次 20 倍的口误（把 tick 当秒，把速率报成 0.2/秒，真值是 4/秒），所以每次引用速率都重新除一次。

## 默认档

| 量 | 值 |
| --- | --- |
| 间隔 | 600 tick = 30 秒 |
| 每批 | 120 个奇点 |
| 换算速率 | 4 个/秒 = 240/分 = 14,400/小时 |
| 库存粒度 | 一次跳 120，不是慢慢滴 |

## 最快档（配置夹到的边界）

| 量 | 值 |
| --- | --- |
| 间隔下限 | 60 tick = 3 秒 |
| 每批上限 | 262,144（256 Ki） |
| 换算速率 | ≈ 87,381 个/秒 |

## 这些数字对得上什么（需求锚点）

`ae2:singularity` 在这个包里的真实去处，全部按配方文件现数（shaped 的要按 pattern 里字母出现次数算，不是按 key 表条目数）：

| 要花奇点的地方 | 数量 | 出处 |
| --- | --- | --- |
| 三台 L1 接口（存储/计算/合成） | 各 2 = **6** | `data/neoecoprototype/recipe/simplify_{storage,computation,crafting}_interface.json`，pattern 里 `CDC` |
| eco 一只 MEGA 4G 矩阵（十种媒体各一张） | **128** | eco jar `data/neoecoae/recipe/eco_mega_*_4g.json` |
| eco 的无限存储组件 | **128** | eco jar `data/neoecoae/recipe/eco_infinite_cell_component.json` |
| eco 的无限存储解锁：插 64 个组件 + 12 台 L9 | **8,192** | 上一条 × 64（组件数来自 eco 的 tooltip `infinite_component.unlock`） |

按默认档 4 个/秒换算：**我们的三台接口第一批就到（30 秒内）**；一只 MEGA 盘 / 一个组件 **32 秒**；eco 那口 8,192 的终局墙 **约 34 分钟**。最快档下这些全部降到亚秒级 —— 那一档是给整合包作者的，不是给生存玩家的节奏。

## "库存上限"现在只剩算术意义

2026-10-07 决定不配置上限（`unbounded_cap` 那个键被删）。留着一条 2.1G 的墙本来会怎样：默认档摸到它要 **约 17 年**，最快档也要 **约 2.4 个月** —— 也就是说"到上限后停止"在任何正常玩法里都不会被观察到。于是它被撤掉，取而代之的是：**累计产量做 long 饱和**（`batches > Long.MAX_VALUE / 每批量` 时钉在 `Long.MAX_VALUE`，不翻负），这条有断言。

## 副作用：库存是算出来的，所以配置会追溯生效

栈上只存两个数（起始世界 tick、已取走量），每次被问到才算。因此改 `amount_per_batch` 或 `ticks_per_batch` 会**重读历史**：调大立刻变多，调小可能把存下的清零。这不是 bug，是 C1（推导而非累加）这一形状的直接后果 —— 之所以只能推导：AE2 19.2.17 的 `StorageCell`、eco 的 `IECOStorageCell` 都没有 tick 入口，`IManagedGridNode` 在 19.2.17 也没有 `setMainAction`，而我们自己的 L1 驱动器一次 tick 都没有（详见 `ae2-extension-playbook.md` 同名小节）。

## 钉住这些数的断言

- `singularity_cell_math`（批次边界、抽走之后靠时钟补、**不截断**、时钟推到 long 尽头不翻负、两个边界常数 60 与 262,144）。
- `singularity_cell_yield`（走 eco 的单元 API：收不进任何东西、只报奇点这一行、付完 `drawn` 真写回栈、付完不再 advertise、`canFitInsideCell()` 必须为 false、悬停文本里那行数字对得上）。
