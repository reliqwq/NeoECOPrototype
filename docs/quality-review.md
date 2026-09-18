# 项目质量评审 —— Neo ECO Prototype 1.2.0

- 评审时间：2026-09-18
- 评审对象：commit `4340387`（Release NeoECOPrototype 1.2.0）
- 评审方式：静态扫描 + 代码通读 + 资源可达性分析，**未运行游戏**
- 配套文档：[`asset-index-audit.md`](asset-index-audit.md)（资源索引审计明细）
- 评审边界：**不评价玩法手感、数值平衡、多方块成型体验**——这些需要实际游玩或玩家反馈

---

## 1. 总评

**综合 6.2 / 10**

一句话：**骨架比外表好得多。内核是个 7 分的工程，被 4 分的资产治理和 3 分的自动化拖住了，旗舰功能还挂着"暂未实现"。**

这个项目最值得肯定的一点是：**代码在主动收敛，而资产在被动发散**。`MatrixMaterials` 把九个家族的外观压成一张表、全仓库只剩 6 处硬编码资源引用，这是正确的方向；但资产树里 47% 的贴图已经不可达，且每加一个家族就多一批。这两股力量正在把项目往两个方向拉。

---

## 2. 评分卡

| 维度 | 分数 | 一句话理由 |
|---|:--:|---|
| 工程卫生 | **8.5** | 版本一致、mixin 无缺失、`build/` `run/` 未入库 |
| 代码架构 | 7.5 | `MatrixMaterials` 收敛得当；集成层有死代码与条件加载隐患 |
| 数据层 | 7.0 | 82 配方零孤儿、lang 中英对齐；2 处 BOM、4 个方块缺 loot_table |
| 文档 | 6.0 | 量大但存在漂移，README 宣传未使用的 API |
| 功能完成度 | 5.5 | L1 存储家族扎实；旗舰 Trinity 仍 experimental 且游戏内标"暂未实现" |
| 资产治理 | 4.0 | 116 张贴图不可达、19 张重复、产物与手绘混放、命名双轨 |
| 测试与自动化 | 3.0 | 4 个测试全在逻辑层，资源层零覆盖；审计脚本结论不可信 |

---

## 3. 分项详评

### 3.1 工程卫生 8.5

- 三处版本号一致：`build.gradle` / `neoforge.mods.toml` / `CHANGELOG` 均为 `1.2.0`；
- `neoecoprototype.mixins.json` 声明的 14 个 mixin **全部有对应源文件，无缺失、无未登记**；
- `build/`、`run/` 均未纳入版本控制，仓库只跟踪 1 个 jar（gradle wrapper）。

扣分点：无。这是全项目最让人放心的部分。

### 3.2 代码架构 7.5

做得对的地方：

- **`api/MatrixMaterials` 是唯一的外观映射表**，九个家族的物品栏模型与驱动器内模型都在这里声明；
- **Java 侧资源引用只剩 6 处硬编码**，其余全部走常量 / 枚举，重构成本极低；
- 集成层按可选依赖分包（`kubejs` / `mekanism` / `omni` / `beyonddimensions`），边界清楚。

扣分点：

- **死代码**：`ChemicalMatrixBuilder` 从未注册进 `KJSPlugin`，脚本根本用不到；
  `StorageMatrixRegistration` 被 `docs/kubejs.md` 和 README 当作"新矩阵注册 API"宣传，但**代码里零调用**；
- **条件加载隐患**：`NeoECOPrototypeClient` 无条件读取 `InfiniteMatrixBuilder.DEFAULT_DRIVE_MODEL`，
  而该类继承自 KubeJS 的 `ItemBuilder`。没装 KubeJS 的环境理论上会 `NoClassDefFoundError`，
  与文档"KubeJS 可选、不装也正常"的承诺冲突（未在无 KJS 环境实测，属静态分析结论）。

### 3.3 数据层 7.0

- 82 个配方，**孤儿配方 0**（产物指向本模组不存在物品的情况没有）；
- `en_us` / `zh_cn` 各 100 条且 key 完全对齐；方块译名 31/31 全覆盖；
- 待修：`recipe/simplify_universal_storage_cell_1k.json`、`_1m.json` **带 UTF-8 BOM**；
- 待确认：4 个方块没有 loot_table —— `simplify_computation_network_interface`、
  `simplify_crafting_network_interface`、`simplify_storage_network_interface`、
  `simplify_green_aluminum_casing`（破坏不掉东西，需确认是有意还是遗漏）；
- lang 真问题 1 处：`simplify_default_storage_cell` 是实际物品却无译名。

### 3.4 文档 6.0

文档量很大：`README`、`DESIGN.md`、`AGENT.md`、`CHANGELOG.md`、`PROGRESS_SUMMARY.md`、
两份 dev log、`docs/` 三份、以及 `knowledge/` 与 `_repro/` 的过程记录。

问题在于**量与可信度不成正比**：

- `docs/kubejs.md` 的"固定家族材质"表格与 `MatrixMaterials` **四项全不一致**
  （小宗 / 猪咪 / 全能 / 量子）——只要映射存在第二份，它就一定过期；
- README 宣传 `StorageMatrixRegistration` API，但无人调用；
- 文档没有生成机制，全靠手同步。

建议：**凡是能从代码推导的信息，不要抄进 markdown**。需要时指向代码，或由脚本生成。

### 3.5 功能完成度 5.5

已经能用的部分（这部分质量不低）：

