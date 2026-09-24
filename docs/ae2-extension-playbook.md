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
