# L1 处理器装配器 自定义配方

配方类型 ID：

```text
neoecoprototype:processor_assembler
```

## 1. 这台机器是什么

L1 处理器装配器**就是 AE2 分子装配室的绿色变体**：方块实体直接继承 `MolecularAssemblerBlockEntity`，所以界面、九宫格、升级卡槽、进度动画、样板供应器对接全部是 AE2 原生的，本模组不另写一套。

本模组只加了一件事：**让装配室接受"处理样板"承载的自定义装配配方**（内置 13 条里只有四条是处理器——其中一条要求装 AE2 闪电科技，其余是别的物品，见 §6）。

AE2 原版只允许 `AECraftingPattern` / `AEStonecuttingPattern` / `AESmithingTablePattern` 进装配室，处理样板（`AEProcessingPattern`）会被拒收。我们覆写 `pushPattern`：当推来的样板能对上下面这种自定义配方时，就地包装成装配室认识的样板类型，交回父类照常合成。

- 不会出现在工作台；
- 不会改变原版 AE2 分子装配室；
- **本机不跑普通的分子装配配方**：只有能对上一条装配配方的样板才会开工；对不上的——不管它是处理样板还是原版合成样板——都会被拒收，其中单输出的样板还会在日志里留一行 WARN（见 §10）。

### 配方来源

**默认来源有两个**：本模组的 JSON 配方（内置 13 条，见 §6），以及从 AE2 压印器自动推导的配方。两者都会经过下面的排除列表。

推导规则（扫描 `ae2:inscriber`）：

1. 只取 `mode == press`、且三个输入槽都非空的配方——空槽凑不出装配器要的原料；
2. 把其中的"印刷件"输入替换成生产它那条 `mode == inscribe` 配方的 `middle` 材料（压印模不算消耗品）；
3. 只认**每次恰好产出一个**印刷件的 `inscribe` 配方：一次压出多个的批量配方没有整数材料成本可摊，不拿它当材料来源；
4. 同一个印刷件若有**多条 1:1 来源**，不猜——保留印刷件本身当原料；
5. 已经被 JSON 配方覆盖的组合（输出相同、原料集合相同）不再重复出现。

例如 AE2 的 `logic_processor` 压印配方实际吃的是 `printed_silicon + redstone + printed_logic_processor`，推导后变成 `c:silicon + redstone + c:ingots/gold`。这样任何模组往 AE2 压印器里加的 press 配方都能直接用，不需要写数据文件。

**JSON 优先**：同一条组合两边都有时用 JSON 那条；推导只补 JSON 没有的组合。

### 服务端配置

| 配置项 | 默认 | 作用 |
| --- | --- | --- |
| `derive_processor_recipes_from_inscriber` | `true` | 关掉后只认 JSON 配方 |
| `disabled_processor_recipes` | `[]`（空） | 按**输出物品 ID** 排除，JSON 与推导两个来源一起过滤 |
| `processor_assembler_max_runs_per_cycle` | `64` | 一个装配周期最多处理几份配方（范围 1~192，上限由配方形状决定）；样板要的份数超过它就整张拒收，原料留在网络里 |

排除列表存在的理由：**光删 JSON 没用**——AE2 压印器仍然提供同一条 press 配方，推导会把它加回来。要真正禁用某条处理器，必须走这个列表。

`processor_assembler_max_runs_per_cycle` 限的是**份数**（样板输出量 ÷ 配方 `result.count`），不是原料个数。出厂默认 `64` 就是**一次弹出的一整组**（每份产 1 个的处理器）：一个周期直接把 64 个装进弹出栈，不需要把余量再送回网络。想跑更宽的批次就往上调，范围的顶 192 就是配方本身决定的上限：三原料处理器每份吃 3 个，九格工作台只摆得下 576 个物品（9 格 × 每格 64 个），正好 192 份；四原料的配方上限是 144。再大的一定摆不下，所以配置没有比 192 更高的档位；调大之后超出弹出栈的那部分会自动进网络。要收紧就把这个值调小，填 `1` 就是一个周期只做一份。超过它的样板整张拒收并在日志里写明超了多少，不会只跑一部分、也不会吃掉超出的原料。

> ⚠️ NeoForge 不会用新默认值覆盖**已存在**的配置文件。老存档/老整合包里 `derive_processor_recipes_from_inscriber` 可能仍是旧的 `false`，需要手动改。

