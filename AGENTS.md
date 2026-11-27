# 仓库约定

> 面向 AI agent 与贡献者。**改代码前先读 §0 那十条**，其余各节按指针查——不必从头读完。
> 每一条都对着现仓核过；标了"现状"的是待办，不要当成已达成。

---

## 0. 承重规则索引

这一节是必须遵守的部分。后面 §2 / §13 / §15 的大部分篇幅是**结论怎么来的**（抬级推演、字节码读数、控制组记录），只在追来历时时查，不是新增约束。

1. **不改 eco 源码**，所有接入只走 Mixin + 公开 API。—— §1
2. **目录依赖单向**：`api ← blockentity / items / block ← integration`；`api/` 禁引上层，唯一例外写在 §3 里，不许扩大。—— §3
3. **可选依赖有三条轴**：内容 / must-resolve 类连接 / 注册顺序。只审第一条等于没审。—— §4
4. **编译基线 = 运行地板**；地板不许只写下限。现量版本在 §2 表格里，区间后面的推演是附录。—— §2 §5
5. **静态状态不许按世界/维度/Level 键**跨钩子存活；非主线程一律不读方块。—— §6
6. **数据结构**：record 优先；Tier 只描述等级不描述实现；配置每次现读；enum 不写 `switch`；权重是份额不是概率。—— §7
7. **Mixin**：类头写版本锁、必须命中的注入用 `require = 1`、每条注入的撤销条件写在类头（要 `javap` 能判）。—— §8
8. **测试**：探针隔离、空扫描必须 `fail`、每批配一条会红的控制组、撤控制组**不许用 `git checkout`**（反向替换那个字面量）。—— §9
9. **发布面**：被 gitignore 的 `resources` 照样进 jar；CRLF 文件别用 `sed -i`；语言文件写盘前先解析一次 JSON。—— §10
10. **对外边界**：`src/main/resources/assets` 里不许出现 `neoecoae:` 引用（CI 拦）；物品/方块显示名不许逐字抄上游已有的名字；注入方法加 `neoecoae$` 前缀。—— §11 §14

---

## 1. 项目定位

Neo ECO AE Extension（下称 eco）的 **L1 超前期扩展**：给 eco 的 L4 体系往前加一级——存储、计算、合成三个子系统，造价低于量子。另含无限存储矩阵、KubeJS 自定义、世界生成、装饰内容。

**不是** eco 的 fork，**不改** eco 源码。所有接入只走 Mixin + 公开 API。

---

## 2. 环境锁定

| 组件 | 版本 | 出处 |
|---|---|---|
| Minecraft | 1.21.1 | — |
| NeoForge | 21.1.251 | — |
| AE2 | 19.2.17，地板 `[19.2.17,)`；编译 = 运行 = 同一颗 | `build.gradle`、`neoforge.mods.toml` |
| Neo ECO AE Extension | **21.3.0**（2026-11-27 换入用户提供的 `参考模组/最新eco/neoecoae-21.3.0.jar`；7,812,374 bytes，SHA-256 `f453b62c3676a92682b4487f5f5a87ba738355bc0e51d16bc2ce7e69324e5fbf`）／地板 `[21.3.0,)` | `build.gradle:274`、`neoforge.mods.toml:81` |
| LDLib2 | 2.2.40，**硬依赖**，地板 `[2.2.40,)`（抄 eco 自己那条，不是我们挑的） | `build.gradle` |
| 数据能源（Data Energistics） | 3.4.0，**可选**，只有 `compileOnly`；dev 运行期 2026-11-12 起不装 | `build.gradle:291` |
| 闪电科技 ae2lt + thunderbolt | 2.1.4-beta3 / 2.0.3，`runtimeOnly`，我们 0 处 Java 引用 | `build.gradle:304` |

**地板为什么在这颗，各一句话**（抬级的完整推演在 [docs/eco-floor-history.md](docs/eco-floor-history.md)）：

- **21.3.0**：`crafting.*` 与 `storage.*` 整树迁到 `grid.crafting.*` / `grid.storage.*`；`ECOInfiniteStorageMember.isMember(ItemStack)` 被删。我们不再点这个内部类，驱动器灯色改用 MC 公开数据组件读取旧标记 `neoecoae_infinite_member`，保持匿名旧元件的紫色灯语义。
- **21.2.17**：`SimplifyTier` 与 `SimplifyCraftingTier` 各覆写 `getCraftingStatusCpuTierGlyph()`，返回类型是 eco 的 `api/ECOCpuTierGlyph` ⇒ 方法描述符 must-resolve，旧 eco 上加载这两只枚举就断。同轮撤掉了我们自己那只状态角标 mixin（§8）。
- **21.2.16**：`SimplifyStorageHostBlockEntity` / `SimplifyDriveBlockEntity` 各 implements 一条 #156 交付的接口（`api/storage/IECOConfigurableCellHost`、`api/storage/IECOCellWorkbenchDrive`），21.2.15 的发布件里这两条 0 命中 ⇒ 断在类加载，不是断在玩家点那一下。
- **21.2.12**：两只流体仓读罐子统一走 `getFluidTankHandler()`（21.2.10/21.2.11 各 0 条声明、21.2.12 有 2 条）⇒ 现在是这条访问器钉住地板，不再是那条字段描述符。

**判一颗上游版本能不能跟，符号面不够。** 三把尺都要过，缺一不可：

1. 类级——我们字节码点名的 eco 类型，逐个对目标 jar 的条目表；
2. 成员级——`tools/audit_eco_abi.py`；
3. **构造子级**——只比构造子描述符（`tools/audit_eco_ctors.py`，2026-11-27 对 21.3.0 现量：**69 条，0 缺**；历史 beta2 为 68 条 0 缺）。

**21.3.0 公开 API 核对结论（2026-11-27）**：`ECOExternalCrafting` / `Adapter` 与 `ECOParallelCraftingProviders` 的新快速规划入口都要求先注册 adapter；我们没有注册，现有 `ECOExternalCpuSupport` 语义不变，因此不接。`ECOFastPathFacade.prepareStatefulCalculator(...)` 的返回类型仍是内部类型，不作为干净缝接。无限元件新增的候选/安装钩子不会被我们的元件触发（`isInfiniteStorageEligible()` 仍答 false，tier 也不开 infinite 特性）；`ICellHost.getBuiltInCellForDisplay()` 默认 null，盈能箱子已有真实 `cellStack`，无需覆写。

第 3 把不能省：成员级尺子结构上看不见"record 多一个分量"，因为 javap 把构造子印在类名下、`<init>` 永不匹配——而 record 加分量正是 `NoSuchMethodError` 那一类。尺子之外还要**照那份 jar 重编 + 跑套件 + 客户端开一遍界面**，三件齐全才算跟过。

**jar 身份只认逐条目 name + CRC + length**（`unzip -v` 三列排序后 diff）；md5 会因 zip 时间戳假报"不是同一颗"。换 eco 基线时 `build.gradle` 点名的**文件名一起改**：现在是 `neoecobeta/neoecoae-21.3.0.jar`，与用户提供的 `参考模组/最新eco/neoecoae-21.3.0.jar` 按 SHA-256 核对为同一颗。上一档 `neoecobeta/neoecoae-21.2.19-beta2.jar` 与 `neoecobeta/neoecoae-21.2.19.jar.baseline-cbd282778` 都保留；beta2 与 cbd282778 同报 21.2.19，但条目 5090→5103（+57/-44，1406 changed），旧基线缺 `StorageHostUI$CellExtractor` 正是 1.4.11 崩溃来源。21.3.0 静态读数：**416 个 class、522 条 eco 引用，类级 0 缺；69 条构造子 0 缺；成员脚本相对 beta2 无新增真实缺失**（脚本仍会打印父类/接口默认方法/构造子假阳性）。GameTest 337/337 已过；21.3.0 客户端与 GUI 实机读数尚未取得，不能把静态/服务端绿当作客户端验收。

