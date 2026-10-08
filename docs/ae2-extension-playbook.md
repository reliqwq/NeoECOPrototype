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

**第四个参数是分组键，不是可有可无的装饰。** `Upgrades.add(card, machine, max)` 这个三参重载传的是 `null`，而 `createTooltipLinesForCard` 的处理是：同一个组名的机器塌成**一行组名**，组名已经出现过就整条跳过；没有组的机器**各打自己的显示名**。我们的机器名和上游不同，所以少传这个参数就会在别人的卡片上多出几行。appflux 的感应卡用的两个组是 `GuiText.Interface.getTranslationKey()`（接口）和字面量 `"group.pattern_provider.name"`（样板供应器，它没有常量）。放行照旧生效，但**放行与占行是同一张表的两个后果**，改的时候两样都要看。

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
- **mixin 实盘形状**：7 个类（`mixins.json` 里 4 server + 3 client），注解级是 3 个 `@Inject` + 4 个 `@Redirect`，另有 3 个 `@Accessor` + 4 个 `@Invoker`。其中 `InterfaceLogicAccess`（2 访问器 + 4 invoker）与 `SlotYAccessor` 是纯访问器、没有注入点。旧条目里"从 16 条降到 7 条"的那个 16 是 PR 合并前的数，**别再引用**。
- **合成侧的功耗已经不靠注入**：`ECOCraftingHighPowerMixin` 删掉了，三块合成成员（worker / parallel core / vent）改成我们自己的子类，`onReady()` 里 `super.onReady()` 之后写 `SimplifyPowerProfile.L1.highIdleComponentPower()`。**注册面一个字没改**：`BlockEntityType` 声明的泛型仍是 eco 的父类，只有工厂 lambda 里 `new` 的类换了，`AEBaseEntityBlock#setBlockEntity` 传的仍是父类的 class —— 与 `6a00d02` 给 F1 主机用的是同一个形状。守卫是 `craftingMembersIdleAtTheAddonRate`：eco 自己三块都写 64.0 AE/t，我们写 **4.0**（`highIdleComponentPower` 的 32.0 也过 1/8 缩放），摘掉任何一块的覆写，那条就点名到具体方块。
- **同一招还能往下拆**：`NEBlockEntityPowerMixin` 现在管着其余仍复用 eco BE 类的方块（计算侧 threading / parallel / cooling / transmitter、pattern bus、两个流体孔；注册表里一共挂着 12 个 eco 的 BE 类型）。要不要拆看成本，但**每拆一块都得先有读数**——那三块成员在补断言之前是零覆盖，也就是注入坏了没人知道。

## EMI 的多方块工作台清单是上游硬编码的，我们的主机要自己登记

`NeoECOAEEmiPlugin.register()` 干两件事：按名字把 eco 自己的九台控制器（`NEBlocks.{STORAGE,CRAFTING,COMPUTATION}_SYSTEM_L{4,6,9}`）登记成 `MULTIBLOCK` 分类的工作台，然后无条件走一遍 `NEMultiBlocks.DEFINITIONS` 加结构配方。前一半不是白送的（`NeoECOPrototypeEmiPlugin` 补三行 `addWorkstation`），后一半要写对重载才是白送的。

