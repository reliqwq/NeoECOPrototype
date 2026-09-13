# KubeJS support

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

- `.cellModel('neoecoprototype:block/cell/xxx')`：覆盖该矩阵在存储矩阵驱动器
  中渲染的格子模型（默认使用普通物品格子模型）。注意：必须传**格子芯片规格**
  的模型——格式同 `neoecoprototype:block/cell/storage_cell_l1_*`（约 6×12×2
  的小方块，带 `level` / `type` / `base` 贴图槽）。传普通整方块模型
  （如 `minecraft:block/stone`）不会报错，但会在驱动器中尺寸过大并穿模；
- `.displayName(...)`、`.tooltip(...)` 等 KubeJS 通用物品 builder 方法均可叠加。

物品/流体绑定类型延迟解析，注册时目标内容尚不存在也不会报错，进世界首次
使用时才求值；解析失败会在日志中给出含脚本 ID 的可读报错。

### 可发现性与自动补全

- builder 的所有方法带 `@Info` 中文描述；整合包作者安装 **ProbeJS** 后，在
  VSCode 中编写脚本即可获得 `itemType` / `fluidType` / `cellModel` 等
  方法的自动补全与提示；
- 未安装 ProbeJS 时，以本文档与 `docs/kubejs.md` 同级示例为准；
- `.cellModel(...)` 不调用时默认使用无限系列的银灰类型灯模型。