**数据能源这条线是"编译地板 / 运行基线"曾经分开的历史原因**：DE 3.4.0 自己 required `ae2 [19.2.18,)` + `ldlib2 [2.2.44,)` + `ae2wtlib`，那两条区间是把 dev 运行期顶上去的唯一原因。11-12 起它不在 dev 运行期，编译与运行重新合成一颗；音乐那一族本来就整族对玩家隐藏。**接回来是三步，少一件就是启动期依赖报错**：① 恢复那行 `runtimeOnly`；② ae2 / ldlib2 的 `runtimeOnly` 回到 19.2.18 / 2.2.44；③ 去掉 `run/mods/` 里那颗 jar 的 `.disabled` 后缀（改名，不是删除）——原文见 `build.gradle:288-290`。

**闪电科技**只有 `runtimeOnly`：我们这边 0 处 Java 引用，唯一点名 `ae2lt` 的是那条过载处理器配方 JSON（带 `mod_loaded` 条件），装它只为给那条配方一个真的加载环境。它 required `ae2 [19.2.17,19.3)` + `ae2wtlib_api` + `thunderbolt [2.0.3,3.0.0)`，**2.1.4-beta3 一个 ldlib2 依赖都不写**，所以不需要抬 LDLib2。

- 新增 Mixin 时在类头写版本锁注释：`/* Version lock: NeoForge 21.1.251, eco 21.2.17. */`（写你实际编译 against 的那颗；历史 mixin 头部留着旧号，改它只在动那条注入时）。
- 模组版本号是 `build.gradle:8` 里的**字面量**，没有 `gradle.properties`。
- 上游 jar 全在本地目录：eco 在 `neoecobeta/`，AE2/LDLib2/可选 mod 在 `libs/`，**两个目录都被 gitignore**。新克隆必须按 `build.gradle` 点名的文件名把 jar 放进去才能编译。CI 不编译（只跑 `git diff --check` 与 grep，见 `.github/workflows/repository-checks.yml`），所以"CI 绿"永远不证明这条路是通的。
- 面板换图只走 eco 的公开口子：五个方块 implements `IHostPanelTextureProvider`、两个档位 implements `IECOTier.getPanelAtlas(String)`；eco 自己那两张换图查表类仍是包私有 final，addon 注册不了。

**可选依赖**（未装时模组必须能启动、能进服）：MEGA Cells（小宗仓与压缩链）、AE2 OmniCells（通用/量子矩阵）、AppliedFlux（FE 存储矩阵）、Mekanism + Applied Mekanistics（化学矩阵）、Ars Nouveau + arseng（魔源）、Curios（玩偶首饰栏）、KubeJS、JEI/EMI/Jade（compileOnly）、Beyond Dimensions（见 §11）、数据能源（音乐数据类型，见上）、闪电科技（那条过载处理器配方）。

---

## 3. 目录分层

```
api/          接口 + 不可变数据。只依赖 MC/NeoForge/eco 公开 API。
blockentity/  运行时状态（BE、cluster、calculator）。
block/        Block 类。
items/        物品元数据。
integration/  可选依赖代码，按 mod 分子包。
mixin/        Mixin，逐个对应上游类。
gametest/     测试。探针类单独成文件。
```

依赖方向单向：`api ← blockentity / items / block ← integration`，`mixin` 仅按需。

`api/` 不许引用 `blockentity/`、`items/`、`integration/`。

**唯一例外，且是限定的**：`api/StorageMatrixRegistration` 允许引用 `items/`（仅此一层、仅此一个文件）。理由：它是注册工厂，工厂必须点名它要 `new` 的具体物品类；而且它是对外的入口——`docs/kubejs.md` 宣传过它、1.2.12 起的每颗 jar 里都有它，挪动或改名就是破坏公开面。`.github/workflows/repository-checks.yml` 里那道 grep 按这个口径写（该文件仍禁 `blockentity/`、`block/`、`integration/`），别把它当成"api/ 分层规则松了"。
**不要扩大这条例外**：它成立的唯一理由是"已经对外承诺了"，不是"工厂就该这样写"。新写的注册代码应该放在 `registration/` 或 `integration/kubejs/` 里，而不是再往 `api/` 塞一个能引用物品类的入口。（该文件目前 0 调用者、0 测试执行过，这是另一条待办，不构成豁免理由。）

**关于 `integration/` 的规矩不是"一律禁止"**（现仓有 13 处 import，一刀切会要求先大改），而是二选一：

1. 那个子包对应的是**硬依赖**（`integration/ae2/`）⇒ 可以直接引；
2. 对应的是**可选 mod** ⇒ 引用点必须被**同一个方法内**的 `ModList.get().isLoaded(...)` 包住（`SimplifyStorageHostBlockEntity.createStorageTypeLines` / `panelRowOf` 就是这个形状）。

---

## 4. 可选依赖：三条轴，一条都不能漏

审一个可选 mod 时按这三条各自过一遍。**只在 dev 跑是审不出第 2、3 条的——开发机什么都装着。**

### 4.1 内容轴

没装那个 mod，那一族内容注册不出来就行，不许连累别的族。反例（1.3.4 之前）：`storage_matrix` 整个 builder 绑在 OmniCells 的闸上，没装 OmniCells 的包连普通物品矩阵都写不了。

### 4.2 类连接轴（这条最容易想错）

**实测结论**：方法体里**没走到的分支**不会解析那个类，连 `-Xverify:all` 都不报 ⇒ `if (isLoaded) { IntegrationClass.doThing(); }` **够用**。

真正会在闸生效之前就炸的，是 JVM **must-resolve** 的那几类引用：

- 把 `Foo.class` 当参数递给别的方法 —— 1.3.4 的启动崩溃就是 `BuilderTypeRegistry.add("storage_matrix", StorageMatrixBuilder.class, ...)` 这一行把 `MekanismKey` 拉起来的；
- 字段类型、父类、方法签名；
- 真的走到的 `instanceof` / checkcast。

**污染常常隔一跳**：我们引 eco 的一个类，eco 那个类在 `neoecoae.integration.ae2omnicells.*` 子包里，它才去引 OmniCells。所以判据是"顺着父类走一遍"，不是"我这一行有没有写第三方名字"。

**落地做法**：注册**无条件**，把第三方的**名字**关进自己的小类里 —— 见 `integration/kubejs/ChemicalMatrixSupport`、`OmniMatrixSupport`（提交 `967eb24`）。
**不要**为此上 `Class.forName` / `ServiceLoader`：全仓 `ServiceLoader` 0 处，两处 `Class.forName` 都不是这个用途（一处在 gametest 探针里故意不初始化，一处在 `TrinityFastPathCompatibility`）。

### 4.3 顺序轴

`ordering` 也是依赖。`ae2` / `neoecoae` 一度写成 `NONE` ⇒ 我们能在 AE2 绑定自己的注册表之前注册物品，于是"不装 MegaCells 就开局崩"（issue #6，1.3.3 用 `AFTER` + 奇点 key 懒取两条一起修）。我们若干物品**继承 eco 的细胞物品类**，绑定注册项就会加载 eco 的类并跑它的静态块 ⇒ `AFTER` 是必需的，不是洁癖。

---

## 5. 版本地板不许只写下限

1.3.3 的地板 `[21.2.1-beta2,)` **没有上限** ⇒ eco 21.2.2 删了 `blocks/entity/crafting/CraftingUIHelper` ⇒ 装新 eco + 旧我们的玩家，右键流体输入仓就 `NoClassDefFoundError`。

上限我们**故意不写**（会拦掉所有新 eco 玩家）。替代做法是每次跟上游大版本时重跑一次符号扫描：把发布 jar 里引用的上游类逐个对上游 jar 的条目表（那次实测：1.3.3 引 102 个 eco 类，21.2.2 只缺 1 个）。