- **`Builder` 有两个 `create`，只有一个进那张表**：`create()` 构造完 `NEMultiBlocks.DEFINITIONS.add(def)`；`create(Consumer)` 只 `consumer.accept(def)`，**不加**。我们三条 L1 定义从首发（`8bfed03`）起一直用的是 `.create(definition -> {})`，理由只记成一句"勿污染 eco DEFINITIONS"（`进度/SESSION_SUMMARY_2026-09-05.md:47`），于是两个百科页都翻不到我们的结构。2026-10-06 改成 `create()`。
- **那张表在 eco 里只有两个读者**：`integration/jei/categories/multiblock/MultiBlockInfoCategory` 与 `integration/emi/NeoECOAEEmiPlugin`（外加 `Builder` 自己那行 add、`NEMultiBlocks` 那行声明，全 jar 就这四个 class）。机器成型走 BE 的 `getBuildDefinition()`，不扫这张表——所以"进去会污染功能"这个担心没有对应的代码，进去的代价只是百科页多出三条。
- **Trinity 反过来，故意留在 `create(Consumer)` 上**：那就是"Trinity 不进 JEI/EMI"的真正实现处，比 `TRINITY_VISIBLE_IN_JEI` 那个开关更底层。别顺手把它也改成 `create()`。
- **不依赖插件顺序**：`EmiRegistry.addWorkstation` 转给 `EmiRecipes.addWorkstation`，键是分类**对象**本身，不查注册表名字。这跟 JEI 那次"按名字要 eco 的分类"是两回事，那种写法会随插件顺序炸（2026-10-06 实测过，见 `NeoECOPrototypeJeiPlugin` 里那段注释）。
- **发现机制是注解扫描**：EMI 在 NeoForge 上从 `ModList.getAllScanData()` 里挑 `Ldev/emi/emi/api/EmiEntrypoint;`（CLASS 保留，ASM 读得到），自己 `Class.forName`。所以没装 EMI 的环境根本不会构造我们这个类，`compileOnly` 就够；产物里引用 `dev/emi` 的 class 只有这一个。
- **没有 COOLING 工作台**：上游还把 `COOLING` 登记在它三台合成主机上，我们没跟。两条证据：全仓对 `NERecipeTypes.COOLING` / `CoolingRecipe` / `ECOCraftingCoolingController` 0 引用，并且我们既不注册合成侧的冷却控制器方块、F1 结构的成员表里也没有它（只有散热口 `SIMPLIFY_CRAFTING_VENT_BLOCK`）。所以给 F1 登记冷却工作台会是假承诺。
- **套件覆盖是 0**：`gameTestServer` 里没有 EMI，这条只能起客户端看。别把"编译过了"当成"页出来了"。

## 自己写一个 EMI 页面（L1 装配室，2026-10-07）

签名全部在 `libs/emi-1.1.24+1.21.1+neoforge.jar` 上 javap 过，别照 JEI 的形状套：

- `EmiRegistry` **只有 `addRecipe(EmiRecipe)`**，没有收 List 的 `addRecipes`；分类是 `addCategory(EmiRecipeCategory)`，工作台 `addWorkstation(category, EmiIngredient)`。
- 分类构造是 `EmiRecipeCategory(ResourceLocation, EmiRenderable)`，而 `EmiStack` 自己就 implements `EmiRenderable` ⇒ 图标一行 `EmiStack.of(我们的物品)`。
- **`EmiIngredient.of(Ingredient)` 存在**（还有 `of(TagKey<T>)` / `of(List, long)`）⇒ 原版原料直接转，**标签不会摊成 N 个格子**。别自己写 `getItems()` 循环。
- 现成的基类是 `BasicEmiRecipe(category, id, width, height)`（eco 自己的工作间页就继承它），`getInputs/getOutputs/getDisplayWidth...` 都替实现了，**但 `addWidgets(WidgetHolder)` 仍然是抽象的**，版面得自己画（`widgets.addSlot(EmiIngredient,x,y)`、`addTexture(EmiTexture.EMPTY_ARROW,x,y)`）。用之前先确认它在构造里给 `inputs/outputs` 建了列表，再往里 `add`。
- `getBackingRecipe()` 是有默认实现的：JSON 配方给真 `RecipeHolder`，**我们那种"从冲压器派生"的配方没有 holder，就返回 null**，别造一个假的。
- **和 JEI 必须读同一套规则**：我们的装配室配方 = JSON 配方 + 配置开启时派生的一批 − `isProcessorRecipeDisabled` 过滤。两边不一致就会被当成"配方丢了"，而且派生那一半在 recipe manager 里根本不存在，只能照抄 JEI 侧的构造过程。
- 拿读数的办法（EMI 不在套件运行期）：起客户端，日志里要看到 `[EMI] Baked recipes after reload in Nms` + `Reloaded EMI in Nms`，再把 `Exception` 命中行**逐条点开**确认没有一条来自我们的类 —— 这条只证明"注册没炸"，页面排布仍需眼睛。


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

`neoforge.mods.toml` 里 eco 的下限写成 `[21.2.1-beta2,)`，不是 `[21.2.1,)`。原因：`21.2.1-beta1` 把自己声明成 plain `21.2.1`，所以宽范围恰好收下了它；`beta2` 声明真实版本，而预发布版本比较时排在正式版**之下**，于是 `[21.2.1,)` 直接拒绝启动（实测过那句报错）。正式版 `21.2.1` 已经在 2026-10-07 由作者放出，下限仍然留在 beta2 是**故意的**：写成 `[21.2.1,)` 对新增玩家没有多买到什么，却会把还拿着 beta2 的人整台游戏挡在启动之外。我们要求这个下限的实际内容是：`gui.GuiTitleProvider` 在 `21.2.0` 里不存在，而我们的通讯接口与样板总线方块实现了它。

