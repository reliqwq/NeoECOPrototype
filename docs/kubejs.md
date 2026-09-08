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