**判据：别把区间当安全网。** 拦不住"上游改名"，也拦不住"玩家升上游"。真正接住的是"编译基线 = 运行地板"这一条。

---

## 6. 静态状态的生命周期

**禁止以世界、维度或 `Level` 实例为键的静态表跨钩子存活。** 注意键的形式不是重点：真踩的那次是 `ResourceKey<Level>` + long 的**维度**键，它跨存档存活，单机换存档就串味。

**多线程铁律（工作线程防御）**

- 绝不在非主线程（如 `ChunkEvent.Load` 等事件回调、网络包回调）中调用任何 `Level.getBlockEntity`、`getBlockState` 或触发区块加载的方法。
- 强制落地模式：工作线程只做纯内存判断；若涉及世界查询，直接返回"离线/看不出"结果，并将待处理标记推入主线程的 burst（`urgentUntilTick`）或状态队列中。由主线程的 `serverTick` 在主线程安全地完成重建。**永远不要相信 `hasChunk` 能阻止主线程阻塞。**

现仓落点：`WirelessLinks.Data.urgentUntil` + `WAKE_BURST_TICKS` + 主线程那条 `LevelTickEvent.Post → sweep` 就是这个模式的实现；`mayReadBlocks` 是"纯内存判断"那一半的闸。。**这条已经量掉了（2026-11-04）**：RPC 回调跑在**服务端主线程**。三段读数连着看：`PayloadRegistrar(String)` 的构造子把私有字段 `thread` 赋成 `getstatic HandlerThread.MAIN`（`HandlerThread` 只有 `MAIN` / `NETWORK` 两档）；LDLib2 的 `LDLNetworking.registerPayloads` 只走 `registrar → playToClient / playBidirectional`，**整个 jar 里 0 处 `executesOn`**（这把尺先在 `PayloadRegistrar` 自己的 javap 里数到 1 处命中，才自证它看得见这个词）；`PacketRPCBlockEntity.handle` 直接 `invokeinterface IRPCBlockEntity.handleRPCPacket(RPCSender, [B)`，没有 `enqueueWork` —— LDLib2 里唯一引用它的是 `client/ClientProxy`。所以那 7 只 BE 的 RPC 路径里 `level.getBlockState(worldPosition)` 是主线程读自己那一格，合法；本条禁止的"同步等待"我们本来也没有。`neoforge-*-sources.jar` 只含被 NeoForge 打过补丁的 MC 类、`PayloadContext` 不在里面，答案在 `build/moddev/artifacts/neoforge-21.1.251.jar` 的 javap 里。

- 允许的静态状态只有全局常量与注册表，key 是 `ResourceLocation` / `String`。
- 需要跨 tick 的待办：挂 BE 实例字段 + 生命周期钩子（`onChunkUnloaded` / `setRemoved` / `onRemove`）。
- **MC 会跳过"方块已经换了"的 block tick** ⇒ 任何交接表必须在 `onRemove` 里清，不能只靠下一次 tick。
- 唯一的现存例外：`SimplifyComputationSystemBlock.PENDING_SHAPE`（维度键 + 位置键）。它不跨存档（`ServerStoppingEvent` 里 `clearPendingShapes()`），逐位置条目在 `onRemove` 里清；残留只有"同一会话内某个已卸载维度里没被消费的一条"，有界且无害。**它不能迁去 BE 实例字段**：这台主机的 BE 类型是 eco 的 `ECOComputationSystemBlockEntity`，addon 子类会掉出 AE2 的机器表（见 §13 同族教训）。要动它先量这条。
- **`ChunkEvent.Load` 跑在区块工作线程上，那条线程里不许读方块。** 机制是三处互锁，全部现量：`ChunkStatusTasks.full` 里 `EventBus.post` ⇒ 我们的钩子不在主线程；`ServerChunkCache.getChunk(...)` 开头就是"不是主线程？把这次读排到主线程再 `join`"；而世界加载中主线程正卡在 `BlockableEventLoop.waitForTasks` 等区块池 ⇒ 互等，"准备生成区域中：0%"永远不动。`getChunkNow` **不是出口**，它自己写着"非主线程返回 null"——MC 的设计意图就是"离线线程别问区块内容"。`hasChunk(x,z)` 是 holder 查询，本身合法、也确实答了 true；骗人的是它后面那一次 `getBlockEntity`。落法：`WirelessLinks.mayReadBlocks` 一只闸管住 `presence` 与 `node` 两条读路径，离线时答"看不出"（ASLEEP / null）而**不是**答"没了"（⇒ 一条记录都不许掉），唤醒事件只把这条标成紧急，真正的重建交给随后主线程那一串 burst。守卫 `wirelessLinkWakeFromAnotherThreadNeverReadsBlocks` 的做法是让主线程在 `latch.await` 里故意不排队列：摘掉闸它就在"5 秒答不上来"那句红（控制组 10-27 现跑：6 条里 1 红，红的就是那句），装上闸 6/6 绿。

---

## 7. 数据结构约定

- **record 优先**（equals/hashCode 免费、可做 map key、可模式匹配）。
- record 里**可以**带 `ItemStack` / `FluidStack`，条件是只读、要改就 copy —— `DissociationRecipe` 就是这种（`input` + 两罐流体）。别为了"别放可变对象"把它们拆成 id + count。
- **Tier（`SimplifyTier`）只描述"等级多强"，不描述怎么实现。**
- **配置每次现读**，不许静态缓存：对 `Config.L1_CPU_THREADS.get()`，错 `private static final int X = Config.X.get()`。回归守卫 `l1ComputationNumbersComeFromConfig`。
- enum 不写 `switch`：让成员各自覆写方法或携带数据。
- 注册表 `Map<ResourceLocation, T> BY_ID`，返回不可变视图或快照，不返回活 map。
- **权重是份额，不是概率**：解离表在加载时 Σ→1 归一，`1/1/1` 意思是各三分之一。任何对外文案不许写"按权重随机抽取"这种暗示必出的句子。

---

## 8. Mixin 规范