AE2 的下限从始至终是 `[19.2.17,)`，现在也是编译所依据的版本。上游自己在 `21.2.1-beta1` 把它的 AE2 下限抬到 `19.2.18`、在 `beta2` 又退回 `19.2.17`，所以从 `beta2` 起，对 AE2 唯一的闸就是我们这一条声明。依据是对两个 jar 的逐类比对：本模组点名的 AE2 类里只有一个在两者之间发生变化——`core.localization.Tooltips`——而 `19.2.18` 从它里面去掉的是三个我们从未点名的字段，所以 `19.2.18` 的玩家同样被覆盖。**作者自己那份 `21.2.1` 发布说明写的是同一句话（"AE2 最低版本为 19.2.17，兼容 19.2.17 / 19.2.18"），而那份 jar 的 `META-INF/neoforge.mods.toml` 里 `ae2` 就是 `[19.2.17,)`、`ldlib2` 就是 `[2.2.40,)` —— 与我们声明的完全同一档。**

**`1.3.1` 起，我们那份 `[[dependencies.neoecoprototype]]` 按 eco 21.2.1 的表逐条对齐。** 上游那张表是现量出来的，不是抄文档：`neoecobeta/neoecoae-21.2.1.jar` 里 `META-INF/neoforge.mods.toml` 一共 14 条 —— required 四条（`neoforge [21.1.0,)`、`minecraft [1.21.1, 1.22)`、`ae2 [19.2.17,)`、`ldlib2 [2.2.40,)`），optional 十条（`megacells [4.11.0,)`、`ae2omnicells [1.1.6,)`、`ae2_pattern_disk [0,)`、`extendedae [0,)`、`ae2lt [1.1.3,)`、`appliedenhancements [1.0.9,)`、`molecularmanipulator [2.0.5,),[2.0.5-fix]`、`useless_mod [1.21.1-2.3.7.2,)`、`jade [0,)`、`jei [0,)`）。

我们这一侧落成的样子与理由：

- `neoforge` / `minecraft` / `ae2`：与上游逐字同档，没有二次判断。
- `ldlib2`：**新增 required `[2.2.40,)`**。我们的 GUI 代码直接站在它上面，所以"我们也需要它"是真话；写出来只改变错误消息归到谁头上，不改变能跑起来的集合（上游本来就拦）。
- `megacells`：由 `*` 改成 **`[4.11.0,)`**。上游拦的版本我们不拦也没用，所以对齐等于零成本；这一条与"绝不写没量过的地板"不冲突 —— `4.11.0` 正是我们 classpath 里那一只（各 jar 的自报版本现量：ldlib2 `2.2.40`、ae2omnicells `1.1.6`、megacells `4.11.0`、jei `19.57.0.445`，全部落在我们写的区间里）。
- `ae2omnicells`：**新增 optional `[1.1.6,)`**，全能与量子那两只矩阵读它的 cell type。
- `jade` / `jei`：**新增 optional `*`**（上游给的是 `[0,)`，同为不设限），我们各有 4 处集成、全部走 `ModList` 判断。
- `appflux`：上游那份**没有**这一条，FE 那一族是我们自己的依赖，所以地板也不由它代言，保持 `*`。
- `extendedae`、`ae2lt`、`ae2_pattern_disk`、`appliedenhancements`、`molecularmanipulator`、`useless_mod`：**不声明**。按 mod id 与类包名两向 grep，我们源码里 0 处引用；上游声明它们不代表我们要声明。
- **还开着的口子**：`mekanism` 与 `appmek` 各有 4 处引用（化学品族），但上游那份表里也没有这两条，所以这一版没写进 toml。要不要单独立 optional，是一个还没拍的判断。

自证方式只有一条：改完跑 `runGameTestServer`。版本区间写错的表现是 pre-load FATAL（`[9.9.9,)` 那次实测过），而不是某条测试红 —— 这一次加载器起来、80 条 2.569 秒全绿。

比对方法记在这里，别重复踩：类清单要取常量池里被引用的 `appeng` 宿主类（源码 grep 会漏掉 `var` 推出来的类型），只比 entry name 不算证明——`21.2.1` 的两代 jar 里 0 个类被删除，但 230 个类的字节变了。