JEI 与 EMI 页面都跟随以上全部规则，只显示机器实际接受的组合。


## 2. 玩家怎么用

1. 在装配器旁边放一个**样板供应器**：AE2 原版那只、本模组的 L1 样板供应器、以及它的线缆版都能用（它只要求旁边是一台 AE2 认的合成机器）；
2. 在样板编码终端里编一张**处理样板**，内容对应下面任一条装配配方（3～4 样原料 + 一个输出）；也可以在 JEI 里打开该配方按 `+`，由本模组注册的传输器直接编进终端（重复原料会合并进一个槽、数量相加，机器认得这种形状）；
3. 网络请求该输出时，样板供应器把材料推给装配器，装配器合成后把产物送回网络。

**输入顺序无所谓**：配方是无序匹配的，样板里哪样材料占哪个槽都能对上。

材料组合必须命中一条已注册的配方，否则样板会被拒收——这就是"整合包作者能控制什么能做、什么不能做"的白名单。

在 JEI / EMI 里打开这台机器的分类页能看到全部可用组合，**包含从压印器推导出来的那些**；JEI 里对机器物品按 `U` 也能进来，物品说明写着"直接制作处理器，与分子装配室逻辑类似"。`+` 直接编码目前只有 JEI 有，EMI 那页只能看。

## 3. 配方文件位置

内置配方：

```text
src/main/resources/data/neoecoprototype/recipe/processor_assembler/*.json
```

数据包覆盖（推荐给整合包使用）：

```text
<存档>/datapacks/<你的包>/data/<命名空间>/recipe/processor_assembler/任意名字.json
```

文件名可以随意，`type` 字段决定配方属于哪台机器。内置配方共 13 条，见 §6；其中 `overload_processor.json` 带 `neoforge:mod_loaded` 条件，没装 AE2 闪电科技时那一条不加载，实际只有 12 条。

## 4. 字段说明

| 字段 | 必填 | 说明 |
| --- | --- | --- |
| `type` | 是 | 固定为 `neoecoprototype:processor_assembler` |
| `ingredients` | 是 | **3 或 4 个** `Ingredient` 的数组，匹配时不分顺序 |
| `result` | 是 | 输出物品，格式 `{ "id": "...", "count": N }`；`count` 省略时为 1 |

数组长度必须是 3 或 4，否则解析直接失败。

`result.count` 会参与样板换算：样板输出量必须是它的整数倍，否则对不上这条配方（见 §10）。

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

## 6. 内置配方（13 条）

四条处理器（`#` 开头的是标签，其余是物品 ID）：

```text
#c:silicon + minecraft:redstone + #c:ingots/gold
    -> ae2:logic_processor

#c:silicon + minecraft:redstone + ae2:certus_quartz_crystal
    -> ae2:calculation_processor

#c:silicon + minecraft:redstone + #c:gems/diamond
    -> ae2:engineering_processor

#c:silicon + minecraft:redstone + ae2lt:overload_crystal
    -> ae2lt:overload_processor        （装了 AE2 闪电科技才有这一条）
```

过载处理器另外还有一条从压印器推导出来的路径（`ae2lt:overload_circuit_board + 红石 + 硅`），两条都能用：机器是按样板里的原料去对配方的，两条各自对得上自己的原料，不冲突。

其余九条——这台机器同样是本模组的通用装配台，并不只做处理器：

```text
neoecoprototype:simplify_computation_cell_1m ×2 + neoecoprototype:simplify_storage_component_4m
    -> neoecoprototype:energized_computation_cell_4m

neoecoae:computation_threading_core_l4 ×2 + neoecoprototype:simplify_computation_casing
    -> neoecoprototype:energized_computation_threading_core

ae2:crystal_resonance_generator ×2 + ae2:interface + neoecoae:superconducting_processor
    -> neoecoprototype:simplify_powered_me_interface        （4 样原料）

minecraft:snow_block ×4
    -> minecraft:snowball ×4                                （4 样原料，一次出 4 个）

minecraft:snowball ×3 + minecraft:bucket
    -> minecraft:powder_snow_bucket

ae2:memory_card + neoecoae:aluminum_ingot + neoecoprototype:simplify_green_crystal_matrix
    -> neoecoprototype:simplify_structure_expansion_card

neoecoprototype:simplify_storage_controller + neoecoprototype:simplify_computation_system
    + neoecoprototype:simplify_crafting_system
    -> neoecoprototype:fumo_doll_reliqwq

neoecoae:storage_system_l4 + neoecoae:computation_system_l4 + neoecoae:crafting_system_l4
    -> neoecoprototype:fumo_doll_yang120

minecraft:pink_dye + minecraft:dried_kelp + minecraft:brown_mushroom
    -> neoecoprototype:fumo_doll_kouooki
```