- 类头写版本锁（§2 格式）。
- `require = 1` 用于必须命中的注入：上游删方法时在类变换期崩，比静默失效好。**崩 > 静默。** `require = 0` 只在能接受功能失效时用，并配一条 GameTest 断言。
- **现状（2026-11-20 现量）：12 个注入点（6 `@Inject` + 6 `@Redirect`）分布在 7 个类里，另有 2 个 Accessor。** `require = 1` 现在写在 5 条上，剩下 2 条（`ECOClientUIBridgeMixin` 的 `@Inject`、`FixedBlockEntityRenderersMixin` 的 `@Redirect`）靠 `neoecoprototype.mixins.json` 的 `injectors.defaultRequire = 1` 兜住 —— 那一条是承重的，别删。2026-11-13 撤掉状态角标那只（`ComputationCpuStatusTierGlyphMixin`）；**2026-11-20 又撤掉 `MolecularAssemblerFluidOutputMixin`（4 条）**：L1 装配室重写为独立机器（`AENetworkedInvBlockEntity + IUpgradeableObject + IGridTickable + ICraftingMachine`，ExtendedAE 同款形状），周期引擎是我们自己的，纯流体产出不再经过 AE2 的物品完成路径。
- **不要用 `@At("RETURN")` 改局部变量** —— 返回值已经压栈，改局部变量不影响结果。要用 `cir.setReturnValue(...)` 或 MixinExtras 的 `@ModifyReturnValue`。（上游那条死钩子就是这么写的，见 §11。）
- `mixinListMatchesShippedMixinClasses` 是双向守卫：增删 Mixin 类或 json 条目，两个方向都要同步。
- **`client` 数组里的注入没有任何服务端面**：mixin 目标不是编译期符号，编译查不到；`runGameTestServer` 是 dedicated 侧，那两条根本不参与类变换。所以"客户端起一遍"必须带读数，不是随便开一下：确认 `FixedBlockEntityRenderersMixin` 与 `SlotYAccessor` 各被应用一次，然后点开一个超尺寸接口界面（走 `SlotYAccessor` 的 +36）和一台带 eco 固定渲染器的机器（我们注册了三台：驱动器、计算驱动器、冷却控制器）。
- **@Redirect 还有一层 require 数不出来的失效**：它 catch 的异常类型是写死的（`ArrayIndexOutOfBoundsException`）。上游或 NeoForge 换了越界 lookup 的失败形态，注入仍然"命中一次、报告成功"，却什么都不做 —— 只有读数能看出来，静态检查与 `require` 都看不见。
- **每条注入的撤销条件写在各自类头**，是 `javap -c` 能直接判的字节码条件（`ClientUIBridge.call` 的异常表是否收进 `RuntimeException`、方法开头是否加了 dist 判断；`FixedBlockEntityRenderers.render` 偏移 59 那道 `getBlockEntity` 外面是否有了边界检查）。去留按判据读，不靠再讨论一轮。
- **客户端起一遍的复现命令与预期读数（2026-11-04 实测）**：`JAVA_HOME=F:/java JAVA_TOOL_OPTIONS=-Dmixin.debug.verbose=true ./gradlew runClient --offline`——verbose 走环境变量注入游戏 JVM（日志里 "Picked up JAVA_TOOL_OPTIONS" 为证），**不改仓库任何文件**。日志应当出现六行 `Mixing <我们的Mixin> from neoecoprototype.mixins.json into <目标类>`：五对在启动阶段就有，**第六对（`ECOClientUIBridgeMixin` → `gui.crafting.ClientUIBridge`）必须空手右键 `simplify_crafting_network_interface`（通讯接口）才出**——那个类只在 `CraftingInterfaceUI` 的 JSON 布局构建 `input_search` / `output_search` 两个 66×12 搜索框节点时加载，所以**认界面的标志就是上沿那两个并排的搜索框**，看见即已加载、即已应用。**普通 `simplify_crafting_interface` 永远不会触发它**：那只方块是我们自己把 `useWithoutItem` 写成返回 `PASS` 的死端点（GUI 只挂在通讯接口上，C1/L1 同规则），点它不是「注入失效」，是根本没走到那条 UI 路径——别再把它当读数。
- **客户端 `Mixing` 行读数（2026-11-21，cbd282778 基线实测）：启动 8 行 + 交互 1 行 = 9。** 启动的八行：SlotYAccessor（accessor 在这条线上也产生 Mixing 行，别按"accessor 不产生行"排除它）、EnergyServiceRequesterReAddMixin、InterfaceLogicAccess、CraftingBlockEntityDollSidesMixin、AdvancedAEPatternUpgradeMixin、ExtendedAEPatternUpgradeMixin、FixedBlockEntityRenderersMixin（client 数组唯一一条）、CraftingCPUCalculatorFamilyMixin。第 9 行（`ECOClientUIBridgeMixin` → `gui.crafting.ClientUIBridge`）必须空手右键 `simplify_crafting_network_interface`（通讯接口）才出——那个类只在 `CraftingInterfaceUI` 的 JSON 布局构建 `input_search` / `output_search` 两个 66×12 搜索框节点时加载，**认界面的标志就是上沿那两个并排的搜索框**。**普通 `simplify_crafting_interface` 永远不会触发它**：那只方块是我们自己把 `useWithoutItem` 写成返回 `PASS` 的死端点（GUI 只挂在通讯接口上，C1/L1 同规则），点它不是「注入失效」，是根本没走到那条 UI 路径——别再把它当读数。EC9、通讯核心塔、反应堆删除三轮都零新 mixin；再增删 Mixin 时按本条重数。日志里别的 mod 的 "Error loading class" 是它们自己的可选依赖缺席，与我们无关。
- **beta2 客户端启动读数（2026-11-24）**：上述 8 行全部命中，客户端进入现有世界后正常退出；这次没有打开通讯接口，因此第 9 行与存储/计算/元件工作台界面仍未取得 beta2 实机读数，不能把启动 8 行当作 GUI 验收。

---

## 9. 测试规范

### 模板

`empty`（无世界，纯逻辑，优先用）、`l1_room`（17³，多方块成型）、`trinity_room`（Trinity 专用）。

### 必须遵守

- **探针隔离**：`@GameTest` 方法体里不许内联可选 mod 的类 —— 那会让整台 `gameTestServer` 起不来。引用第三方类的代码放单独的探针类，主测试只在 `ModList` 检查通过后调用。
- **空扫描必须 fail**：扫不到目标就 `helper.fail(...)`，不许静默 `succeed()`。
- **`getLevel().getServer().getResourceManager()` 只装 `SERVER_DATA`**：模型、贴图、语言这些 `CLIENT_ASSETS` 在 GameTest 里 `getResource` 一律 empty（11-07 现量：把守卫判红在 "no pack in this game carries it"，而那六张图在 `build/resources/main` 里明明都在）。要读客户端资源就走 mod 自己的 classpath —— `NeoECOPrototype.class.getResourceAsStream("/assets/<ns>/<path>")`，判 `.mcmeta` 是否存在也用同一个口（null = 没有）。loot_table / worldgen 这类数据包才用资源管理器。附带的坑：`ImageIO.read(stream)` 读回来的图片尺寸必须按名字分别断言（body 128×128、两条灯带各 128×256 两帧、工作态屏幕 16×352 共 22 帧，这三张各带自己的 `.mcmeta`——灯带 15 刻带插值、屏幕 5 刻不插值；待机屏幕与 particle 各 16×16 且无 `.mcmeta`），一刀切的规则会把 `controller_particle` 判成动画；`.mcmeta` 要走同一个 classpath 口判（`addonAsset` 返回 null = 没有）。两条灯带的第二帧不是"跟第一帧不一样"就算数：正面与顶面点亮的那 **344** 个像素一个都不许停（eco 自己那对实测翻的就是这 344 个；只翻 68 个的早期稿实机看着是静止图，11-07 现量）。**11-19 起这批预烤灯带/屏幕图已删（见 §15 工作站那行），外观守卫改为检查模型槽位、palette 色板与 atlas 声明闭包；"按名字分别断言、一刀切会误判"这条教训不变。**
- `required = false` 给可选依赖测试，内部仍要做 `ModList` 检查。
- **负向优先**：边界、拒绝路径、过量消耗、错误状态。正向只证"能跑"。
- **每批配一个会红的控制组**，并且要能指出这条断言是哪一层验的（套件里 0 次引用某功能，只证编译面，不证功能被验过）。
- **撤控制组别用 `git checkout -- <file>`**（2026-11-04 现踩）：控制组往往是在**未提交**的正式改动上临时改一个字面量，`checkout` 回滚到的是 HEAD，于是把正式改动一起冲掉，而且**不报错**——那次跑完控制组后 `grep` 才发现枚举又变回旧值，只能把 javadoc 与数值整段重做。撤控制组的正确做法是把那个字面量反向替换回去（或一开始就用 `git stash create` 式的一次性补丁）；要验证"提交后的样子"用 `git checkout-index --prefix=/tmp/x/ -a`，它不动工作树。
- 失败信息只留"哪里、什么值、期望什么"，诊断细节走日志。
- **套件看不见"钩子跑在哪个线程"。** gameTest 的世界一开始就在 tick，主线程不卡在启动区生成上，所以 §6 那条 `ChunkEvent.Load` 死锁在 165 条全绿的情况下照样存在（10-27 实测：客户端进世界停在 0%，两份间隔 44 秒的线程栈一模一样）。任何挂在**加载期事件**（`ChunkEvent.Load`、BE `onReady`、维度进入）上的代码，验收只能靠客户端起一遍。
- 新测试优先用机器包围盒（`machineSnapshot` / `blockCounts` / `ledger` / `clearMachineBox`），不要扫整间房。
- **"上游开了口子"不等于"我们接上了"，接上了也不等于"扫对了"。** 2026-11-06 实测：eco #153 的 `getCraftConfirmCpuTierGlyph()` 我先加在 `SimplifyTier`（4 只档位）上，守卫也只扫 `SimplifyTier.values()` ⇒ 232 条全绿，而玩家看到的是 EC0 —— 因为**合成确认页问的是 CPU 的 tier**，我们 F1 的并行核递出去的是 `SimplifyCraftingTier`（`SimplifyCraftingParallelCoreBlock.java:8`）。规矩：加覆写之前先顺着消费方找到"它问的到底是哪一个对象"；守卫必须扫**全部**实现（现在两个枚举都扫，并断言条数 ≥5，少一只就红）；控制组 = 删掉那只覆写 ⇒ 正好 1 条红、报 "L1 still answers eco's interface default (null)"。 **同日再补一层：接上了也不等于可达。** 覆写加对枚举、守卫也扩到两个枚举之后，客户端**仍然是 EC0** —— eco 的 mixin 不直接问 tier 对象，而是 `NERegistries.ECO_TIER.getId(cpu.getTier())` 转成 int、客户端再 `byId(int)` 转回来问。我们的 tier 是普通枚举、不在那张注册表里 ⇒ `getId` 给 **-1** ⇒ `getCraftConfirmCpuTierGlyph()` 一次都不会被调到。修法：`RegisterEvent` + `NERegistries.Keys.ECO_TIER` 把我们 5 只 tier 注册进去（`ef3d725`，公开 API；现量：除这条 mixin 与 LDLib2 的 accessor 包装外没人读这张表，且它 `sync(true)`、`maxId(256)`，我们的落在 id 3-7）。规矩再加两条：**判"接口方法有没有被调到"要照消费方的调用链判，不能照方法签名判**；**守卫必须走同一条链**（现在先断 `byId(getId(tier)) == tier` 再断 glyph）。控制组 = 短路那段发布 ⇒ 正好 1 条红、报 `getId=-1, byId=null`。 **同一条规矩也适用于"家族级"断言（11-06 第二次踩，同一个失败形状）**：`smallRungsFillToTheirSurchargedBytes` 抬 25% 时只扫物品/流体/化学三家，FE 与 omni 那五只 1K 留在 1024，套件全绿、玩家先看见 —— **断言扫的是我记得的家族，不是实际存在的家族**。落法：家族级断言要么从注册表/枚举把家族列出来，要么在末尾断"扫了几家"并与常量表对账；新增的 `reportsSurchargedBudget` 走引擎自报（`ECOStorageCells.getCellInventory(...).getTotalBytes()`），不回读我们自己的常量；omni 那几只进不了原来的灌满测试，因为那个助手从 `IBasicECOCellItem` 取每型开档价，而 eco 的 omni 细胞只 implements `IECOStorageCellItem`。控制组 = 量子 1K 退回 1024 ⇒ 正好 1 条红、报 "quantum 1k reports total=1024"。夹具：`buildL1Room`、`buildComputationStructure`、`buildTrinityShell`。

