# eco 公开接口盘点（21.2.15：api 顶层类 121 个，我们引用 27 个）

现量来源：**eco 21.2.15 正式版那颗 jar**（`neoecobeta/neoecoae-21.2.15.jar`，与 reliqwq 从发布渠道下载的 `E:/software E/neoecoae-21.2.15.jar` **逐条目 CRC 全等**：4982 行 name+CRC+length 排序后 diff 0 行，md5 只差 zip 时间戳）+ 我们自己的 `build/classes/java/main`（310 个 class）。重跑日期 2026-11-06。

两把尺，口径不同，别混：

- **常量池尺**（`javap -v` 逐 class 扫 `cn/dancingsnow/neoecoae/api/...`）⇒ 我们点名 **27 个**。第四节这张表用它。
- **原始字节尺**（直接对 class 文件字节找内部名）⇒ **28 个**，多出来的是 `api/me/planning/ECOPlannerRequest`：它在我们的字节里只以签名/属性字符串出现，不是常量池里的引用。差一个就是这两把尺的边界，写数的时候要带口径。
- "eco 内部引用数" = 该类的内部名出现在 eco 自己多少个 class 文件里（不含自身，原始字节尺，eco 共 1903 个 class）。**这个数只说明它是不是 eco 的主干管道，不说明我们用不用得上。**

**哪些行是新量的、哪些不是**：标题的两个数、第四节的 27 行与 eco 内部引用数、以及新增的第六节，都是 21.2.15 现量。**第一节的第二条判定已被今天的证据推翻（照旧保留，写明翻案理由）**；第二、三节的判定是 21.2.4 那轮的推理，作为推理记录仍然有效，但它们引用的行号与"eco 内部引用数"没有逐行重测，引用之前先照 21.2.15 重跑那一行。

结论一句话：**仍然没有一片"我们自建、eco 已有公开口"的重复实现可以删掉**；这一轮反而多接上了三片（`IECOConfigurableCellDrive`、`IECOStoragePriorityHost`、`ECOCpuTierGlyph`），都是上游开口子、我们这边一行没接的那类。

---

## 一、纯减法（21.2.4 那轮量出来的两条，第二条已翻案）

| 东西 | 事实 | 处置 |
|---|---|---|
| `multiblock/trinity/fastpath/TrinityFastPathCompatibility.java` | 它引用了 `ECOFastPathFacade` 与 `ECOFastPathDispatchProvider`，但**全仓库没有任何调用方**——`isFacadeAvailable()` 与 `resolveReadOnly()` 只被自己命中 | 仍未删（第五节第 1 条）。删掉 ⇒ 已用接口少两个上游符号引用面 |
| ~~`api/storage/IECOStoragePriorityHost`：别去 implements 它~~ | **这条 11-06 翻案了。** 21.2.4 那轮的推理本身没错——当时"照签名补一遍是零现象"，因为没有任何东西消费它。现在 eco #154 交付的 `ECOStorageCellPriorities` 与面板窗口**就是消费者**：主机基线经这个接口读写，我们的 `setStoragePriority` 也不再只是同签名方法，而是契约的一部分（persistence + sync + mount refresh） | **已 implements**（`SimplifyStorageHostBlockEntity`，11-06）。守卫里那一臂是走接口写基线、再把挂载值读回来，所以"接上了"有读数，不是签名对齐 |

---

## 二、可评估（有真实现象，但要先验前提）