所有 `+` 都表示"凑齐即可"，不区分先后；`×N` 表示同一个原料要 N 份。

## 7. 新增一条配方

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

数组可以是 3 项也可以是 4 项（§6 里的 `snow_block` 与 `simplify_powered_me_interface` 就是 4 项）；同一材料写几条相同的 ingredient 就表示要几份。

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
// 内置 13 条 + 本节示例新增 1 条 = 14（没装闪电科技时少 1 条）
ServerEvents.afterRecipes(event => {
  console.info(event.countRecipes({ type: 'neoecoprototype:processor_assembler' }))
})
```


## 9. 升级卡

因为机器就是 AE2 分子装配室，升级槽与卡片效果**全部沿用 AE2**：5 个槽，速度卡加快合成、能耗卡降低耗电，由 AE2 自己结算。本模组只在 `commonSetup` 里把这两种卡关联到本机器物品上（`Upgrades.add`），否则 AE2 不让插。

## 10. 注意事项与排查

- 机器按**输出物品**查找配方：同一种输出只会用第一条匹配的配方，别为同一输出写多条互相冲突的。
- 机器需要 ME 网络供电，且必须**由样板供应器推送**才会开工（继承自 AE2 装配室，没有"手动往格子里放材料就出货"的路径）。界面里那九格只是显示：手放不进去，JEI/EMI 往里拖的幽灵物品也不会写进机器。
- 样板的形状：
  - 输出必须是**一种物品**，数量必须是配方 `result.count` 的整数倍。
  - 输入随之按同一倍数走：样板输出量除以 `result.count` 得执行次数，每个输入槽的数量必须能被这个次数整除。只放大一边的样板（例如输入各 1、输出写 64）算不出整数，会被拒收。
  - 同一个原料被 AE2 合并进一个槽、用倍数表示时可以对上；一次推送的原料必须摆得进九格，摆不下整张拒收。
- 一次循环交出的产物最多一组（一个 `ItemStack`，受该物品最大堆叠数限制）；装不下的余量机器直接送进所在网络的存储，网络不收就掉在机器旁边。输入里的容器物品（如桶）不返还。
- 一个周期能跑几份另受服务端配置 `processor_assembler_max_runs_per_cycle` 限制（默认 64，范围 1~192，上限即三原料配方的九格上限）。默认值就是一次弹出的一整组；调小收紧（`1` = 一个周期一份），调大最多到 192。超过它的样板整张拒收并留一行 WARN（"样板一次要 N 份，超过本机每周期上限 M 份"）；这个数是**份数**，不是原料个数。
- 拒收不静默（单输出样板）：日志里会有一行 `[stonecutting assembler] refused a pattern at …` 的 WARN，写明输出物、样板类、每槽的 `[候选数x候选数量x批量倍数]`，以及这把样板是谁推来的；同一台机器、同一个输出只打一次。多输出样板在这台机器上直接对不上（它一次只产一种物品），这一类拒收没有日志。
- 数据包替换内置配方时，请让文件路径与内置文件同名，或先清掉原配方，避免同输出冲突。

## 11. 同一批配方在 eco 的大型集成工作站里也能跑

在 eco 的大型集成工作站里装上 **L1 处理器装配卡**，这批配方会同时成为工作站的一个模块族：

- 来源与装配室一致：数据包与 KubeJS 新增的 `neoecoprototype:processor_assembler` 配方会被自动改写进工作站；**压印器推导出来的那些不算**（它们不是配方表里的条目）；
- 工作站按 **64 次执行一批**结算：每种原料 ×64、产物 ×64，整批 64 AE（每次 1 AE）；面板上显示的 100 刻只用于显示与同批配方之间的取舍，实际节奏由工作站自己的超频决定；
- 装配室这边不受影响，1× 的处理样板照旧在装配室跑。