## 指南里的配方图只画"注册过映射"的配方类型

`<RecipeFor id="..." />` 的 `id` 走的是**物品**注册表（`MdxAttrs.getRequiredItemAndId` → `BuiltInRegistries.ITEM`），不是配方 id。拿到物品之后 `RecipeCompiler$RecipeTypeMapping.tryCreate` 做的是 `RecipeManager.byType(recipeType)` 再按 `getResultItem().getItem()` 过滤——**所以只有映射过的 `RecipeType` 才会被画出来**。现量：guideme 自己只映射 `RecipeType.CRAFTING`（`DefaultExtensions`），AE2 另加 inscriber / charger / transform 三种（`appeng.client.guidebook.RecipeTypeContributions`），eco 的 jar 里 `RecipeTypeMappingSupplier` 命中 0，我们的源码对 guideme 命中 0。

结果就是 `neoecoprototype:processor_assembler` 与 `neoecoae:integrated_working_station` 的配方在指南里**永远画不出来**：不报错、不警告，那个盒子只会去渲染同一件物品的别的配方。L1 供能接口那页原本就是踩在这个坑上——写"在处理器装配室里组装"后面跟的 `<RecipeFor>`，画出来的其实是"线缆部件 → 方块"的合成栏转换，看着像配方图，方向还是反的。

口径：装配室与工作站产出的东西，原料写在正文里，格子交给 JEI；`<RecipeFor>` 只用在原版合成台能做出的物品上。真要让指南画装配室的配方，就得实现 guideme 的 `RecipeTypeMappingSupplier` 扩展点并用 `LytStandardRecipeBox.Builder` 把我们的配方摊开——那时 `build.gradle:45` 的 `implementation files("libs/guideme-21.1.1.jar")` 才第一次真正参与编译（现在它只是给 dev 启动摆着，因为 AE2 把 guideme 声明成 `REQUIRED`）。

`C:/tmp/guide_recipefor_check.py` 是这一条的守卫：它把指南里每条 `<RecipeFor>` 的 id 对回我们自己配方的产出。**它看不见本节说的这个坑**——盒子有内容但内容不是正文说的那张配方，只有把正文句子与配方类型一起读才看得出来。

## 多方块朝向与镜像

处理“某一格才接受指定成员”或网络交换模块位置时，先读取 eco 自己的实现，再读取 AE2，最后才参考原版。eco 的正规入口是 `NENetworkSwitchUtil.switchPosition`，不要用 `front.getCounterClockWise()` 等手工推导替代它。

- 使用 `OrientationStrategies.horizontalFacing()` 获取 `IOrientationStrategy`。
- 使用 `strategy.getSide(controllerState, RelativeSide.LEFT/RIGHT)` 获取玩家视角下的左右方向。
- 非镜像位置使用 `RelativeSide.RIGHT`，镜像位置使用 `RelativeSide.LEFT`；这与 eco 的 `switchPosition(controllerPos, state, mirrored)` 同构。
- 位置判断和结构校验必须同时传递 `mirrored`，否则镜像结构会把合法成员判到错误的一侧。
- GameTest 中 `helper.assertTrue` 只记录失败，不会中断后续代码。异步测试遇到前置失败时必须使用 `helper.fail(...); return;`，否则继续调用 `succeed()` 可能让批次静默卡住。

## 装配室一轮合成的两个调用点（升级 AE2 时只重量这两处）

`MolecularAssemblerBlockEntity.tickingRequest()` 在一轮结束时按这个顺序问我们的 pattern。下面是 AE2 19.2.17 的字节码偏移，升级后**先重量这张表**再谈别的：

| 偏移 | 调用 | 对我们的意义 |
| --- | --- | --- |
| 375 → 384 | `craftingInv.asPositionedCraftInput()` → `CraftingInput$Positioned.input()`，结果存进 local 5 | 后面两次调用共用**同一个** `CraftingInput` |
| 404 | `myPlan.assemble(local5, level)` | 必须是纯函数：只返回产出，不发东西、不动网络 |
| 425 | `result.onCraftedBySystem(level)` | 原版行为，与我们无关 |
| 442 | `CraftingEvent.fireAutoCraftingEvent(level, plan, result, craftingInv)` | 见第 3 条 |
| 451 | `myPlan.getRemainingItems(local5)` | 超出一次的份数在这里交付（`ProcessorAssemblyPattern` 的 `surplusSink`） |