### 跑法

- 游戏侧：`./gradlew runGameTestServer`。**跑前删 `run-gametest/world`。**
- **跑前跑后都要按命令行数 GAMETEST 的 JVM**：gradle 任务退出 ≠ 游戏 JVM 退出。上一轮的尸体攥着 `run-gametest/world`，下一轮存盘改名就 `AccessDeniedException`，然后 Server 线程在 `ChunkMap.processUnloads` 空转（jstack 里 RUNNABLE 且 cpu≈elapsed）—— 10-14 一晚六轮冻全是这一个原因，与代码无关。判据二：AccessDenied 只出现在冻的轮，跑完的轮 0 条。
- **改完文件第一次跑常冻在任意进度**（见过跑到 3/4 卡满 timeout）。这不是代码：按命令行数字 java、杀掉**自己起的**那颗（连 wrapper 一起，TaskStop 之后 JVM 还活着）、删世界、重跑一次，仍挂两轮才去摘嫌疑项。
- 冻结也可能是网络：见过卡在 authlib 等 Mojang 会话服务器的 HTTP 响应上。看 `run-gametest/logs/latest.log` 的最后一条真实事件再判。
- `grep -c "@GameTest"` 会多 1（`@GameTestHolder` 也算）。
- 往 `run-gametest/mods` 放第三方 jar 之前，先读**它自己**的 required 依赖：一条缺失的 `ae2wtlib` 能打死整台服务器，且一条测试都不会跑。
- EMI 是 compileOnly ⇒ 它的页面永远没有套件面，唯一读数是在客户端逐条点开。

---

## 10. 出包与验证

- 出包 `./gradlew build -x test`。`test` 是**纯 JUnit**，不是游戏 JVM，两者要分开报。
- 被 gitignore 的 `resources` 照样会进 jar。
- 发布前工作树里"不是我改的"未提交资源改动会跟着进 jar，tag 复现不出来 —— 先解析成"哪半有视觉效果"再拍。
- 改 `neoforge.mods.toml` / lang 这类 **CRLF** 文件别用 `sed -i`（会把整份变成 LF）。
- **同一个 lang 目录里两份文件行尾可以不同**：现量 2026-11-06，`en_us.json` 是纯 LF（320 行）、`zh_cn.json` 是纯 CRLF（320 行）。所以按 `
` 切行的脚本在 en_us 上会把**整个文件当成一行**删掉；这次靠"写盘前先 `json.loads` 验一遍"当场拦住。改这类文件：先探测每个文件自己的行尾，按那个行尾拼回去，改完必须解析一次 JSON。

---

## 11. 资源与对外边界（CI 有闸）

- **`src/main/resources/assets` 的 JSON/MCMeta 里不许出现 `neoecoae:` 引用**（`.github/workflows/repository-checks.yml` 用 grep 拦，命中即红）。例外不是放宽资源文件规则，而是用代码注册小型 atlas source：#165 起 `WorkstationPaletteSource` 委托 eco 21.2.19 的公开 `WorkstationSpriteSource`，palette key 写成 `namespace:path` 后生成 sprite 落在该 namespace；六张不染色的共享工作站贴图由同一个 source 做本地命名空间别名。禁止在资源文件里点名上游路径，版本锁与来源闭包由 GameTest 检查。
- 同一份 CI 还跑 `git diff --check`。
- 第三方素材先量授权：LICENSE 常只覆盖代码，素材可能是署名第三人且无授权。
- **判"死贴图"的尺子要把四个引用根一起数**，11-17 那次每漏一个就误判一张：① java 里的字面量与拼名；② `assets/**/screens/*.json` 的 `"texture"`（`textures/gui/simplify_assembler.png` 就这样活着）；③ `data/neoecoprototype/curios/slots/*.json` 的 `"icon"`（`textures/slot/empty_universal_link_card.png`）；④ 随 jar 打包的内置回退资源包 `src/main/resources/legacy_art/` 自己的 blockstate/model（它引用改名前的 `block/aluminum_alloy_casing`；这条不挡住删除 —— 那份包是从 tag 切出来的自足切片，但它会让"闭包扫不到"这句话扫错对象）。同型坑：给标识符长度设下限会漏掉 `gui/tier/l1.png`（两张 tier 枚举的 `getCPUOverlayTexture()` 返回它）。

---

## 12. 本机工具坑

