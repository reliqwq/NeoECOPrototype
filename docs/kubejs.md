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

离线调色只生成材质；如果要新增一个实际矩阵物品，还需要在 Java 中注册对应的 cell item、模型和翻译。当前的 4K 化学品矩阵可用下面的命令获取：

```text
/give @s neoecoprototype:simplify_chemical_storage_cell_4k
```

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

  event.create('water_matrix_1m', 'neoecoprototype:storage_matrix')
       .fluidType('minecraft:water')
       .size('1m')
})
```

可用方法：

- `.itemType('namespace:item')`：注册绑定单个物品的矩阵。
- `.fluidType('namespace:fluid')`：注册绑定单个流体的矩阵。
- `.chemicalType('namespace:chemical')`：注册绑定单个 Mekanism 化学品的矩阵，需要 Mekanism 和 Applied Mekanistics。
- `.type('item'/'fluid'/'chemical')`：设置矩阵声明类型；绑定方法会自动设置对应类型。
- `.size('infinite'/'1k'/'4k'/'16k'/'64k'/'1m'/'4m')`：设置容量。
- `.inventoryModel('namespace:item/model')`：覆盖物品栏模型。
- `.cellModel('namespace:block/model')`：覆盖驱动器内格子模型。
- `.parentModel(...)` / `.textures(...)`：完全接管物品栏模型；设置后不再套用默认模型。

`infinite` 使用现有无限矩阵 handler；有限 item/fluid 使用 eco 存储后端；有限 chemical
使用 AppMek 化学品后端。所有脚本在 `startup_scripts` 中运行，修改后需要重启游戏。

默认模型不需要脚本配置，按矩阵**类型**自动选择材质，不会所有矩阵都长一样。
材质定义集中在 `MatrixMaterials`，脚本不应自行乱放贴图：

| 类型 | 物品栏模型 | 驱动器内模型 | 外壳材质 |
|---|---|---|---|
| item 物品 | `item/simplify_item_storage_cell_1k` | `block/cell/storage_cell_l1_item` | 物品外壳 |
| fluid 流体 | `item/simplify_fluid_storage_cell_1k` | `block/cell/storage_cell_l1_fluid` | 流体外壳 |
| chemical 化学品 | `item/simplify_chemical_storage_cell_1k` | `block/cell/storage_cell_l1_chemical` | 化学品外壳 |
| infinite 无限 | `item/simplify_concrete_storage_cell` | `block/cell/storage_cell_l1_concrete` | 无限银灰 |
| other 其他 | `item/simplify_default_storage_cell` | `block/cell/storage_cell_l1_custom` | 原量子外壳 / 驱动器棕色 |

固定物品家族的材质（同样由 `MatrixMaterials` 记录，供对照）：

| 家族 | 物品栏外壳 |
|---|---|
| 小宗 small bulk | `item/storage_recolor/small_bulk_cell_housing`（深灰） |
| 猪咪 pigcat | `item/storage_recolor/pigcat_cell_housing` |
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