三条要记住的：

1. **`hasMats()`（偏移 361）也调 `assemble`**，而它只是探询"这轮跑不跑得动"。所以任何副作用都不能放进 `assemble`。`getRemainingItems` 在全类里**只有 451 这一个调用点**，一轮一次 —— 副作用放这里。
2. **两次调用拿到的是同一个 input 实例**（local 5，中间没人重建）。所以"把 `cycleOutput` 的结果缓存到 pattern 字段"这种写法是错的：`myPlan` 在一次推送里被反复使用，字段缓存会让第二轮沿用第一轮的数量，那才是真会丢东西或 dup 的写法。
3. **残余风险在 442**：`fireAutoCraftingEvent` 用 `Platform.getFakePlayer(serverLevel, ...)` 造一个假玩家，把 **`PlayerEvent.ItemCraftedEvent`** 发到 `NeoForge.EVENT_BUS` 上，并把**那个 `craftingInv` 本身**交给监听者。任何第三方 handler 都能在 `assemble` 与 `getRemainingItems` 之间同步改这个网格。我们没防（防它要把 input 复制一份）。下面这两半都是照我们自己的算法推出来的，**没有做过实验**：handler **清空**网格会让我们少交付（安全方向），handler **往网格里添**东西会让 `cycleOutput` 按添后的量算、可能多交付。已知，未防。

## FE 存储单元的字节尺子（1M 为什么一只都存不进）

appflux 把 AE2 字节换算成 FE：`FluxKeyType.getAmountPerByte()` 直接返回配置 `flux_cell.amount`，默认 1048576，所以 **1 字节 = 1 Mi FE**。但**物品名里的"1m/16m"数的是字节，不是 FE**：appflux 自己的 `ItemFECell(core, n, drain)` 算的是 `totalBytes = n × 1024`，`fe_1m_cell` 是 1,048,576 字节；eco 的 `ECOFeStorageCellItem` 只有三参构造，容量在基类里推 —— `totalBytes = tier.getStorageTotalBytes()`、`bytesPerType = 1 << (12 + tier.getTier())`、`idleDrain = totalBytes / 1048576`、`getTotalTypes() = cellType.typeCount()`。eco 的 "16m" 于是 = 16,777,216 字节 ≈ 1.7e13 FE。照着名字抄数字会抄错一个量级。

**更要紧的是 eco 的字节算法有一道静默死路**：`ECOStorageCell` 给每个新类型先扣一份"每类型字节"，`canHoldNewItem()` 要 `freeBytes > bytesPerType`（相等时只留 `getUnusedItemCount() > 0` 这条后门，空仓走不通），`innerInsert` 接着要 `remainingItemCount − bytesPerType × amountPerByte > 0`。所以 **`totalBytes == bytesPerType` 的仓永远接不下它的第一个 key**，不报错、不提示，只是存不进去。我们的 1M/4M 两只最后落在 `1L<<20 / 1L<<22` 字节、`bytesPerType = bytes >> 8`，正好和 appflux 的 fe_1m/fe_4m 同尺。

**这类容量算术只有真插一次才算验过。** 守卫是 `feCellsCarryTheL1FluxLayout`：读完布局之后拿 `ECOStorageCells.getCellInventory(stack, null)` 真 `insert(FluxKey.of(FE), MAX, MODULATE)` 一次，返回 0 就红。把旧的 1 字节布局种回去，它就在这一条红出来。

## 存储单元没有 tick：会"随时间变化"的单元只能靠推导

想加一只"自己慢慢产东西"的存储单元之前，先接受这个结构事实（AE2 19.2.17 + eco beta2，全部 javap 现量）：

