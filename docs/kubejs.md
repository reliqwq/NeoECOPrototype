# KubeJS support

Ready-to-copy script: [`docs/kubejs-example.js`](kubejs-example.js). It covers the startup matrix builders, recipe changes, and a debug command.

## Offline matrix texture recoloring

矩阵材质改色使用仓库内的离线工具，不在 Minecraft 运行时处理 PNG。工具只匹配配置中的源颜色及其色相邻近色，保留灰白外壳、黑色边框、透明像素和原始明暗关系。

需要 Pillow：

```powershell
python -m pip install pillow
python tools/generate_matrix_textures.py tools/matrix_textures.json
```

配置文件：

```text
tools/matrix_textures.json
```

每个 `textures` 条目包含 `source`、`output`、`source_colors`、`target_color`、`hue_tolerance`、`min_saturation` 和 `brightness`。当前示例把流体矩阵蓝色区域生成深绿色化学品矩阵纹理。生成结果仍是普通 PNG，客户端无需运行时处理。

离线调色只生成材质；如果要新增一个实际矩阵物品，还需要在 Java 中注册对应的 cell item、模型和翻译。

配方口径统一是**外壳 + AE2 同级 `cell_component_<档>`**（化学品每一件额外带 `appmek` 加载条件）。

| 介质 | 有的档位 |
|---|---|
| 物品 | `1k / 16k / 64k / 1m / 4m` |
| 流体 | `1k / 16k / 64k / 1m / 4m` |
| 化学品 | `1k / 16k / 64k / 1m / 4m` |

**三档介质都没有 4k。** 化学品 4k 以前是个"注册了但只能 `/give`"的半成品（1.2.0 起就这样，没有配方也不在创造栏），这次直接撤掉了——撤它的代价几乎为零，因为正常存档里不可能有这件东西。撤的理由是驱动图 `cell_level_lower` 里没有 4k 的颜色格：Ted 确认过行序是 **v0-1 蓝=1k、v2-3 淡紫=16k、v4-5 青=64k、v6-7 绿=1M**，她交付时按背包灯的档位少报了一档，所以我们把她的 `l0_*_4k`（淡紫）归位成 `l0_*_16k`、`l0_*_16k`（青）归位成 `l0_*_64k`。

KubeJS is optional. Neo ECO Prototype does not declare KubeJS as a runtime dependency and the mod continues to work without it.

Put scripts in the KubeJS `server_scripts` directory and reload recipes with `/reload` or restart the game.

## Remove an addon recipe

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'neoecoprototype:simplify_storage_component_4m' })
  event.remove({ id: 'neoecoprototype:simplify_computation_casing' })
})
```

## Replace an ordinary crafting recipe

Vanilla shaped and shapeless recipes use the normal KubeJS recipe helpers.

```js
ServerEvents.recipes(event => {
  event.shaped('neoecoprototype:simplify_storage_component_4m', [
    'ADA',
    'BCB',
    'AEA'
  ], {
    A: 'neoecoae:energized_crystal_dust',
    B: 'neoecoprototype:simplify_storage_component_1m',
    C: 'ae2:quartz_glass',
    D: 'neoecoae:superconducting_processor',
    E: 'neoecoae:energized_superconductive_ingot'
  })
})
```

Remove the original recipe first when replacing an existing recipe ID:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'neoecoprototype:simplify_storage_component_4m' })

  event.shaped('neoecoprototype:simplify_storage_component_4m', [
    'ADA',
    'BCB',
    'AEA'
  ], {
    A: 'minecraft:iron_ingot',
    B: 'neoecoprototype:simplify_storage_component_1m',
    C: 'ae2:quartz_glass',
    D: 'ae2:printed_engineering_processor',
    E: 'minecraft:gold_ingot'
  })
})
```

## Add an integrated working station recipe

