# L1 处理器装配器 自定义配方

配方类型 ID：

```text
neoecoprototype:processor_assembler
```

## 1. 这台机器是什么

L1 处理器装配器**就是 AE2 分子装配室的绿色变体**：方块实体直接继承 `MolecularAssemblerBlockEntity`，所以界面、九宫格、升级卡槽、进度动画、样板供应器对接全部是 AE2 原生的，本模组不另写一套。

本模组只加了一件事：**让装配室接受"处理样板"承载的处理器配方**。

AE2 原版只允许 `AECraftingPattern` / `AEStonecuttingPattern` / `AESmithingTablePattern` 进装配室，处理样板（`AEProcessingPattern`）会被拒收。我们覆写 `pushPattern`：当推来的样板能对上下面这种自定义配方时，就地包装成装配室认识的样板类型，交回父类照常合成。

- 不会出现在工作台；
- 不会改变原版 AE2 分子装配室；
- 对不上任何一条配方的处理样板仍然会被拒收。

### 配方来源

**默认来源有两个**：本模组的 JSON 配方，以及从 AE2 压印器自动推导的配方。两者都会经过下面的排除列表。

推导规则：扫描 `ae2:inscriber`，只取 `mode == press` 且三个输入槽都非空的配方，并把其中的"印刷件"输入替换成生产它那条 `inscribe` 配方的 `middle` 材料（压印模不算消耗品）。例如 AE2 的 `logic_processor` 压印配方实际吃的是 `printed_silicon + redstone + printed_logic_processor`，推导后变成 `c:silicon + redstone + c:ingots/gold`。这样任何模组往 AE2 压印器里加的 press 配方都能直接用，不需要写数据文件。

### 服务端配置

| 配置项 | 默认 | 作用 |
| --- | --- | --- |
| `derive_processor_recipes_from_inscriber` | `true` | 关掉后只认 JSON 配方 |
| `disabled_processor_recipes` | `[]`（空） | 按**输出物品 ID** 排除，JSON 与推导两个来源一起过滤 |

排除列表存在的理由：**光删 JSON 没用**——AE2 压印器仍然提供同一条 press 配方，推导会把它加回来。要真正禁用某条处理器，必须走这个列表。

> ⚠️ NeoForge 不会用新默认值覆盖**已存在**的配置文件。老存档/老整合包里 `derive_processor_recipes_from_inscriber` 可能仍是旧的 `false`，需要手动改。

JEI 页面跟随以上全部规则，只显示机器实际接受的组合。


## 2. 玩家怎么用

1. 在装配器旁边放一个 AE2 样板供应器；
2. 在样板编码终端里编一个**处理样板**：输入放三样材料（各 1 个），输出放目标处理器（1 个）；
3. 网络请求该处理器时，样板供应器把材料推给装配器，装配器合成后把产物送回网络。

**输入顺序无所谓**：配方是无序匹配的，样板里三个输入槽谁先谁后都能对上。

材料组合必须命中一条已注册的配方，否则样板会被拒收——这就是"整合包作者能控制什么能做、什么不能做"的白名单。

在 JEI 里查看该机器（或按 `+` 悬停其物品）可以看到一页列出的全部可用组合，**包含从压印器推导出来的那些**，页面说明里也写清了"要先编成处理样板"。

## 3. 配方文件位置

内置配方：

```text
src/main/resources/data/neoecoprototype/recipe/processor_assembler/*.json
```

数据包覆盖（推荐给整合包使用）：

```text
<存档>/datapacks/<你的包>/data/<命名空间>/recipe/processor_assembler/任意名字.json
```

文件名可以随意，`type` 字段决定配方属于哪台机器。

## 4. 字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `type` | 是 | 固定为 `neoecoprototype:processor_assembler` |
| `ingredients` | 是 | **恰好 3 个** `Ingredient` 的数组，匹配时不分顺序 |
| `result` | 是 | 输出物品，格式 `{ "id": "...", "count": N }` |

数组长度必须是 3，否则解析直接失败。

加工时长**不在配方里**：它由 AE2 分子装配室自己的时序决定，受 AE2 速度卡影响。以前有个 `processing_ticks` 字段，因为机器改成继承装配室后已无法生效，所以整个删掉了。

每个元素都是标准 Minecraft `Ingredient`，可以写成：

```json
{ "item": "ae2:silicon" }
```

```json
{ "tag": "c:silicon" }
```

```json
[ { "item": "ae2:silicon" }, { "item": "ae2:silicon_crystal" } ]
```

## 5. 最小示例

```json
{
  "type": "neoecoprototype:processor_assembler",
  "ingredients": [
    { "item": "ae2:silicon" },
    { "item": "minecraft:redstone" },
    { "item": "minecraft:gold_ingot" }
  ],
  "result": { "id": "ae2:logic_processor", "count": 1 }
}
```

## 6. 内置的三条配方