- `javap` / jar 工具在 `F:/java/bin`（PATH 里没有）。
- 反编译的 MC 源在 `build/moddev/artifacts/neoforge-<ver>-sources.jar`，比用 javap 猜包路径便宜；注意它只含被 NeoForge 打过的那部分。
- python 只吃 `C:/tmp` 下的路径，中文要 `-X utf8`；Git-Bash 里用 `os.sep` 拼路径会静默 0 拷贝。
- `core.autocrlf=true` ⇒ 行尾差异不等于文件被写坏。
- 像素/贴图结论一律用 Pillow，别用眼睛。
- PowerShell 脚本块会被 bash 吃掉 `$_`：写成 `.ps1` 用 `-File`，或者用不含 `$_` 的形式（如 `Format-Table ProcessId, CommandLine`）。
- **源码编码跟的是 Gradle daemon 那颗 JVM，不是 `java.toolchain`。** 本仓库 `build.gradle:15` 钉的是 toolchain 21，但没设 `compileJava.options.encoding`，于是 javac 按 daemon 的 `file.encoding` 解码源文件：拿 `JAVA_HOME=F:/jdk17 ./gradlew compileJava` 起一轮，中文 Windows 上就是 CP936 ⇒ **101 条"编码 GBK 的不可映射字符"，外加两条假的"未结束的字符串文字"**（刷出来的行号是 `SimplifyTrinityControllerBlockEntity:303` 与 `NeoECOPrototypeGameTests:2704`，都是含中文的行）。这副样子会被读成"上游改了签名，源码编不过了"。换 `JAVA_HOME=F:/java`（那是 **JDK 21.0.11**，JEP 400 起 `file.encoding` 默认 UTF-8；同一路径也是 §12 第一条那颗 `javap`）同一条命令就 0 错。**先确认 daemon 是哪颗 JDK，再决定要不要动源码或加 encoding。**
- **上一条不能反过来用**：那次全量重编里混着一条**真**错误——`无法将记录 Config 中的构造器 Config 应用到给定类型`，因为 eco 21.2.15-beta 把 `StorageHostActionUI$Config` 从 13 个分量改成 14 个，而**调用点有两个**（`SimplifyStorageHostBlockEntity:222` 与 `SimplifyTrinityControllerBlockEntity:354`），构造子级 ABI 扫描只看签名（1 条）不看调用点（2 处），所以"就一条"那句话的口径是签名。**读数顺序**：先摘 `无法将记录…应用到给定类型` / `错误: 无法将`，剩下的不可映射字符才是 shell 的账；一次只解一件事，别把整屏倒进"编码问题"。
- **daemon 按 JVM 参数复用，所以别用 `--stop` 清环境**：`./gradlew compileJava "-Dorg.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8"` 会**新起**一颗 daemon，不动已经在跑的那颗（IDE 常常正攥着它）。跑前跑后按命令行数 java 进程这条规矩仍然算数：204 条套件退出后 java 数与跑前一模一样（4 颗，全是编译期 daemon），说明游戏 JVM 自己走干净了。

---

## 13. 有些问题属于上游，不要在本仓库 workaround

提 issue / PR，别在这儿绕。**每份稿子一律先写精简版**（一句现象 + 带偏移的读数 + 请求 bullet）；
长证据留本地稿。发新稿之前先重跑一遍符号核对（§2 那三把尺）——这条线一天内就会漂。
上游一关单，当天就要报"交付形状 + 我们接了几行"，不能只报"已关"。

**请求台账（2026-11-16 用 GitHub API 现量，逐条带关闭时间与理由）：**

| # | 求什么 | 状态 | 交付形状 | 我们接了几行 |
|---|---|---|---|---|
| #127 | `IECOTier` 的 idle power / `getBuildDefinition` / 面板通道 | CLOSED 10-24 | 三条全落 21.2.10 | 撤 3 只 mixin；`ECOPowerProfiles` + 两只 tier 覆写 |
| #128 | 样板迁移源开关 | CLOSED 10-23 | `allowsPatternMigrationSource()`，默认 false | 落过又撤（见 §15 那行），现由守卫答 false |
| #136 | 迁移自匹配吞样板 | CLOSED(completed) 10-25 | 三条都在 21.2.11 落地 | **行为面没跑过**，开回去是四件事，见 §15 |
| #143 | 流体罐字段类型 | CLOSED 10-28 | `getFluidTankHandler()`（21.2.12 起两只仓都有） | 三处读全换 + `HatchTankAccessorContractTest` |
| #151 | 动作栏 ABI | CLOSED(completed) 11-04 | 正式版恢复 13 参便利构造子（[13,14] 并存） | 0 行——我们已按 14 参编 |
| #152 | 合成主机溢出档与并行阶梯 | **CLOSED 11-12，非作者关、无 commit** | **零交付**：`calculateOverclock` 在 21.2.16 与 21.2.17 逐字节相同（5,087 B / 102 行） | 0 行；**已拍（11-16）：F1 的并行阶梯照我们自己的数字定稿，这条线结掉**——不再等上游，也不按他们要的那样补一份完整公式重发 |
| #153 | 合成确认页 CPU 档位角标 | CLOSED(completed) 11-04 | 一条 default + 哨兵 0→-1 | 两只枚举覆写 + 把我们 5 只 tier 注册进 `NERegistries.ECO_TIER` |
| #154 | 单盘存储优先级 | CLOSED(completed) 11-05 | 驱动解析与 RPC 不再绑死 eco 的宿主 | 已接（偏移存盘上、挂载=主机+偏移） |
| #155 | 输入仓面板标题 | CLOSED(completed) 11-05 | **只落输入仓**：从方块取描述键 | 输入仓 0 行；输出仓那半是我们自己覆写 `createUI`（见 §15） |
| #156 | 元件工作台宿主 API | CLOSED(completed) 11-10 | `IECOConfigurableCellHost` + `IECOCellWorkbenchDrive`，21.2.16 是首个可安装件 | 两条 `implements` + 面板第五个动作 + 守卫 |
| #165 | 工作站 colormap 输出 namespace | CLOSED 11-19 07:41Z | 21.2.19 (`a0970206`) 接受 `namespace:path` palette key，并在对应命名空间输出 sprite | `WorkstationPaletteSource` 委托上游 source；删本地染色/动画副本，保留静态 alias + 守卫 |

**仍活着的三条约束：**

1. **驱动器满光 / `cullface` 这条不要再往上游提**——机制被证伪（`neoforge_data` 的 `block_light`/`sky_light` 不会让面自发光，两条渲染路径都重算并覆盖顶点 lightmap）。测量记录仍有效，见 `上游请求稿/NEO_ECO_AE_DRIVE_LIGHT_REPORT.md` 文件头（`上游请求稿/` 是本机目录，不进仓库）。
2. **输出仓标题**：eco 到 21.2.17 仍不给入口，我们那半边是自己覆写 `createUI` 顶上的，代价是一处会漂的手抄——所以 `FluidHatchPanelTitleContractTest` 里钉了"我们抄的那段与 eco 那段各读一遍比差值"。上游一补，那条守卫会红，届时**删覆写**而不是留。
3. **已撤回/作废的稿子不要再发**：`NEO_ECO_AE_TYPE_CAP.md`（10-21 我们自己砍了大宗标记）、`NEO_ECO_AE_CPU_LIST_REPORT.md`（21.2.9 的 `grid.NEMachineLookup` 已实现它请求的事）。

`上游请求稿/` 现在 **22 份**（2026-11-16 数，该目录 gitignore，只有本机看得到）。逐条的原始请求文字与当时读数在
[docs/upstream-asks-history.md](docs/upstream-asks-history.md)。

---

## 14. 命名