| 类 | eco 内部引用 | 我们这边对应什么 | 要验的前提 |
|---|---|---|---|
| ~~`storage/ECOBigIntegerStorage`~~ | 3 | **已验完：对不上。** 它的签名是 `insertBigInteger(AEKey, BigInteger, Actionable, IActionSource)` + `static insert(MEStorage, ...)`，引用方是 eco 的 `NetworkStorageMixin` / `ECOCraftingThread` / `ECOInfiniteStorage` —— 讲的是"**往 MEStorage 插超过 long 的量**"（无限仓与合成线程用）。我们的 `OversizeInterfaceLogic` 讲的是"接口一格允许多少"（`getMaxAmount(AEKey)` 返回 8192），两回事 | 无动作。eco 整个 jar 里 `InterfaceLogic` 与 `getMaxAmount` **各出现 0 次** ⇒ 它根本不碰接口槽，不可能有替这条的口子 |
| `AuxiliaryPatternHolder` | 9 | 我们的 L1 样板总线**直接注册 eco 的 BE**（`block/crafting/SimplifyCraftingPatternBusBlock.java:9` + `registration/ModRegistration.java:1215 :1260`） | 如果复用成立，那这条联动我们**本来就有**，旧记忆里"白拿、零读数"要改口径为"已具备、缺读数"。要跑一次盘片联动的眼验/断言 |
| `fastpath/EcoFastpathHost` | 8 | 我们的 Trinity 合成模块自己实现 AE2 的 `ICraftingProvider`（`SimplifyTrinityCraftingModuleBlockEntity.java:135`） | eco 的派发器会不会因为我们的 provider 没实现它的宿主口而跳过/重复计——这条只能读 eco 的调用点，或实测 |
| `me/cooling/ECOVirtualCraftingCoolantRegistry` | 2 | 我们有**第二套冷却**：分解机独立流体箱（`MeteoriteDissociatorBlockEntity.java:46 :50`）+ "冷却→功耗"模型（`api/MachineMood.java:52 :61`） | 这个注册表管的是"哪些流体允许当冷却剂"。我们的 `cryotheum_solution` 若不在里面，eco 的虚拟合成可能不认——但那是 eco 的功能，不是我们的分解机 |
| `integration/Integration` / `IntegrationManager` | 15 / 1 | 我们没有等价机制：靠构造函数里手写 `ModList.isLoaded` + 自建注册表 `api/StorageMatrixRegistry.java`（冻结点 `:268`） | 旧结论仍在：两个前提未验（我们的注解会不会被它扫到、`apply()` 能否赶在我们构造器之前）。**它治的是"漏注册"，我们那个 mixin 崩溃就是这类** |
| `storage/IECOBulkDisplayCell` | 2 | 我们的小宗仓 + `items/SmallBulkTypeCap.java:20`（绕开 eco final 后端硬编码 25） | `cycleCompressionCutoff` 是"循环切压缩阈值"，和我们的类型上限补丁**不是一回事**。加了它的现象只出现在 **eco 自己的巨型控制器**里，不在我们界面 |
| `NEFakePlayer` | 1 | 生产代码里没有假玩家；只有 gametest 用 `FakePlayerFactory` | 备用。哪天解离器要"代客取料"再用它，别自己造 |
| `IOverlayTextureHolder` | 4 | 无对应 | 与主机面板无关，但它是 eco 第二处**"递路径不递下标"**的先例 ⇒ 写上游稿子时引它（面板 marker 与档位名也该这样） |
| `PatternEncodingTermMenuExtension` / `components/AutoExportSides` | 1 / 0 | 我们的接口/供应器有导出侧逻辑（`SimplifySuperconductiveInterfaceBlockEntity.java:33`） | 需逐条比对，还没读 |

---

## 三、无关：合成侧 `me.**` 那 40 个

判定依据是实测的"我们没有对应自建"：`blockentity/trinity/SimplifyTrinityCraftingModuleBlockEntity.java` 的注释写得很明白——**`ECOCraftingCPU` / `ECOCraftingCPULogic` 绑死 eco 的 `NEComputationCluster`，要重写 CPU 生命周期与派发得改几千行**。我们没有重写，走的是另一条路：