```text
ae2:silicon + minecraft:redstone + minecraft:gold_ingot
    -> ae2:logic_processor

ae2:silicon + minecraft:redstone + ae2:certus_quartz_crystal
    -> ae2:calculation_processor

ae2:silicon + minecraft:redstone + minecraft:diamond
    -> ae2:engineering_processor
```

三者的 `+` 都表示"凑齐即可"，不区分先后。

## 7. 新增一个处理器配方

```json
{
  "type": "neoecoprototype:processor_assembler",
  "ingredients": [
    { "tag": "c:silicon" },
    { "tag": "c:dusts/redstone" },
    { "item": "minecraft:netherite_ingot" }
  ],
  "result": { "id": "some_mod:advanced_processor", "count": 1 }
}
```

允许重复材料，例如两份硅加一份红石：

```json
"ingredients": [
  { "tag": "c:silicon" },
  { "tag": "c:silicon" },
  { "tag": "c:dusts/redstone" }
]
```

## 8. KubeJS 写法

**新增配方用原始 JSON**（`event.custom`）：

```js
ServerEvents.recipes(event => {
  event.custom({
    type: 'neoecoprototype:processor_assembler',
    ingredients: [
      { item: 'ae2:silicon' },
      { item: 'minecraft:redstone' },
      { item: 'minecraft:emerald' }
    ],
    result: { id: 'ae2:engineering_processor', count: 1 }
  })
})
```

新增不需要任何额外注册（不需要 recipe factory）。KubeJS 对 `event.custom` 的处理是：按 schema 解析字段 → 所有脚本跑完后把配方重新序列化成 JSON 交回原版 `RecipeManager` → 用与内置数据包 JSON 相同的 `ProcessorAssemblerRecipe.Serializer` codec 解析成真实配方。机器与 JEI 看到的就是这条解析结果；字段解析失败时 KubeJS 控制台会明确报错，不会静默丢弃。

**删除与匹配可以按字段写**，因为配方类型注册了 KubeJS `RecipeSchema`，KubeJS 知道 `ingredients` 是输入、`result` 是输出：

```js
ServerEvents.recipes(event => {
  // 删单条
  event.remove({ id: 'neoecoprototype:processor_assembler/calculation_processor' })

  // 按产物删
  event.remove({ type: 'neoecoprototype:processor_assembler', output: 'ae2:logic_processor' })

  // 按输入删（凑齐即匹配，不分顺序）
  event.remove({ type: 'neoecoprototype:processor_assembler', input: 'minecraft:diamond' })

  // 整个类型清空（只影响 JSON 来源）
  event.remove({ type: 'neoecoprototype:processor_assembler' })
})
```

> 该类型**不提供 builder 语法**（`event.recipes.neoecoprototype.processorAssembler(...)`）。KubeJS 无法把 JS 数组隐式转成本配方要求的 `List<Ingredient>`，所以 schema 里刻意不声明构造器，新增一律走上面的 `event.custom`。

> ⚠️ **KubeJS 管不到压印器推导那一侧。** 推导是运行时扫 `ae2:inscriber` 得到的，`event.remove` 删不掉它。要真正禁用某个处理器，用服务端配置 `disabled_processor_recipes`（按输出物品 ID 排除，JSON 与推导一起过滤）；要彻底关掉推导，用 `derive_processor_recipes_from_inscriber = false`。

> `ServerEvents.recipes` 事件内，`countRecipes` 和按字段 `remove` 只作用于数据包来源的配方；脚本里 `event.custom` 新增的配方要等所有脚本跑完才会并入配方表（KubeJS 7.2 的既定行为）。想在加载完成后断言新增是否生效，用 `ServerEvents.afterRecipes`，它的 `countRecipes` 读的是最终配方表：

```js
// 内置 3 条 + 本节示例新增 1 条 = 4
ServerEvents.afterRecipes(event => {
  console.info(event.countRecipes({ type: 'neoecoprototype:processor_assembler' }))
})
```


## 9. 升级卡

因为机器就是 AE2 分子装配室，升级槽与卡片效果**全部沿用 AE2**：5 个槽，速度卡加快合成、能耗卡降低耗电，由 AE2 自己结算。本模组只在 `commonSetup` 里把这两种卡关联到本机器物品上（`Upgrades.add`），否则 AE2 不让插。

## 10. 注意事项

- 机器按**输出物品**查找配方：同一种输出只会用第一条匹配的配方，别为同一输出写多条互相冲突的。
- 机器需要 ME 网络供电，且必须**由样板供应器推送**才会开工（继承自 AE2 装配室，没有"手动往格子里放材料就出货"的路径）。
- 处理样板的每种输入数量必须是 1、输出必须是 1 个物品，且三个输入槽都要有物品，否则不会被识别。
- 数据包替换内置配方时，请让文件路径与内置文件同名，或先清掉原配方，避免同输出冲突。
