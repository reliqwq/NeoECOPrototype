# AE2 扩展开发经验

这份文档记录 Neo ECO Prototype 在 Minecraft 1.21.1、NeoForge 21.1.x、AE2 19.2.x 中扩展接口、Part、菜单和存储设备时的稳定做法。

## 设计边界

- 优先复用 AE2 的 `InterfaceLogic`、`PatternProviderLogic`、`InterfaceMenu`、`InterfaceScreen`、`UpgradeableMenu`、`PartItem` 和 `IPassiveEnergyGenerator`。
- 普通 AE2 接口、普通 AE2 线缆 Part 和 AE2 全局资源不能被覆盖。自定义界面资源必须放在本模组自己的路径，例如 `assets/ae2/screens/neoecoprototype/`。
- 只有专用菜单注册专用 screen；不要通过修改全局 `interface.json` 或全局纹理来修一个 L1 变体。
- 真实交互由菜单槽位和逻辑决定，背景贴图只表达状态。任何贴图上的箭头、凹槽都必须和真实槽位逐项核对。

## 接口槽位布局

AE2 `InterfaceMenu` 先创建所有 `CONFIG` 槽，再创建所有 `STORAGE` 槽。18 槽接口因此不是交错创建，而是两个连续的 9 槽组。库存量按钮只绑定 `CONFIG` 槽。

L1 供电接口采用以下六行视觉布局：

| 视觉行 | y | 内容 | 可交互 |
|---|---:|---|---|
| 0 | 35 | 第一组库存量按钮 | 按钮 |
| 1 | 53 | 第一组标记槽，带箭头 | CONFIG |
| 2 | 71 | 第一组输出槽，无箭头 | STORAGE |
| 3 | 89 | 第二组库存量按钮 | 按钮 |
| 4 | 107 | 第二组标记槽，带箭头 | CONFIG |
| 5 | 125 | 第二组输出槽，无箭头 | STORAGE |

AE2 的样式网格只能直接描述两个连续的网格。L1 专用 screen 在 `super.init()` 完成 AE2 定位后，将 CONFIG 和 STORAGE 的索引 `9..17` 槽位各下移 36 像素。这个调整必须限定在 `PoweredInterfaceScreen`，不能注入普通 AE2 screen。

修改 GUI 时必须同时检查三件事：

1. 菜单真实槽位的 `x/y`；
2. `InterfaceScreen` 创建的 amount button 所绑定的 CONFIG 索引；
3. 背景 PNG 的凹槽、箭头和标题位置。

## AE 卡与升级库存

`InterfaceLogic` 自带 AE2 升级库存，`InterfaceMenu` 也继承 `UpgradeableMenu`，但新方块或新 Part 不会自动继承 AE2 的升级卡白名单。需要在 common setup 中显式注册：

```java
var group = GuiText.Interface.getTranslationKey();
Upgrades.add(AEItems.CRAFTING_CARD.asItem(), myBlockItem, 1, group);
Upgrades.add(AEItems.FUZZY_CARD.asItem(), myBlockItem, 1, group);
```

方块和 Part 是两个不同的 `ItemLike`，必须分别登记。登记表同时影响升级槽放行和卡片 tooltip。不要为了方便把所有 AE 卡都注册给设备；只登记逻辑真正支持的卡。

## 供电接口

AE2 被动发电机是网络级竞争关系：一个网络通常只选择一个被动发电源，其他发电源会被设置为 suppressed，`getRate()` 返回 0。这是运行时选择，不代表 Part 或方块失效。L1 供电接口应实现 AE2 的被动发电 API，并在 suppressed 状态下返回 0，默认输出约 200 AE/t。

## Part 模型与资源

AE2 Part 不是普通方块模型。保留 AE2 的 Part 几何、背面和侧面语义，只替换需要定制的正面与状态覆盖层。不要用一个完整立方体模型替代 Part，否则会破坏线缆连接方向、厚度和 overlay 行为。

资源命名建议：

- 专用 screen：`assets/ae2/screens/<modid>/...`
- 专用 GUI 纹理：`assets/ae2/textures/guis/<modid>/...`
- 自有模型、方块状态和语言：`assets/<modid>/...`

## 配置边界

适合 ModConfig 的内容：默认槽位数量（如果产品需求允许改变）、功率、最大结构长度、额外可接受的存储元件白名单和服务器侧性能开关。

不适合配置的内容：槽位语义顺序、CONFIG/STORAGE 的交错规则、amount button 与 CONFIG 索引的映射、Mixin 作用范围、Part 几何和 GUI 像素坐标。这些属于菜单契约或资源契约，应固定在代码和资源中，避免服务器配置制造不可交互的界面。

## 方块默认状态：`StateDefinition.any()` 给布尔的是 `true`