Neo ECO AE Extension recipes can be added with KubeJS `event.custom`. The recipe type is supplied by the upstream mod.

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'neoecoae:integrated_working_station',
    energy: 10000,
    inputItems: [
      { item: 'neoecoprototype:simplify_storage_component_4m', count: 3 },
      { item: 'neoecoae:energized_superconductive_ingot', count: 26 },
      { item: 'neoecoae:superconducting_processor', count: 1 },
      { item: 'neoecoae:crystal_ingot', count: 1 }
    ],
    itemOutput: {
      id: 'neoecoae:eco_cell_component_16m',
      count: 1
    }
  })
})
```

Use the recipe ID to remove a specific integrated working station recipe. For example:

```js
ServerEvents.recipes(event => {
  event.remove({ id: 'neoecoprototype:integrated_working_station/simplify_storage_component_4m' })
})
```

The exact item IDs and recipe IDs are part of the addon data files. Check `src/main/resources/data/neoecoprototype/recipe` when adapting a script to a particular release.

KubeJS scripts can change addon recipes, but they do not change multiblock structure definitions or the behavior of the storage and computation controllers.

## What KubeJS should configure

KubeJS is an optional content-layer extension point, not a replacement for the Java AE2 integration layer.

Good candidates for KubeJS:

- storage matrix definitions, bound item/fluid/chemical, capacity tier, material family, and models;
- addon and upstream integrated-working-station recipes;
- pack-specific recipe removal, replacement, and balancing;
- future data-driven allowlists whose meaning is already defined by a stable Java API.

Keep these in Java and resources:

- AE2 menu semantics and the distinction between `CONFIG` marker slots and `STORAGE` output slots;
- the L1 interface's alternating six-row screen layout, amount-button-to-CONFIG mapping, and screen-scoped slot repositioning;
- passive power generation, suppression behavior, network node flags, and block/Part host logic;
- AE2 upgrade-card associations, because they must be registered for the exact block item and Part item and must match the logic that actually supports the card;
- multiblock structure validation, controller behavior, and Part geometry.

This split is intentional. A script can safely choose what content exists and how it is crafted, while the Java layer guarantees that an interface slot, button, texture, network behavior, and upgrade inventory remain the same contract. KubeJS support is therefore useful, but exposing raw GUI coordinates or arbitrary AE2 card insertion through scripts is not recommended.

## 自定义无限存储矩阵（KubeJS 在场时）

KubeJS 在场时可以通过自定义 builder 类型 `neoecoprototype:infinite_storage_matrix`
创建任意"无限仓库"矩阵：绑定的物品/流体存入即融入、无限取出、优先路由，
与无限混凝土矩阵同一套行为（L1 等级、8 AE/t 待机、不可拆卸）。

在 `kubejs/startup_scripts` 中注册（需重启游戏生效）：

```js
StartupEvents.registry('item', event => {
  // 绑定单个物品
  event.create('infinite_dirt', 'neoecoprototype:infinite_storage_matrix')
       .itemType('minecraft:dirt')
       .displayName('无限泥土存储矩阵')

  // 绑定单个流体
  event.create('infinite_water', 'neoecoprototype:infinite_storage_matrix')
       .fluidType('minecraft:water')
       .displayName('无限水存储矩阵')
})
```

可选方法：

- 物品栏：按类型自动选择（item/fluid/chemical/infinite/other），各用自己家族的外壳配色；
  也可以用 `.material(...)` 显式指定。
- 驱动器内：同样按类型自动选择（`storage_cell_l1_item` / `storage_cell_l1_fluid` /
  `storage_cell_l1_chemical` / `storage_cell_l1_concrete` / `storage_cell_l1_custom`）。
- `cellModel` 必须传**格子芯片规格**的模型，而不是普通整方块模型。模型通常约为
  6×12×2，并包含 `level` / `type` / `base` 贴图槽；例如
  `neoecoprototype:block/cell/storage_cell_l1_fluid`。
- `.displayName(...)`、`.tooltip(...)` 等 KubeJS 通用物品 builder 方法均可叠加。

物品栏模型按绑定类型自动选择（item/fluid/chemical 各用自己的外壳配色，无限矩阵用银灰配色），
脚本也可以用 `.inventoryModel(...)` 覆盖。物品/流体绑定类型延迟解析，注册时目标
内容尚不存在也不会报错，进世界首次使用时才求值；解析失败会在日志中给出含脚本
ID 的可读报错。

可选的统一参数方法（下面显式写出的是无限矩阵的默认配色，不写也一样）：

```js
StartupEvents.registry('item', event => {
  event.create('infinite_oak_log', 'neoecoprototype:infinite_storage_matrix')
       .itemType('minecraft:oak_log')
       .size('infinite')
       .tier('l1')
       .inventoryModel('neoecoprototype:item/simplify_concrete_storage_cell')
       .cellModel('neoecoprototype:block/cell/storage_cell_l1_concrete')
})
```

`.size('infinite')` 和 `.tier('l1')` 是当前无限矩阵实现支持的固定组合；传入其他值会
明确报错，而不是创建元数据与实际存储行为不一致的矩阵。

## 通用存储矩阵注册（KubeJS）

使用 `neoecoprototype:storage_matrix` 可以注册新的物品、流体或化学品矩阵：

```js
StartupEvents.registry('item', event => {
  event.create('hydrogen_matrix_4k', 'neoecoprototype:storage_matrix')
       .chemicalType('mekanism:hydrogen')
       .size('4k')
       .displayName('4K 氢气存储矩阵')

  event.create('oak_matrix_16k', 'neoecoprototype:storage_matrix')
       .itemType('minecraft:oak_log')
       .size('16k')
       // 注意：这一档是通用物品矩阵，上面绑的 oak_log 不会生效；
       // 要一台只装 oak_log 的矩阵就别写 size，用默认的 infinite。

  event.create('water_matrix_1m', 'neoecoprototype:storage_matrix')
       .fluidType('minecraft:water')
       .size('1m')
})
```

可用方法：

- `.itemType('namespace:item')`：绑定一种物品。**只有 `size('infinite')` 那一档真的绑**；有限档是通用物品矩阵（默认 256 种），绑定的那一种被忽略。
- `.fluidType('namespace:fluid')`：同上，只有无限档真的绑。
- `.chemicalType('namespace:chemical')`：绑定一种 Mekanism 化学品，有限档也真的绑；需要 Mekanism 和 Applied Mekanistics，缺了脚本会收到这一句而不是找不到类。
- `.type('item'/'fluid'/'chemical')`：设置矩阵声明类型；绑定方法会自动设置对应类型。传家族名（`pigmee`/`universal`/`quantum`/`small_bulk`/`infinite`/`other`）只换材质，不换容量 —— 容量一律用 `.size(...)`。
- `.size('infinite'/'1k'/'4k'/'16k'/'64k'/'1m'/'4m')`：设置容量，档位名就是它字面的 2 的幂 —— `1k` = 1024、`4k` = 4096、`16k` = 16384、`64k` = 65536、`1m` = 1048576、`4m` = 4194304 字节。**注意这跟我们自己成品的那几只小仓不是一回事**：成品物品/流体/化学的 1K/16K/64K 比名字多 25%（1280 / 20480 / 81920），量子的 1M 比名字多 50%（1572864）—— 那是成品里唯一在 1M 上抬高的档位。要在脚本里做出和成品一样大小的仓，别用档位，直接写 `.bytes(1280)`；`.size(...)` 与 `.bytes(...)` 同时出现时以 `.bytes(...)` 为准。
- `.bytes(n)`：改用自定义字节数，**它会顶掉先前的或之后的 `size(...)` 档位**（两种顺序都一样）；`size('infinite')` 与 `.bytes(...)` 同时出现会明确报错。
- `.totalTypes(n)`：改类型数，默认 256（物品/流体）或 25（化学品）。
- `.grows(perBatch, ticksPerBatch)`：把它变成**自产矩阵**，见下一节。
- `.inventoryModel('namespace:item/model')`：覆盖物品栏模型。
- `.cellModel('namespace:block/model')`：覆盖驱动器内格子模型。
- `.parentModel(...)` / `.textures(...)`：完全接管物品栏模型；设置后不再套用默认模型。

`infinite` 使用现有无限矩阵 handler；有限 item/fluid 使用 eco 存储后端；有限 chemical
使用 AppMek 化学品后端。这个 builder **不需要装 AE2 Omni Cells**：只有 `universal` 与
`quantum` 两个家族要它，缺了会在脚本注册那一项上报"需要 AE2 Omni Cells"。所有脚本在
`startup_scripts` 中运行，修改后需要重启游戏。

**卡片（AE2 的升级卡）按后端归属走**：

- 所有带分区表的矩阵（包括 `universal` / `quantum` 两个家族）**都认模糊卡与反转卡各一张**。这两张改的是分区表的匹配方式，Omni Cells 后端照单执行；其余卡不收 —— 容量、速度这类靠数字改机器行为的卡不动它们（它们的类型上限与字节由 Omni Cells 那侧决定，量子连上限都没有）。
- **溢出销毁卡除小宗仓外都接受** —— 它会删掉塞不下的东西，所以小宗那六只（3 类/10 类上限）不收：删掉的可能是玩家还看得见位置、只是被类型上限挡住的那部分货。其余矩阵（含 `universal` / `quantum`）收一张，和 Omni Cells 自己那族一致。
- 没有分区表的那几只（自产矩阵、无限混凝土源）本来就收不了卡，存储盘的工作台对它们是拒开的。

这些不是文档里的愿望：`everyPartitionedCellTakesTheFilterCardsAndVoidSkipsSmallBulk`（batch `cell_card_policy`）在启动后按注册时的同一条谓词把 AE2 的卡↔元件关联表整个扫一遍，读数写进日志（`cell card policy:` 那行）。

默认模型不需要脚本配置，按矩阵**类型**自动选择材质，不会所有矩阵都长一样。
材质定义集中在 `MatrixMaterials`，脚本不应自行乱放贴图：

| 类型 | 物品栏模型 | 驱动器内模型 | 外壳材质 |
|---|---|---|---|
| item 物品 | `item/simplify_item_storage_cell_1k` | `block/cell/storage_cell_l1_item` | 物品外壳 |
| fluid 流体 | `item/simplify_fluid_storage_cell_1k` | `block/cell/storage_cell_l1_fluid` | 流体外壳 |
| chemical 化学品 | `item/simplify_chemical_storage_cell_1k` | `block/cell/storage_cell_l1_chemical` | 化学品外壳 |
| infinite 无限 | `item/simplify_concrete_storage_cell` | `block/cell/storage_cell_l1_concrete` | 无限银灰 |
| grows 自产（不写 material 时） | `item/simplify_concrete_storage_cell` | `block/cell/storage_cell_l1_concrete` | 无限银灰（它属于无限家族，不跟随绑定的媒介） |
| other 其他 | `item/simplify_default_storage_cell` | `block/cell/storage_cell_l1_custom` | 原量子外壳 / 驱动器棕色 |

固定物品家族的材质（同样由 `MatrixMaterials` 记录，供对照）：

| 家族 | 物品栏外壳 |
|---|---|
| 小宗 small bulk | `item/storage_recolor/small_bulk_cell_housing`（深灰） |
| 猪咪 pigmee | `item/storage_recolor/pigmee_cell_housing` |
| 全能 universal | eco `omni_cell_housing` |
| 量子 quantum | eco `quantum_omni_cell_housing` |

`.material('item'/'fluid'/'chemical'/'infinite'/'other')` 可以显式指定材质；不写时按类型自动决定。
写法保持一行一句即可，例如：

```js
StartupEvents.registry('item', event => {
  event.create('stone_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:stone').size('1k').displayName('石头存储矩阵')

  event.create('other_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:gold_ingot').size('1k').material('other').displayName('其他材质矩阵')
})
```

这些模型都会在客户端自动注册，因此脚本创建的矩阵不会再出现紫黑缺失贴图。
需要 eco 那种"外壳 + 类型贴图"拼装时可以自行设置 `textures`，例如：

```js
StartupEvents.registry('item', event => {
  const matrix = event.create('custom_item_matrix_1k', 'neoecoprototype:storage_matrix')
  matrix.itemType('minecraft:stone')
  matrix.size('1k')
  matrix.parentModel('minecraft:item/generated')
  matrix.textures({
    layer0: 'neoecoprototype:item/storage_recolor/eco_item_cell_housing',
    layer1: 'neoecoprototype:item/storage_recolor/eco_cell_light_16m',
    layer2: 'neoecoprototype:item/storage_recolor/eco_cell_status_light'
  })
})
```

## 自产矩阵（`grows`）

`.grows(单批产量, 间隔刻)` 造的是一只**什么都不吃、自己长货**的仓：每隔
`ticksPerBatch` 世界刻跳一次，每次给出 `perBatch` 个，内容就是
`itemType` / `fluidType` / `chemicalType` 绑的那一个 AE key。本模组的奇点存储矩阵
就是这一族里注册出来的那一只，`grows` 让整合包加自己的成员。

```js
StartupEvents.registry('item', event => {
  event.create('lava_grower', 'neoecoprototype:storage_matrix')
       .fluidType('minecraft:lava')
       .grows(128, 600)
       .displayName('熔岩自产矩阵')

  event.create('hydrogen_grower', 'neoecoprototype:storage_matrix')
       .chemicalType('mekanism:hydrogen')
       .grows(1, 1200)
       .displayName('氢气自产矩阵')
})
```

规则与限制，一条一句：

- 两个数都必须写、都必须为正；写 0 或负数当场报错，不做"缺省回配置"。
- 产量与间隔是这只仓自己的数字，**不读服务端配置**：改配置只影响注册的那只奇点仓。
- 脚本这条路只要求为正，没有配置那条 60 刻的地板；想要慢就直接写大间隔。
- 内容用哪个媒介由绑定的 key 决定，任意 mod 的物品、流体、化学品都可以；化学品照旧要 Mekanism 与 Applied Mekanistics。
- `perBatch` 的单位就是那个 key 自己的计量单位，AE2 终端怎么显示那个媒介，这里就按什么计。
- 往它推任何东西都是 0，只出不进。
- 插进驱动器、被网络问过库存才开始计时；**放在身上不算**（和奇点仓同一条规则）。
- 库存没有上限，长到 `long` 顶就饱和、不回绕；已经被取走的部分不会自己回来。
- 它属于**无限家族**：不写 `.material(...)` 时戴银灰混凝土外壳（和 `infinite` 档那几只同款），不跟随绑定的媒介 —— 一只没有字节预算的仓套上流体外壳会说法不一致。`.material('other')` 一类的显式指定照旧听脚本的。
- 它也进 `StorageMatrixRegistry`（按 `infinite` 定义），为的是驱动器那一格有模型可读；所以 `cellModel(...)` 可以用。`bytes(...)`、`totalTypes(...)`、`size(...)` 仍然拒绝 —— 它没有字节预算可描述。
- 不能借 `universal` / `quantum` 家族：那两个后端存的是网络推进来的东西。
- 待机耗电固定 1.0，与奇点仓一致；物品堆叠上限 1。
- 主机面板上它和奇点仓一样不占存储那一族的行；数量写在它自己的 tooltip 与玉的驱动器提示里，那一行会带上内容名。
- 注册表冻结之后不能再造物品，所以这类脚本只在启动脚本里跑，改完要重启游戏。

**这条的套件面只断在算法上**：注册表冻结后连一个没注册的 `Item` 都造不出来，
gameTest 恰好跑在冻结之后，所以断言落在 `SimplifySingularityCellItem.Growth` 这个对象上；
`grows(...)` 接到真物品那一段已在客户端实测注册通过（化学那只会把密钥留到第一次运行期才解，
见上），驱动器格子里的外观与产量跳变还要开一次存档眼验。

### 新矩阵注册 API（Java）

后续新增的有限矩阵可以使用 `StorageMatrixDefinition`、`StorageMatrixRegistration` 和
`StorageMatrixRegistry`。现有矩阵注册入口保持不变，不需要迁移旧代码。

```java
StorageMatrixDefinition definition = StorageMatrixDefinition.finite(
        ResourceLocation.fromNamespaceAndPath("neoecoprototype", "example_cell_1m"),
        StorageMatrixDefinition.MatrixType.ITEM,
        StorageMatrixDefinition.MatrixSize.M1,
        1L << 20,
        1 << 12,
        256,
        SimplifyTier.L1,
        inventoryModel,
        driveModel);

StorageMatrixRegistration.registerFinite(
        ModRegistration.ITEMS,
        "example_cell_1m",
        definition,
        AEKeyType.items(),
        SimplifyStorageCellItem::getItemCellType);
```

定义包含类型、大小、容量、每类型容量、类型数量、等级、待机耗电以及物品栏/驱动器
模型。注册器会校验定义 ID 与物品注册名一致，并拒绝重复定义。可用
`StorageMatrixRegistry.get(item)` 或 `get(resourceLocation)` 查询定义。

该 API 当前是新增矩阵的声明和索引入口；已有 finite、chemical、fluid、infinite 以及
Beyond 适配器仍使用原有 handler 和注册路径，避免改变现有存档行为。

### 可发现性与自动补全

- builder 的所有方法带 `@Info` 中文描述；整合包作者安装 **ProbeJS** 后，在
  VSCode 中编写脚本即可获得 `itemType` / `fluidType` / `cellModel` 等
  方法的自动补全与提示；
- 未安装 ProbeJS 时，以本文档与 `docs/kubejs.md` 同级示例为准；
- `.cellModel(...)` 不调用时默认使用无限系列的银灰类型灯模型。
