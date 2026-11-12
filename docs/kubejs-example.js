// =============================================================================
// Neo ECO Prototype —— KubeJS 完整示例
// 版本对应：neoecoprototype 1.2.0 / KubeJS 2101.7.2（1.21.1 NeoForge）
//
// 用法：
//   1. 把「STARTUP 部分」存成 kubejs/startup_scripts/neoeco_matrix_example.js
//      把「SERVER 部分」存成 kubejs/server_scripts/neoeco_recipe_example.js
//      （也可以整段塞进同一个 startup 文件，但配方改动建议放 server_scripts，
//        这样不用重启，/reload 即可生效）
//   2. 启动脚本改完必须重启游戏；服务端脚本 /reload 或重启都行。
//   3. 脚本创建的物品 ID 统一是 kubejs:<你写的名字>，例如 kubejs:stone_matrix_1k。
//
// 可选依赖（没装就把对应段落注释掉，否则启动会明确报错而不是静默跳过）：
//   - 化学品矩阵  需要 Mekanism + Applied Mekanistics
//   - 全能/量子矩阵 需要 ae2omnicells（AE2 Omni Cells）
// =============================================================================

// -----------------------------------------------------------------------------
// STARTUP 部分 —— kubejs/startup_scripts/neoeco_matrix_example.js
// -----------------------------------------------------------------------------

StartupEvents.registry('item', event => {
  // ---------- 1. 最常用：有限物品 / 流体矩阵 ----------
  // 外壳、驱动器内模型、类型数都按类型自动决定，不用手写贴图。
  event.create('stone_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:stone')
    .size('1k')
    .displayName('1K 石头存储矩阵')

  event.create('water_matrix_4k', 'neoecoprototype:storage_matrix')
    .fluidType('minecraft:water')
    .size('4k')
    .displayName('4K 水存储矩阵')

  // ---------- 2. 化学品矩阵（需要 Mekanism + Applied Mekanistics）----------
  event.create('hydrogen_matrix_4k', 'neoecoprototype:storage_matrix')
    .chemicalType('mekanism:hydrogen')
    .size('4k')
    .displayName('4K 氢气存储矩阵')

  // ---------- 3. 无限矩阵：绑定一个物品，存入即吸收、无限取出 ----------
  event.create('infinite_glass', 'neoecoprototype:infinite_storage_matrix')
    .itemType('minecraft:glass')
    .displayName('无限玻璃存储矩阵')

  event.create('infinite_water', 'neoecoprototype:infinite_storage_matrix')
    .fluidType('minecraft:water')
    .displayName('无限水存储矩阵')

  // 无限矩阵可以用别的家族的「格子芯片规格」模型当驱动器内外观。
  // 注意：cellModel 必须是 6x12x2 的格子芯片模型，传整方块模型会在驱动器里穿模。
  event.create('infinite_wool', 'neoecoprototype:infinite_storage_matrix')
    .itemType('minecraft:white_wool')
    .cellModel('neoecoprototype:block/cell/storage_cell_l1_pigmee')
    .displayName('无限羊毛存储矩阵')

  // ---------- 4. 自定义容量与类型数（不走固定档位）----------
  // bytes(...) / totalTypes(...) 会给定义打上 CUSTOM，不受 1k/4k/1m 档位约束。
  event.create('pigmee_400_matrix', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:gold_ingot')
    .material('pigmee')
    .bytes(256)
    .totalTypes(400)
    .displayName('猪咪定制矩阵 | 256 B / 400 类型')

  // ---------- 5. 全能 / 量子后端（需要 ae2omnicells）----------
  // 给 type(...) 传家族名会同时切换后端与外观：
  //   universal = 全能后端，默认 256 类型，可用 totalTypes(...) 覆盖
  //   quantum   = 量子后端，无限类型，不要写 totalTypes(...)
  event.create('universal_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:gold_ingot')
    .type('universal')
    .size('1k')
    .displayName('1K 全能矩阵 | 256 类型')

  event.create('quantum_matrix_1k', 'neoecoprototype:storage_matrix')
    .itemType('minecraft:gold_ingot')
    .type('quantum')
    .size('1k')
    .displayName('1K 量子矩阵 | 无限类型')

  // ---------- 6. 完全接管物品栏外观 ----------
  // 设了 parentModel + textures 后，builder 不再套用默认矩阵模型。
  const custom = event.create('custom_look_matrix_1k', 'neoecoprototype:storage_matrix')
  custom.itemType('minecraft:diamond')
  custom.size('1k')
  custom.parentModel('minecraft:item/generated')
  custom.textures({
    layer0: 'neoecoprototype:item/storage_recolor/eco_item_cell_housing',
    layer1: 'neoecoprototype:item/storage_recolor/eco_cell_light_16m',
    layer2: 'neoecoprototype:item/storage_recolor/eco_cell_status_light'
  })
  custom.displayName('自定义外观 1K 钻石矩阵')

  // ---------- 7. 自产矩阵：不吃东西，按世界时间自己长货 ----------
  // grows(单批产量, 间隔刻)：每隔 ticksPerBatch 世界刻跳一次，每次给 perBatch 个，
  // 内容就是 itemType/fluidType/chemicalType 绑的那一个 key，任意 mod 的都可以。
  // 两个数都必须为正且不读服务端配置；往它推东西一律拒收。
  // 插进驱动器、被网络问过库存才开始计时，放在身上不算。
  event.create('lava_grower', 'neoecoprototype:storage_matrix')
    .fluidType('minecraft:lava')
    .grows(128, 600)
    .displayName('熔岩自产矩阵 | 每 600 刻 128')

  // 不写数量档位：bytes(...) / totalTypes(...) / size(...) 会被拒绝，它没有字节预算可描述。
  // 外观默认走无限家族（银灰混凝土外壳 + 无限那枚驱动器芯片），要换用 material(...)。
  event.create('hydrogen_grower', 'neoecoprototype:storage_matrix')
    .chemicalType('mekanism:hydrogen')
    .grows(1, 1200)
    .displayName('氢气自产矩阵 | 每 1200 刻 1')

  // ---------- 8. 跑在 eco 引擎上的无限供给矩阵 ----------
  // 和上面两只无限矩阵不是一个引擎：这只用的是 eco 自己的无限基础资源存储矩阵
  // （ECOInfiniteResourceCell），绑定的键由 ME 网络直接无限抽取，推进来的
  // 同类资源被直接销毁，别的键一律不收。
  // 至少绑一个键；supplies / suppliesFluid 可以写任意多个，顺序就是 tooltip 的顺序。
  event.create('infinite_base_resources', 'neoecoprototype:eco_supply_cell')
    .suppliesFluid('minecraft:water')
    .supplies('minecraft:cobblestone')
    .suppliesFluid('minecraft:lava')
    .displayName('无限基础资源矩阵')

  // 档位是 eco 的 l4 / l6 / l9（默认 l9），决定每类型成本与待机耗电。
  event.create('infinite_certus', 'neoecoprototype:eco_supply_cell')
    .supplies('ae2:certus_quartz_crystal')
    .tier('l4')
    .displayName('无限赛特斯石英矩阵')

  // 高级用法：绑既不是物品也不是流体的键（魔源、能量这类）。
  // 这类键裸 id 造不出来，键对象要脚本自己从那个模组的 API 取，所以这里只写形状：
  //   .suppliesKey(() => <那个模组给的键对象>)
  // 三种绑法可以混用，顺序就是 tooltip 的顺序；引擎对它们一视同仁。
})

// -----------------------------------------------------------------------------
// SERVER 部分 —— kubejs/server_scripts/neoeco_recipe_example.js
// -----------------------------------------------------------------------------

ServerEvents.recipes(event => {
  // ---------- 1. 删掉一条原版合成 ----------
  event.remove({ id: 'neoecoprototype:simplify_storage_component_4m' })

  // ---------- 2. 替换成自己的有序合成 ----------
  // 先 remove 再重新注册同一个输出，避免 ID 冲突。
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
  }).id('neoecoprototype:simplify_storage_component_4m')

  // ---------- 3. 给脚本矩阵补一条合成 ----------
  event.shaped('kubejs:stone_matrix_1k', ['AB', 'CD'], {
    A: 'ae2:quartz_glass',
    B: 'minecraft:stone',
    C: 'neoecoprototype:simplify_item_storage_matrix_housing',
    D: 'ae2:printed_logic_processor'
  }).id('kubejs:stone_matrix_1k_craft')

  // ---------- 4. 上游 neoecoae 的一体化工作站配方 ----------
  // 该配方类型由 Neo ECO AE Extension 提供，本模组没有自己的 schema，走原生 JSON。
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

  // 删除一体化工作站里的某条配方：按 ID 精确移除
  event.remove({ id: 'neoecoprototype:integrated_working_station/simplify_storage_component_4m' })
})