1.2.11 那条"手放主机闪一下成型面"的根因留在开发文档里，不进 CHANGELOG：

- `registerDefaultState(getStateDefinition().any().setValue(X, false))` 是个陷阱——`any()` 给每个属性的是它值列表的**第一个值**，布尔量就是 `true`。于是一台主机的默认状态自带 `formed / mirrored / network_switch / high_energy_network_switch` 四个真旗标，落地那一帧显示成成型面，集群下一帧才纠正。
- **判据指纹**：落地状态里唯一为 `false` 的布尔，恰好是我们自己 `.setValue(...)` 写的那一个——其余都是 `any()` 送的。
- 正确写法是用父类注册好的 `defaultBlockState()`（F1 合成主机一直这么做，所以它从来不闪）。1.2.7 的主机方块是 6 行空壳、根本没有这个覆盖，所以这条回归是 1.2.8 加 `communication_interface` 时带进来的。
- 守卫：**我们注册的任何一个方块，默认状态里不许有布尔量为 `true`**（常驻，不是一次性验证）。

## AE2 部件模型注册必须在 common setup，不能指望跑在冻结之前

1.2.9.1 那条启动崩溃的机制：

- `PartModels.registerModels(...)` 在集合被冻结后直接抛 `Cannot register models after the pre-initialization phase!`。
- **冻结是惰性发生的**：AE2 自己的 `ModelEvent.RegisterAdditional` 处理器，或任何一次 `CableBusModel.getDependencies()` 解析模型，都会冻结它。
- NeoForge **并行派发 setup 事件**（崩溃报告里就是 `ModLoader.dispatchParallelEvent`），所以"我们跑在冻结之前"从来不是保证，只是开发环境里抢跑赢了；包里有 `quick-pack` / RenderJS / KubeJS 这类会提前碰模型的模组时就会输。
- 落点：注册放 `FMLCommonSetupEvent`——所有模组的 common setup 必然早于任何模组的 client setup，也就必然早于任何模型解析，这是生命周期顺序给的保证。旁证：MegaCells 在注册表定义期注册部件模型，ExtendedAE 用更早的专用处理器，没有人在 client setup 做这件事。

## 计算家族的"成型后全藏机壳"是承重的，改不动

- `NEComputationCluster.hideAllCasingsWhenFormed()` 在发布 jar 里编的是 `iconst_1`（恒真）；eco 的存储簇/合成簇走的是另一条——`getCasingHideOrigin()` + 距主机 `distSqr <= 3`，只藏够得着的。
- 试过把计算簇也换成"只藏够得着的"（簇子类 + 一个注到 `createCluster` 的 mixin，并且有"不生效就会红"的测试证明它确实生效了）。结果：**远端那列机壳画成一整块普通立方，和散热控制器成型模型外伸的面板共面 → 闪烁 + 贴图不对**。
- 根因值得记住：**两家的机壳 blockstate 都只有一条通配键、只有一个模型，`formed` / `invisible` 根本不参与选模型**。所以"藏"这件事一旦改成部分藏，露出来的那几格就没有成型态模型可画。
- 真要修的顺序：先给远端机壳一个成型后可看的模型（美术活），或者让散热控制器的成型模型不伸进那一格。二者都没有之前，这条保持现状。

## AE2 的机器表按**精确类**登记，子类主机会掉出去

- `Grid.add` 里只有一个 `put`，键就是 `node.getOwner().getClass()`，**不沿父类链登记**；而 eco 收集计算集群用 `getMachines(ECOComputationSystemBlockEntity.class)` —— 是 Map 的键相等查找，不是 `instanceof`。
- **同一台主机、同一个 grid 换四个键实测**：按 eco 的类查 = 0，按我们的子类查 = 1，再往上的两层父类查 = 0。所以"继承 eco 的方块实体类"这件事会让主机从 AE2 的 CPU 列表里消失，而编译和运行都不报错。
- 结论形状：**要留在 AE2 机器表里的方块实体，类必须是上游那个本身**，不能是子类。要改变量就改在别处——我们的两条（发布 `communication_interface`、把 eco 的两个交换布尔钉成 `false`）走 `publishShape()` + 方块自己的 scheduled tick。
- **写方块不能发生在 AE2 的集群重算栈里**（那栈会在重算途中读写世界），这也是不能顺手在 `getBuildDefinition` 里写状态的原因。
- 附带账：退回 eco 的类之后，eco 在节点入网时按同一个类读的两行「快速规划 / 循环规划」开关也会在我们主机上生效——这是**收益**，但要知道它因此变了。

## 镜像模型：几何可以翻，动画的 uv 不能翻