- Mixin 注入方法前缀 `neoecoae$` 或 `neoecoprototype$`，`@Unique` 字段同前缀。
- 桥类 `*Types` / `*Facade` / `*Bridge` / `*Support`；探针类 `*TestProbe`。
- 方块 id 只有在**与 eco 同名会撞**时才加 `simplify_` 前缀；没有 eco 对应物的内容用裸名（`meteorite_core`、`meteorite_dissociator`）。
- **物品与方块的显示名不许逐字抄上游 mod 已有的名字。** 判据是"玩家在 JEI/创造栏里读到的那句话"，不是 id：id 不同、模型不同、注册表不同，名字撞了就是一只物品穿在另一只身上，而且没有任何一处会报错。现量教训（2026-11-06）：`neoecoprototype:sky_stone_dust` 的 en 行写 "Sky Stone Dust"、zh 行写「陨石粉」，**两句都是 `ae2:sky_dust` 在那两种语言里的名字**，而同一张解离表同时发出 `ae2:sky_stone_block`（陨石）与这只同名不同物的粉。已按 reliqwq 决定整只删除、解离表与元件账单改用 `ae2:sky_dust`（该物品随 v1.4.5 发过，但 1.4.5 里解离机没有合成配方、它也不在创造栏 ⇒ 唯一来源是 `/give`，仍写进必读第 12 条）。尺子：`NoBorrowedItemNameTest` 拿我们 `item.*`/`block.*` 的**值**去比 AE2 与 eco jar 里的同名值（两个上游是硬依赖，从测试 classpath 读）——删除前每边各撞 1 条，删除后 0 条。**可选 mod（MEGA/OmniCells/AppliedFlux/Mekanistics）不在这把尺范围内**，测试 javadoc 明写了这一半没覆盖。

---

## 15. 已知陷阱清单（现状）

每行只写三样：**现在的行为/取向**、**钉住它的守卫名**、**撤销或重开时要动什么**。
完整的字节码推演与当时的控制组读数在 [docs/known-pitfalls-history.md](docs/known-pitfalls-history.md)。

| 位置 | 问题 | 状态 |
|---|---|---|
| LDLib2 的 `UIEvent.button` 编号 | 我按 GLFW 直觉把 `button == 1` 读成"中键"，实际 **1 = 右键**（GLFW：LEFT=0、RIGHT=1、MIDDLE=2），存储盘的动作菜单是右键点开 | 教训是方法不是数值：读上游 UI 手势先问"哪些值被处理"，再对实机，别按编号表猜 |
| `integration/beyond/` | 物品注册了、创造栏也进了，但 **0 配方** ⇒ 生存拿不到 | 待决：照 grette 先例（注册 + 不进栏）或补全。已定的一半：成就标签 `tags/item/storage_matrices.json` 不收它 |
| `BeyondStorageCellItem.EXTERNAL_TYPES` | `Integer.MAX_VALUE` 可能在 eco 内部溢出 | 未复核 |
| 装配室的压印推导 | 别的 mod 把 1:1 压印改写成批量（DE 的 `printed_silicon ×6`）⇒ `ProcessorAssemblerRecipes.derived()` 重推出已存在的产物，守卫 `processorRecipesDeriveFromInscriberMaterials` 红在"derivation re-added" | 已修（11-09）：只有 `count==1` 的压印进材料表，同一产物有两条 1:1 来源就整条丢弃。再遇到同类失败先看那条产物的压印配方是不是被改写成了批量 |
| 机器不跑时没有任何提示 | 缺电 / 缺流体 / 输出满在界面上不可见 | 待决要不要红字。上游口子已量（21.2.11）：`ECOProcessingStatusRegistry.register(...)`；**没量的**：`ECOProcessingProgress$Machine` 在 `crafting/display/processing`（不是 `api/`），附属能不能自己造样本未证 |
| L1 流体两只仓的面板标题 | 输入仓：eco 正式版起从方块取描述键 ⇒ 我们的键自动生效，附属 0 行。**输出仓：eco 到 21.2.17 仍写死 `block.neoecoae.output_hatch`**，所以那半边是我们自己覆写一行 `createUI`（`SimplifyFluidOutputHatchBlock`） | 手抄的漂移面由 `FluidHatchPanelTitleContractTest` 钉（工厂、两个方向旗、罐走 #143 访问器三项各比差值）。**上游补上同一条改动时第三条守卫会红 ⇒ 删覆写 + 删发版说明那句 caveat，不许两边都留** |
| 玩偶 CPU 走不走 eco 的快速规划 | 三道闸现量：状态挂在 `CraftingCpuLogic`（不是 `CraftingCPUCluster`）、提供方 `instanceof ECOFastPathDispatchProvider`、作业闸 `shouldUseFastPlanner() = isFastPlannerEnabled() && hasComputationHost()`。**`ECOExternalCpuSupport` 的两个口答的是能力不是质量**，别拿它的 true 当"会走快速规划" | 守卫 `formedComputationHostIsTheGateForEcosFastPlanner` 等（batch `doll_cpu`、`fast_planner_contract`）；仍未证：一次真作业确实由快速规划器算出（两把日志旗默认 0，要客户端开） |
| L1 装配室与 L1-EC9 的分工 | L1 只接受声明配方且每次一份；L1-EC9 接受声明与压印推导配方，并按服务端每周期上限批量执行 | 守卫 `derivedRecipesRunOnTheEc9Only`、`processorAssemblerRefusesPatternsAboveTheConfiguredBatchCap`、`processorAssemblerBatchCannotOverspend`、`processorAssemblerFluidOutputReachesTheGrid`；“显示用”格仍由 `assembler_ghost_write` 钉为拒绝手动写入（`mayPlace`/`mayPickup` 为 false） |
| `ECOClientUIBridgeMixin` / `FixedBlockEntityRenderersMixin` | 补的是上游缺的侧别检查与边界检查 | **保留、不撤**；每条的撤销条件写在各自类头（`javap` 可判），跟版按 §8 起客户端重读 |
| 解离表没有 JEI/EMI 页 | 玩家看不见一颗核心能出什么 | 待做 |
| L1 存储主机的"一键大宗标记" | 整块功能 10-21 已删（`autoMarkBulkCells` + 配置项） | **已结案，取向已定：我们不做大宗**。再看到"缺入口"的说法就是抄旧账 |
| 样板迁移源（#128 / #136） | beta2 的 eco 母线已覆写 `allowsPatternMigrationSource()` 答 true；我们的 F1 母线用只改这一答的子类保持 false，子系统迁移仍看不见附属母线里的样板 | **不开**：可写目标缓存与源池闸门收的是同一个 `PatternStorageHost`，附属一答 true 同一只容器两处都在，报告路径不排除来源格 ⇒ 自匹配 → `ALREADY_PRESENT` → 退 blank、清源、还 +1 计数。#136 三条上游 21.2.11 落了但**行为面我们一条没跑**。开回去要覆写返回 true、装守恒测试（草稿只在本机 `C:/tmp/repro_bus_migration_block.java.txt`，重开时按判据重写）、翻守卫 `addonPatternBusStaysOutOfTheMigrationSourcePool` |
| L1 存储盘交互 | beta2 删掉 `CellExtractor` / `CellInserter`，改由 `HostDiskMenu` 负责真实菜单的取出、放入、Shift 转移与占位拒绝；`StorageHostUI.create` 多收 tier/hostState/disks 三参 | 已接；守卫 `panelTakeFollowsTheShiftSplit`、`panelDropSpendsTheHeldCell`。**单颗 stack 必须保留传入引用**：上游 `DiskSlot.replace` 用 `getCellStack() == replacement` 判成功后才扣光标；改回无条件 `copyWithCount(1)` 会插入但不扣，两条守卫都红。清空仍用 `setCellStack(null)` |
| 存储盘/矩阵收不收 AE2 的卡 | 只由 AE2 的 `Upgrades` 关联表决定：不登记 = 上限 0，附属没有覆写点 | 现政策：带分区表的全族收模糊/反转；**销毁卡除六只小宗与无限源外全收**（11-06 拍）。扫描谓词必须是 `ICellWorkbenchItem` + `getConfigInventory != null`，用 eco 的 `IBasicECOCellItem` 会整族漏掉 omni。守卫 `everyPartitionedCellTakesTheFilterCardsAndVoidSkipsSmallBulk`，两个控制组：谓词改回 `IBasicECOCellItem` ⇒ 红的正好 omni 八只；去掉小宗前缀判断 ⇒ 红的正好小宗六只 |
| 存储主机面板的「配置」 | 曾点了没反应（eco 的 locator 把宿主绑死它自己的 BE） | 已接（#156 / `21aa9b1`）：两条 `implements` + 面板第五个动作。**GameTest 只到"locator 接受我们的安装"**——`FakePlayer.openMenu` 不换窗口，所以"窗口真的出现"只有客户端读数。**发版必读**：CHANGELOG 1.4.9 第 14 条那句"点了没反应、已提 #156"要整条翻成"现在能打开元件工作台"，不许只加不删 |
| ECOP 频道核心的升级槽 | `AppEngInternalInventory` 有过滤器，但把 `toContainer()` 包进普通 `Slot` 后，真实菜单的 `mayPlace` 恒为 true，点击会绕过过滤器 | 已修：UI 必须用 `module.boosterSlot(index)`，其 `mayPlace` 委托 `boosters.isItemValid`。守卫 `onlyFourWirelessBoostersFillTheBoosterColumn` 与 `WirelessModuleBoosterDeckTest`；控制组把 `mayPlace` 改成 `return true` ⇒ 正好该 GameTest 红在"accepted an arbitrary stone" |
| 盈能 ME 箱子重进世界或离线时不渲染矩阵 | AE2 的 `getUpdateTag()` 走 `writeToStream/readFromStream`，不会把 `saveAdditional()` 写进 `"cell"` 的 NBT 发给客户端；固定渲染器还会把矩阵烘进区块网格，普通 BE 同步不会稳定重建它 | 更新包同步矩阵物品 ID；矩阵改由已注册的动态 BER 每帧读取并绘制，eco 矩阵用自己的模型，其他元件用完整 L1 物品矩阵模型，不再依赖邻块更新或资源重载。离线供电按“节点供电或缓存足够”判定，`serverTick` 每 tick 刷新显示；节点销毁后不再改初始化数据。守卫 `clientUpdateCarriesInstalledCellItem`、`networkChannelPriorityAndUnreadyMountWork`、`clientCellRenderingReadsTheSyncedCellEveryFrame`、`chestRegistersItsDynamicCellRendererAndGuidePage`；控制组分别把写入 ID 改成 0、摘掉动态 `renderCell`、去掉缓存供电或每 tick 刷新，各红对应断言 |
| 通讯核心塔 | 基座、塔身、范围增幅与高效通讯模块暂时隐藏；物品、方块、模型和战利品仍注册以兼容旧存档 | 守卫 `communicationTowerModulesStayWithheld`：创造栏无、配方结果扫描无、四件注册仍在。恢复时把 `_removed_sources/` 四份配方移回 active recipe 目录、恢复两条 `output.accept`、补回双语指南的 `RecipeFor` 并更新 `docs/配方总览.md` |
| 「工作站」模块卡的外观 | eco 21.2.16 重画 `working_station/controller_formed/`；21.2.19/#165 允许完整 namespace palette key，染色与双帧动画由 eco 公共 source 生成 | 已接：`WorkstationPaletteSource` 委托上游 source，模型用 `neoecoprototype:l1` sprite；静态共享 sheet 在运行期别名到本地 namespace。旧重烤脚本和预烤图已备份至 `贴图工作区/工作站colormap迁移_2026-11-19/` 后删除；守卫 `ourWorkstationModuleUnlocksOurOwnRecipes` 与 `shippedAssetReferencesResolve` 检查模型槽、palette 与 atlas 来源。