- 我们自建的是**单任务状态机**，不是 CPU：`multiblock/trinity/TrinityCraftingExecutor.java`（Phase、提交给 AE2、单槽 `isBusy()`）；CPU 在我们那儿只被数个数。
- 并行/线程数是**喂给 eco 的数据**，不是实现：`api/SimplifyTier.java` + `config/NeoECOPrototypeServerConfig.java`。
- 订单：`multiblock/trinity/TrinityTaskRequest.java` 只有单 target + `int quantity` ⇒ `me/bigorder.**` 整族与我们无关。
- 生命周期/进度/诊断/附件：我们用的是**手写 NBT** + 自建快照 + LDLib2 的 `@DescSynced`。eco 那套挂在**它自己的 crafting job** 上，我们的 executor 不是它的 job ⇒ 接不上，也不需要接。
- 输出路由 `me/output.**`（7 个类）：我们的产物走 AE2 原生路径 ⇒ 无关。
- 菜单侧 `me/menu.**`：那是"给 eco 的合成确认菜单提供模式/大订单状态"的宿主口。**只有当我们想让 Trinity 出现在 eco 的合成确认界面里**才需要——目前没有这条需求。

---

## 四、我们已经引用的 27 个（21.2.15 现量，按 eco 内部引用数排）

| api 类 | eco 内部引用 | 我们用它做什么 |
|---|---|---|
| `api/IECOTier` | 129 | `SimplifyTier` / `SimplifyCraftingTier` 实现它；面板贴图、空闲功率基数、CPU 角标都从这里问 |
| `api/ECOTier` | 44 | 对照与比较：eco 三档的内部档位号是 1/2/3，我们是 0 |
| `api/storage/ECOCellType` | 35 | 我们自己的 cell type 注册与面板行判定（`isSelfGrowType`） |
| `api/storage/IECOStorageCell` | 33 | 每只盘的库存实现（含自产引擎那只） |
| `api/me/provider/ECOFastPathDispatchProvider` | 22 | F1 母线用 eco 的 BE，天然 implements；也是 CONFIGURE 那条的第二道闸 |
| `api/ECOCellModels` | 19 | 驱动器内格子模型注册 |
| `api/me/ECOFastPathFacade` | 15 | 只被第一节那条待删的 `TrinityFastPathCompatibility` 引用 |
| `api/storage/ECOStorageCells` | 13 | 注册我们的 cell handler |
| `api/me/planning/ECOPlannerOptions` | 11 | 规划请求选项（`ECOPlannerRequester` 那条线的守卫用） |
| `api/power/ECOPowerRole` | 11 | 空闲功率 provider 只认 `COMPONENT` 那一档 |
| `api/me/network/CraftingCapabilitySnapshot` | 10 | 能力快照对照 |
| `api/me/network/ECOCraftingNetworkSettings` | 8 | 快速规划那两把开关的读数来源（`shouldUseFastPlanner`） |
| `api/multiblock/ECOMachineKind` | 6 | 成型定义 provider 的键 |
| `api/PatternStorageHost` | 6 | 样板迁移源池那道闸（issue #128/#136 线） |
| `api/IHostPanelTextureProvider` | 6 | 五台方块答面板换图 |
| `api/storage/IECOCellHandler` | 6 | 自产元件的 cell engine |
| `api/ECOCpuTierGlyph` | 5 | **11-06 新接**：合成确认页的档位角标（issue #153 那条 default） |
| `api/multiblock/ECOMultiblockDefinitions` | 5 | 成型定义注册（替掉一条 mixin） |
| `api/ECOComputationModels` | 5 | 缆按宿主档位选模型（替掉另一条 mixin） |
| `api/storage/IECOStorageCellItem` | 4 | 细胞物品的父口 |
| `api/me/ECOCraftingCPU` | 4 | 只被读（并行/线程数对照），不实现 |
| `api/storage/IECOConfigurableCellDrive` | 3 | **11-06 新接**：每盘优先级（issue #154），偏移存在盘上 |
| `api/power/ECOPowerProfiles` | 3 | 我们命名空间的 idle 功率 provider |
| `api/storage/ECOStorageCellPriorities` | 3 | **11-06 新接**：面板那扇优先级窗口的后端 |
| `api/storage/IBasicECOCellItem` | 3 | 自产盘与 Beyond 那只的父口；**不再是卡片关联表的入口**（11-06 改认 AE2 的 `ICellWorkbenchItem`，否则 omni 八只整族被跳过） |
| `api/storage/IECOStoragePriorityHost` | 1 | **11-06 新接**：主机基线经这个接口读写（见第一节翻案那条） |
| `api/me/planning/ECOPlannerRequester` | **0** | 我们套它规划一次作业再对照两套装不进同一个 plan |