- `controller_formed_base_mirrored` 把两张动画贴图（`#coolant`、`#screen`）按 `u0 > u1` 采样，等于把动图水平翻转；**翻转一张转动的图就是反转转向**，所以镜像机与正装机反转不同向。
- 修法：几何与静态底图继续镜像，**8 个动画面改用正装机那套 uv**。
- 这条是**故意偏离 eco**，证据要留全：成型面底图与 eco 差 0 像素、55 个面连同 uv 全等、11 张动画 `.mcmeta` 全同、`mirrored` 判定与 eco 发布 jar 字节码同序——eco 自己的 C4 镜像机同样会反，改判是他拍板的"镜像机也该正转"。
- 散热控制器的 mirrored 模型有同一处翻转，但它是另画的一套几何（面板从 z=2/30 挪到 z=14/-14、north/south 互换），要逐面重新配对，**没跟着改**。

## `variants` 的键是子集匹配：旧副本不会隐形

主机 blockstate 加新键时，**没写的属性走通配**，所以 1.2.7 那份 16 条键照样覆盖当前的 32 个状态、不重不漏，只是表达不出新的区分。1.2.8 的发版说明里"谁覆写过这三份主机文件，他的副本缺新键 → 方块直接隐形"是**错的**，已作废；改属性时不用担心老副本隐形，要担心的是"区分表达不出来"。

## 多方块几何：注入换成上游登记口之后

计算与合成两条几何注入换成 `NEBlockEntity.registerCalculatorFactory(...)` 之后，形状记在这里，别再去 CHANGELOG 里找：

- **各只登记主机一个类型**，不登记成员。理由是上游 21.2.1 的两个计算器都有"把检查转给范围内那台主机的计算器"的路由（`NE{Computation,Crafting}ClusterCalculator#controllerCalculator`），成员方块自己的检查会转过去。合成侧是 javap 看见那个方法确实在那儿才用的，不是从计算侧推出来的。
- **`publishShape` 的 `scheduleTick` 会不会形成重算反馈环：否证了。** 同一 tick 内 160 次几何检查 + 160 次 `publishShape`，装不装这条改动数字一模一样。
- **守卫的强弱两边不一样，要分开说**：计算侧有机壳守卫 `computationCasingCalculatorKnowsL1Geometry`（问的就是机壳自己的计算器，只有走新路由才为真）；**合成侧没有等价守卫**，靠的是既有的 F1 成型测试。所以"合成侧路由断了"这件事现在不会红。
- **F1 主机的 `getBuildDefinition` / `onReady` 已从注入变成 `SimplifyCraftingSystemBlockEntity` 上的普通覆写**（两者在 21.2.1 里都是 public，而 F1 本来就故意是自己的子类）。
- **还能再往前一步**：把覆写从 `verifyInternalStructure` 缩到上游新增的 `protected verifyStructure(...)`，那样 `setMirrored` / 冷却控制器 / `network_switch` 的写回就交回上游。我们现在必须自己写，因为检查一旦路由给我们，上游那半段不跑。
- **mixin 实盘形状**：7 个类（`mixins.json` 里 5 server + 2 client），注解级是 5 个 `@Inject` + 1 个 `@Redirect`，另有 3 个 `@Accessor` + 4 个 `@Invoker`。其中 `InterfaceLogicAccess` 与 `SlotYAccessor` 是纯访问器、没有注入点。旧条目里"从 16 条降到 7 条"的那个 16 是 PR 合并前的数，**别再引用**。

## 拒收是静默的时候，先加一行日志再读代码

装配室曾经拒收"重复同一种原料"与"批量"的样板，而玩家看到的是"材料不动"。根因是 AE2 的 `IInput.multiplier` 一词两义——既表示槽内重复份数，又表示整张样板的批量倍数——区分它的是样板自己的输出量；只读数量会误拒，直接乘进去会造出 64 倍回归，修法是按输出量归一化。

**这条真正的教训是过程**：拒收本身不打日志，我对着代码猜了三轮全错；在推送点加一行 WARN 之后一轮就定死了。现在那行日志留着，并且"每台机器每个输出只报一次"（供应器会每 tick 重试，不压住就是刷屏）。**判据：静默拒绝的路径上，先补可观测再谈推理；能打印上下文的地方不要靠读码。**

## 玩家向改写之后，留在开发文档里的实现锚点

CHANGELOG 现在只写玩家看得见的结果。这一节收着那些"以后改回来要用"的类名、方法名与原因，别去 changelog 找：