- L1 存储家族：物品 / 流体 / 化学品 / 小宗 / 猪咪 / 全能 / 量子 / 无限混凝土；
- L1 驱动器与挂载白名单机制、小宗 3→10 类型的升级链；
- 多方块结构、JEI 建筑预览、专属能量单元与接口类方块。

拉低分数的部分：

- **旗舰卖点 Trinity 在 README 里自己标注 experimental**，发布构建中从 JEI 隐藏；
- 三合一主机与存储 / 计算 / 合成三大模块的物品提示**挂着红字"暂未实现"**；
- 递归材料规划、替代材料、CPU 选择、执行全部委托给 AE2 / eco
  （取舍本身合理，但意味着模组的独特价值目前主要在"壳"上）。

### 3.6 资产治理 4.0

明细见 [`asset-index-audit.md`](asset-index-audit.md)，核心数字：

| 指标 | 数值 |
|---|---|
| 贴图总数 / 不可达 | 244 / **116** |
| 模型总数 / 不可达 | 202 / **44** |
| 字节完全重复的贴图 | **19** |
| `*_recolor` 生成产物占比 | 191 / 244（78%） |

根因不是"懒"，而是**没有分层与没有反馈**：

- 上游 eco 的 6 级 / 9 级整档内容被复制进来，而项目只做 4 级（已确认不做 L2 及以上）；
- 生成产物与手写资产混在同一棵树，只靠 `_recolor` 后缀区分，手改会被生成器覆盖；
- 命名双轨：`simplify_item_storage_cell_1k`（种类在前）↔ `storage_cell_l1_item`（等级在前）；
- 审计脚本只扫 model JSON 的 `textures` 字段，报出的结论混着大量假阳性 → 没人敢信 → 乱继续累积。

### 3.7 测试与自动化 3.0

现有 4 个测试文件，全部在逻辑层：

```
api/MatrixMaterialsTest.java
api/SimplifyPowerProfileTest.java
integration/ae2/SimplifyGridFacadeTest.java
multiblock/trinity/TrinityTaskRuntimeTest.java
```

**资源层、配方、lang 零覆盖**。这意味着今晚审计出来的问题，三个月后会原样再长一遍。

---

## 4. 三个真正的风险

### R1（高）旗舰功能未完工却已对外发布

1.2.0 是一个正式版本号，但 Trinity 仍 experimental、游戏内显式标注"暂未实现"。
玩家的第一印象一旦形成很难挽回。**要么把 Trinity 的完成度补上，要么在发布渠道上明确标注为预览版。**

### R2（高）资产与代码朝相反方向演进

代码在收敛（`MatrixMaterials`），资产在发散（每加一个家族多一批贴图、每加一个维度组合数翻倍）。
按当前趋势，1.3.0 会比 1.2.0 更乱。**在资产治理完成前，新增家族 / 容量档应视为给混乱加杠杆。**

### R3（中）零收益的 KubeJS 集成

已确认没有 KubeJS 的真实需求，却维护着一整套 builder、文档和示例，
还带着一个"没装 KubeJS 可能崩溃"的隐患。**零收益、负风险。**

---

## 5. 改进路线（按性价比排序）

| # | 动作 | 影响维度 | 预估工作量 |
|---|---|---|---|
| 1 | 把可达性分析做成测试 / CI 检查 | 测试 3.0 → 6.5 | 1~2 天 |
| 2 | 摘掉或修好 KubeJS 集成（补 `ModList` 判断或直接删） | 风险归零，架构 7.5 → 8 | 半天 |
| 3 | 清理 l6/l9 模型 42 个 + `_b`/`_c` 贴图 58 张 | 资产 4.0 → 6.5 | 半小时 + 一次跑游戏验证 |
| 4 | 修 BOM、补 loot_table、补 `simplify_default_storage_cell` 译名 | 数据 7.0 → 8.0 | 半小时 |
| 5 | 删除 `docs/kubejs.md` 中漂移的材质表，改为指向 `MatrixMaterials` | 文档 6.0 → 7.0 | 十几分钟 |
| 6 | 产物分层（`*_recolor` 显式标记或移出 `assets/`） | 资产长期可维护 | 需设计决策 |

每次清理后的验证动作：

1. 重跑审计，对比数字；
2. 启动客户端，检查 `run/logs/latest.log` 有无 `Failed to load texture` / `Unable to load model`；
3. 进世界把存储 / 计算 / 合成三系控制器各放一次，确认外观与成型态正常；
4. `/give` 一遍 `simplify_*_storage_cell_*`，确认物品栏与驱动器内模型都在。

---

## 6. 不该做的事

- **不新增对外扩展点**：KubeJS builder、新容量档、新家族。当前无真实需求，而每加一个维度，
  外观与后端的组合数翻倍；
- **不重写 Java 侧索引**：`MatrixMaterials` 是对的，别动；
- **不在资产治理完成前扩大家族矩阵**：先把 116 张死贴图清掉，再谈新内容。

---

## 附录：本次评审的量化指标

| 类别 | 数值 |
|---|---|
| Java 文件 | 125 |
| Java 硬编码资源引用 | 6 处 |
| 零外部引用的类 | 21（多数为注解 / 框架自动注册；真可疑 2 个） |
| 贴图 / 模型 / blockstates | 244 / 202 / 31 |
| 配方 / loot_table | 82 / 27 |
| lang 条目（en_us / zh_cn） | 100 / 100 |
| mixin | 14（缺失 0） |
| 测试文件 | 4 |
| 已跟踪 jar | 1（gradle wrapper） |