// ---------- 5. 调试命令：一次性把示例矩阵发到手里（OP 权限 2）----------
ServerEvents.commandRegistry(event => {
  const TEST_MATRICES = [
    'kubejs:stone_matrix_1k',
    'kubejs:water_matrix_4k',
    'kubejs:hydrogen_matrix_4k',
    'kubejs:infinite_glass',
    'kubejs:infinite_water',
    'kubejs:infinite_wool',
    'kubejs:pigmee_400_matrix',
    'kubejs:universal_matrix_1k',
    'kubejs:quantum_matrix_1k',
    'kubejs:custom_look_matrix_1k'
  ]

  event.register(event.commands.literal('neoeco_matrix_example')
    .requires(source => source.hasPermission(2))
    .executes(ctx => {
      const player = ctx.source.player
      if (!player) {
        return 0
      }
      TEST_MATRICES.forEach(id => {
        const stack = Item.of(id)
        if (!stack.isEmpty()) {
          player.give(stack)
        }
      })
      return 1
    }))
})

// =============================================================================
// 常见报错对照
//   Unknown storage matrix size         -> size() 只认 infinite/1k/4k/16k/64k/1m/4m
//   Infinite storage matrices only ...  -> 无限矩阵只支持 size('infinite') / tier('l1')
//   requires chemicalType(...)          -> 化学品矩阵漏了 chemicalType
//   needs AE2 Omni Cells ...            -> 全能/量子矩阵缺 ae2omnicells
//   Unknown Mekanism chemical           -> 化学品 ID 写错，或 Mekanism/AppMek 没装
//   Duplicate storage matrix id         -> 两个脚本注册了同名矩阵
// 提示：绑定是延迟解析的，脚本注册阶段目标物品还没入册不会报错，
//       进世界第一次用到才会求值并打印含脚本 ID 的错误。
// =============================================================================