- **小宗手动过滤器面板的删除范围**：`SimplifyStorageMegaPanelUI` 一个类、主机侧 7 个方法 + 2 个 `@Persisted` 字段（选中的驱动器 / 页码）、驱动器侧 3 个方法（含 `setSmallBulkFilter` 那条 `@RPCMethod`）。标记能力没少——AE2 自己的盘配置界面和主机"一键标记"写的是**同一份 `getConfigInventory`**，路径不经过被删掉的那些方法；旧存档里那两个 `@Persisted` 字段被直接忽略，不报错、不炸档。
- **驱动器 tooltip 那行图标的形状**：先 `IElementHelper.smallItem(stack)`，再用 `append` 接一条"数量 × 名字"的 Component。`ITooltip.add` 会**另起一行**，所以名字必须走 `append`；`ItemStackElement.text` 只喂 `renderGuiItemDecorations`（数字角标），永远画不出名字。
- **玩偶掉落的两个入口**：生存挖掘靠战利品表里的 `copy_components`（`source = block_entity`），创造中键靠覆写 `getCloneItemStack`。原版那个方法不会去问方块实体要组件，只有蜂箱/潜影盒那几个自己做了这件事——所以"放下是好的、拿回来才坏"。
- **创造栏玩偶的默认皮肤**：profile 只带名字、id 为空时，原版会把四个名字解析成**同一张**兜底皮肤。现在按 `OfflinePlayer:<名字>` 生成离线 UUID（与单机离线玩家同一算法）。真人皮肤仍然只走 `/prototypefumo`——那条走原版 session 解析、会联网，建创造标签页时不能阻塞在上面。
- **JEI 转移按钮必须自己写**：`ae2jeiintegration` 给样板编码终端注册的是一个**通用**处理器，认不出我们自定义的配方类型；而能把终端填进去的只有 AE2 内部的 `EncodingHelper`。

## 版本下限怎么定

`neoforge.mods.toml` 里 eco 的下限写成 `[21.2.1-beta2,)`，不是 `[21.2.1,)`。原因：`21.2.1-beta1` 把自己声明成 plain `21.2.1`，所以宽范围恰好收下了它；`beta2` 声明真实版本，而预发布版本比较时排在正式版**之下**，于是 `[21.2.1,)` 直接拒绝启动。上游至今没有发布 `21.2.1` 正式版，这就是第一个带上它的构建只能是预发布版的原因。我们要求这个下限的实际内容是：`gui.GuiTitleProvider` 在 `21.2.0` 里不存在，而我们的通讯接口与样板总线方块实现了它。

AE2 的下限从始至终是 `[19.2.17,)`，现在也是编译所依据的版本。上游自己在 `21.2.1-beta1` 把它的 AE2 下限抬到 `19.2.18`、在 `beta2` 又退回 `19.2.17`，所以从 `beta2` 起，对 AE2 唯一的闸就是我们这一条声明。依据是对两个 jar 的逐类比对：本模组点名的 AE2 类里只有一个在两者之间发生变化——`core.localization.Tooltips`——而 `19.2.18` 从它里面去掉的是三个我们从未点名的字段，所以 `19.2.18` 的玩家同样被覆盖。

比对方法记在这里，别重复踩：类清单要取常量池里被引用的 `appeng` 宿主类（源码 grep 会漏掉 `var` 推出来的类型），只比 entry name 不算证明——`21.2.1` 的两代 jar 里 0 个类被删除，但 230 个类的字节变了。

## 多方块朝向与镜像

处理“某一格才接受指定成员”或网络交换模块位置时，先读取 eco 自己的实现，再读取 AE2，最后才参考原版。eco 的正规入口是 `NENetworkSwitchUtil.switchPosition`，不要用 `front.getCounterClockWise()` 等手工推导替代它。

- 使用 `OrientationStrategies.horizontalFacing()` 获取 `IOrientationStrategy`。
- 使用 `strategy.getSide(controllerState, RelativeSide.LEFT/RIGHT)` 获取玩家视角下的左右方向。
- 非镜像位置使用 `RelativeSide.RIGHT`，镜像位置使用 `RelativeSide.LEFT`；这与 eco 的 `switchPosition(controllerPos, state, mirrored)` 同构。
- 位置判断和结构校验必须同时传递 `mirrored`，否则镜像结构会把合法成员判到错误的一侧。
- GameTest 中 `helper.assertTrue` 只记录失败，不会中断后续代码。异步测试遇到前置失败时必须使用 `helper.fail(...); return;`，否则继续调用 `succeed()` 可能让批次静默卡住。

## 验证清单

- `compileJava processResources` 通过。
- 普通 AE2 接口和普通 AE2 screen 未被全局资源覆盖。
- L1 方块和 Part 的 CONFIG/STORAGE 槽位数量与实际逻辑一致。
- 第一组和第二组 amount button 都只打开对应 CONFIG 槽的库存量菜单。
- 输出行没有箭头贴图。
- AE 卡能插入、保存、重新打开，并且不支持的卡被拒绝。
- 只有 L1 设备获得额外被动供电；普通 AE2 设备行为不变。
- 至少启动一次客户端检查 Mixin、资源和菜单注册日志。