- `appeng.api.storage.cells.StorageCell` 只有 `getStatus / getIdleDrain / canFitInsideCell / persist`；eco 的 `IECOStorageCell` 补的是容量 getter。**两边都没有 tick 入口**，`ICellHandler` 也只有 `isCell / getCellInventory`。
- `appeng.api.networking.IManagedGridNode` 在 19.2.17 **没有** `setMainAction`；`AEBaseBlockEntity` 不是 `ServerEntity`；我们自己的 L1 驱动器 BE 里一次 `tick` 都没有 —— 全仓库能白拿的钟只有 `SimplifyStorageHostBlockEntity.tick`。
- 所以"累加"这条走不通（没有地方累加）。可用的形状是**推导**：栈上只存两个数（起始世界 tick、已被取走量），库存 = `(now − start)/间隔 × 每批量 − 已取走`；乘法只做 long 饱和，**没有库存上限**。**读取必须不写状态**，只有真正付货时才写回。
- 世界时间在**服务端**从 `ServerLifecycleHooks.getCurrentServer().getLevel(Level.OVERWORLD).getGameTime()` 拿；那个字段是 JVM 内的，多人客户端拿到 null。但客户端并不是没有这个数：见下面两条，所以显示走"手上有 level 就读 level"，结算与没 level 的调用者才回落到服务端钟。
- **客户端确实收到世界 tick，一秒一次**（不是每 tick）：`MinecraftServer.synchronizeTime(ServerLevel)` 用 `serverLevel.getGameTime()` 构造 `ClientboundSetTimePacket`，而 `tickChildren` 只在 `tickCount % 20 == 0` 时调它（`forceTimeSynchronization()` 是插队那条路）；客户端 `ClientPacketListener.handleSetTime` 直接 `ClientLevel.setGameTime(packet.getGameTime())`。NeoForge 另发一份同数的 `ClientboundCustomSetTimePayload`。含义：联机客户端 hover 出来的库存最多比服务端晚 20 tick —— 默认 600 tick 一批看不出来，把间隔调到下限 60 tick 时最坏差三分之一个批量，而 `extract` 是按服务端读数夹的，所以只会"显示得略多"，不会少付。
- **不在主世界的维度共用同一个钟**：`DerivedLevelData.getGameTime()` 转发包裹着的 `ServerLevelData`，而它的 `setGameTime(long)` 是空方法 —— 只有主世界那份在走。所以在下界 hover 也不是另一条时间线。
- 拿到 level 的路是 `Item.TooltipContext.level()`：`Screen.getTooltipFromItem(Minecraft, ItemStack)` 传的是 `Item.TooltipContext.of(minecraft.level)`，`EMPTY` 那份的 `level()` 返回 null（`appendHoverText` 也可能被外面直接传进一个 null context，判一下）。反过来 **`Item.getTooltipImage(ItemStack)` 里没有 level** —— 内容预览小图是这一族在多人客户端上唯一还读不到钟的地方，缺图不缺数。
- 这条成立的前提也量过：`NetworkStorage` 里没有缓存的内容列表字段，终端问网络要内容时会**实时遍历各存储调 `getAvailableStacks`** —— 推导出来的库存不需要任何 mutation 就能长出来。反过来，如果你的单元靠"攒"，就必须自己找到钟，而这里没有。
- 想给"没人来问也要开始"找一个钩子，原版只有一条路，量过：`Inventory.tick()` 遍历 `compartments = ImmutableList.of(items, armor, offhand)`，对每格调 `ItemStack.inventoryTick` ⇒ **玩家身上 41 格都算**（主物品栏 36 + 盔甲 4 + 副手 1），但**两个 dist 都会跑**，所以要自己判 `level.isClientSide`，写服务端再靠槽位同步带回客户端。`ItemEntity` 里没有这个调用（javap 计数 0 处），所以丢在地上、留在箱子里的都不会 tick —— 那只元件要等被捡起来或插进驱动器。**3.1.0 起这条钩子我们故意没用**：规则收成"只有驱动器与主机问过库存才起算"，身上起算要多背一个 dist 判断，而且会让创造栏里放着的一只也在产。

## 验证清单

- `compileJava processResources` 通过。
- 普通 AE2 接口和普通 AE2 screen 未被全局资源覆盖。
- L1 方块和 Part 的 CONFIG/STORAGE 槽位数量与实际逻辑一致。
- 第一组和第二组 amount button 都只打开对应 CONFIG 槽的库存量菜单。
- 输出行没有箭头贴图。
- AE 卡能插入、保存、重新打开，并且不支持的卡被拒绝。
- 只有 L1 设备获得额外被动供电；普通 AE2 设备行为不变。
- 至少启动一次客户端检查 Mixin、资源和菜单注册日志。
- FE 那两条守卫（`fe_cell_l1`、`fe_induction_card`）是 `required = false`：只有把 appflux + guideme + Glodium 三个 jar 放进 `run-gametest/mods/` 才真的执行，不放就是"通过 = 没测"。