**下面这些已经修了，别再照旧说法改一遍**（细节在 [docs/known-pitfalls-history.md](docs/known-pitfalls-history.md)）：

- `KJSPlugin.registerBuilderTypes` 的类级闸 → 无条件注册 + `ChemicalMatrixSupport` / `OmniMatrixSupport`；`storage_matrix` 不再绑死 OmniCells。
- `SimplifyStorageHostBlockEntity` 的 `panelRowOf` / `createStorageTypeLines`：OmniCells 引用被**同一个方法内**的 `ModList.isLoaded` 包住，实测够用（§4.2）。
- L1 高密度能量单元重进世界掉电 → 已实现 `ISyncPersistRPCBlockEntity` 并落盘。
- `SimplifyCraftingClusterCalculator.matchingParallelCore` → 我们那颗私有静态改名 `matchingOwnParallelCore`（查的是自家方块集，不是上游几何路径的覆写；同名同签名会被 javac 判成遮蔽实例方法）。
- **2026-10-25 撤掉的三条 mixin 不是"换个写法"**：idle power 那条有实测陷阱——`getComponentIdlePower()` 只在 `role==COMPONENT && tier!=null` 时替换基数，而我们 32 台机器里 **20 台 BE 的 `tier==null`**，单靠 tier 覆写会把它们放回 eco 的 16.0。落法 = `ECOPowerProfiles` 命名空间 provider + 两只 tier 覆写；尺子 `addonMachineIdlePowerInventory`（每台打一行 `beClass | tier | idle`，改法前后 32 行逐行相同才算等价）。
- 计算系统的成型定义走 `ECOMultiblockDefinitions` 的 provider，**别改回 tier 覆写**：那会把引着 `registration.ModRegistration` 的 `SimplifyComputationDefinition` 拖进 `api/`（§3 精神禁）。
- 驱动器缆线渲染走 `ECOComputationModels.registerCableModel(...)`，mixin 已撤；这条**只有客户端能验**。

---

## 16. 发布说明与 CHANGELOG 的写法

`CHANGELOG.md` 里每一节的形状，以及 GitHub Release 的正文（正文就是从这一节截出去的），一律按下面这个顺序写，**一条只说一件事，读完就知道跟自己有没有关系**。

1. **本版重点** —— 一句话，说这一版干了什么正事。可以带人话（"更新完成美术部分"），不要带内部计数。
2. **⚠️ 升级前必读** —— 编号短句，每条都是玩家会自己撞上的事实：依赖版本不升就起不来、改名的东西要备份数据、被关掉的功能怎么打开、哪些内容默认不进世界、机器/配方的上限是多少。
3. **新增** / **变更** / **修复** / **移除** —— 编号一条一事实。允许在句子里带半句"为什么"（例如"装配室的配方至少要三个原料，所以按四份写"），但只写玩家用得上的那种为什么。
4. **依赖** —— 两三行，硬依赖写清版本地板，可选依赖写清它卡的是哪一族内容。

不要写进去的东西：

- 配方数 / 方块数 / 测试条数这类**内部统计**（会漂，也没人据此行动）。
- 版本区间是**怎么推出来的**、mixin 打在哪、javap 量到什么 —— 这些进 `docs/` 与 `进度/STATUS_MATRIX.md`（`进度/` 是本机目录，不进仓库）。
- 自我叙事（"我们这次终于……"、"上一版的锅……"）。
- 中英并排双语正文。`CHANGELOG.md` 单语中文；`README.md` 纯英文、`README_ZH_CN.md` 纯中文。
- 小标题下的多级嵌套和长段落。一条就是一行。

破坏性的东西**永远排在最前面**（`⚠️ 升级前必读` 那一节），不要藏在"变更"里。

---

## 17. 贴图交接一律进 `贴图工作区/`

- 我生成、导出、或交给美术重画的**所有图片**（面板底图、按钮图集、参考图、比稿、换色预览）一律放进 `贴图工作区/` 下的主题子文件夹，命名带日期（如 `录制机面板_2026-11-10/`），**不再放桌面**。该目录已 gitignore，不进仓库也不进 jar。
- 交图时同时给尺寸与坐标/色板规格（资源守卫会断言尺寸）；美术按同尺寸回稿后，我再替换进 `src/main/resources/assets/neoecoprototype/` 并跑守卫 + 开客户端验收。
- 桌面只作美术自己回稿的临时落点；我主动产出的图不落桌面。