另外 `api/storage/ICellHost` 记在"未引用"里是**假未用**：`util.ICellHost extends api.storage.ICellHost`，我们的驱动器与计算模块仓实现的是 util 那个 ⇒ 已间接实现。副作用：`canExtractCell` / `getCellExtractionBlockReasonText` 走 default，我们没覆写 ⇒ 新面板上"这只仓为什么取不出来"的提示会是空的。

---

## 五、21.2.15 新量到的两件事（上游自己也没用的口子，与附属侧的探测能力）

**三个 api 类，eco 整个 jar 里没有任何东西引用它们**（原始字节尺，1903 个 class 全扫）：

| 类 | 读到的形状 | 对我们的意义 |
|---|---|---|
| `api/extension/ECOAddonApi` | `VERSION`、`FEATURES: Set<String>`、`supports(String)` | 见下面那段——它是"上游开了哪些口子"的**自报清单**，但清单不全 |
| `api/me/planning/ECOPlannerRequester` | 我们引用它，eco 自己不引用 | 它是纯给附属的入口，所以它坏了没人陪葬 ⇒ 我们那条守卫（`ecoPlanEligibilityReadsTheSameJobPlannedBothWays`）是唯一读数 |
| `api/me/provider/ECOBatchCapacityProvider` | 121 个 api 类里没人引用的第三个 | 还没读形状，别当已评估 |

`ECOAddonApi.FEATURES` 现量 11 个旗标：`power_profiles`、`multiblock_definitions`、`multiblock_parts`、`multiblock_requirements`、`computation_cells`、`host_cable_models`、`tier_features`、`storage_channel_displays`、`native_me_storage_restrictions`、`workstation_adapters`、`workstation_modules`。

**这条要记住**：我们真正接上的三片（#154 的每盘优先级、#153 的档位角标、#155 的方块名标题）**一个都不在这份清单里**。所以"这颗 eco 有没有那个口子"只能靠**版本地板 + 对着发布件的字节码尺**判，`ECOAddonApi.supports(...)` 帮不上忙——它记的是 eco 自己的扩展注册表形状，不是"给附属的接口"。反过来说，`ECOExtensionRegistry<T>`（`register(ResourceLocation, int, T)` / `unregister` / `entries()` / `revision()`）确实是 eco 那 10 个注册表共同的底座，这一族用旗标描述是准确的。

---

## 六、下一步（按性价比排）

1. 删 `TrinityFastPathCompatibility`（零调用方，纯减法）。
2. ~~验 `ECOBigIntegerStorage` 能不能替掉那个 mixin~~ —— **已验完，替不掉**（见第二节该行）。真要减一个 mixin，对象是 **AE2**，不是 eco。
3. 验样板总线那条：`AuxiliaryPatternHolder` 的 9 处联动到底在不在（旧记忆写"零读数"，需要一次断言或眼验把它钉成"有"或"没有"）。
4. 上游稿子两件事：面板贴图四张覆盖不到 + 档位名 `tierName` 写死三档 → 已写成 `NEO_ECO_AE_PR_GUI_TIER_ART.md`，未发送。**11-06 补一条**：那份稿子可以顺手请求把 #153/#154/#155 这类"给附属的接口"登记进 `ECOAddonApi.FEATURES`，这样附属能用一个 `supports(...)` 判，而不是靠版本号区间猜。
5. `api/me/provider/ECOBatchCapacityProvider` 还没读形状 —— 它和 #152（并行阶梯）可能是同一件事的上游版本，读它比继续催 #152 便宜。
