# Changelog

## 1.2.11 (2026-09-29) - 主机不再带着成型状态落地，镜像主机的动画转向改正

### 修复 / Fixed

- **手放计算主机不再闪一下"已成型"的外壳**：`SimplifyComputationSystemBlock` 之前用 `registerDefaultState(getStateDefinition().any().setValue(COMMUNICATION_INTERFACE, false))` 注册默认状态，而 `StateDefinition.any()` 给每个属性的是它值列表的**第一个值**，对布尔量就是 `true` —— 于是主机自己的默认状态就是 `formed=true / mirrored=true / network_switch=true / high_energy_network_switch=true`，放进空气那一刻带着这四个旗标落地，集群下一帧才纠正回去，玩家看到的就是"一块从没成型过的主机闪了一下成型面"。判据指纹：落地状态里唯一为 false 的布尔，恰好是我们自己写的那一个。F1 的合成主机早就用的 `defaultBlockState()`（继承父类注册好的默认值），所以它从来不闪；1.2.7 的主机方块是 6 行空壳、根本没有这个覆盖 —— 这条回归是 1.2.8 加 `communication_interface` 时带进来的。新增一条常驻守卫：我们注册的任何一个方块，默认状态里不许有布尔量为 true。 A hand-placed C1 host no longer flashes a formed face: `StateDefinition.any()` gives every property the first value in its list, which for a boolean is `true`, so the host's own default state claimed all four of `formed / mirrored / network_switch / high_energy_network_switch` and the cluster had to correct them a frame later. F1 was written against `defaultBlockState()` and never flickered; 1.2.7 had no override at all, which dates the regression to 1.2.8.
- **镜像的 C1 主机动画转向改正**：`controller_formed_base_mirrored` 把两张动画贴图（`#coolant`、`#screen`）按 `u0 > u1` 采样，等于把动图水平翻转 —— 翻转一张转动的图就是反转转向，所以镜像机转的方向和正装机不一样。现在几何与静态底图继续镜像，8 个动画面改用正装机那套 uv，镜像机就和正装机同向。**这一条是故意偏离 eco**：成型面底图与 eco 差 0 像素、55 个面连同 uv 全等、11 张动画 `.mcmeta` 全同、`mirrored` 判定与 eco 发布 jar 字节码同序 —— eco 自己的 C4 镜像机同样会反，是他拍板"镜像机也该正转"。散热控制器的 mirrored 模型有同一处翻转，但它是另画的一套几何（面板从 z=2/30 挪到 z=14/-14、north/south 互换），要逐面重新配对，这次没动。 A mirrored C1 host now turns the same way as a plain one: the mirrored model sampled both animated textures with flipped uv, and flipping a turning picture reverses the turn. This one is a deliberate divergence from upstream, whose own C4 behaves the same way.

### 已知 / Known

- **成型后机壳仍然全部不画**（计算家族）：`NEComputationCluster.hideAllCasingsWhenFormed()` 在发布 jar 里编的是 `iconst_1`，而 eco 的存储簇/合成簇走的是另一条（`getCasingHideOrigin()` + 距主机 `distSqr <= 3`，只藏够得着的）。我们试过把计算簇也换成那条规则（簇子类 + 一个注到 `createCluster` 的 mixin，并且有一条"不生效就会红"的测试证明它确实生效了），结果是**远端那列机壳画成了一整块普通立方，和散热控制器成型模型外伸的面板共面 -> 闪烁 + 贴图不对**。原因是两家的机壳 blockstate 都只有一条通配键、只有一个模型，`formed`/`invisible` 根本不参与选模型。所以"成型后全藏机壳"在计算家族是**承重**的，改动已撤回。要真修，得先给远端机壳一个成型后可看的模型（美术活），或者让散热控制器的成型模型不伸进那一格。

## 1.2.10 (2026-09-29) - 计算主机回到 eco 的方块实体，CPU 面板恢复

### 修复 / Fixed

- **成型并且接了网的 C1 计算主机重新出现在 AE2 的 CPU 列表里**：1.2.8 我们把主机的方块实体换成 eco 那个类的子类，为的是在主机自己的方块状态上发布成型外观；这一换让它从 AE2 的机器表里消失了。原因是 AE2 登记节点的键就是 `node.getOwner().getClass()`，**只有这一个键**（`Grid.add` 里只有一个 `put`，不沿父类链登记），而 eco 收集计算集群用的是 `getMachines(ECOComputationSystemBlockEntity.class)`——Map 的键相等查找，不是 `instanceof`。同一台主机、同一个 grid 换四个键实测：按 eco 的类查 = 0，按我们的子类查 = 1，再往上的两层父类查 = 0。现在主机的方块实体退回 eco 的类，那两件事改由 `publishShape()` 加方块自己的 scheduled tick 接手（发布 `communication_interface`、把 eco 的两个交换布尔钉成 false）——写方块不能发生在 AE2 的集群重算栈里。附带回来的还有：eco 在节点入网时按同一个类读的「快速规划 / 循环规划」开关，现在在我们的主机上也生效了（tooltip 里能看到那两行）。 A formed, networked C1 host is back in AE2's CPU list. In 1.2.8 we swapped the host's block entity for a subclass of eco's, to publish formed-appearance state, and that quietly removed it from AE2's machine table: AE2 files nodes under `owner.getClass()` only, while eco collects clusters with `getMachines(ECOComputationSystemBlockEntity.class)` -- a key lookup, not an `instanceof` test. Measured on one grid: eco's class 0, our class 1, both parent classes 0.
- **老世界里被点亮成「有交换器」的 C1 会自愈**：我们的计算器整条替换了 eco 的 `verifyInternalStructure`，而 eco 只在那里面清 `network_switch` / `high_energy_network_switch`，所以 1.2.8 之前留下的 true 会一直留着，把 CPU 送进一条不存在的逻辑网络。现在每次集群重算都会投递一次纠正，方块在下一个 tick 把 true 写成 false（已经是 false 时一个字都不写）。守卫：把两个布尔人为设成 true 打在成型主机上，要求 120 tick 内被清掉。
- **五个方块的方块状态不再空转四个朝向**：`energized_computation_core`（整方块、六面同图）、`fumo_reliqwq`（模型里没有元素，画面由渲染器给）、以及三件 Trinity 模块（`cube_all`）——它们的模型四个朝向看起来完全一样，却各写了 4 到 8 条带旋转的条目，合计 32 条永远不可能有差异的键。现在每个文件一条通配键。Five blocks enumerated four rotations their models cannot show; 32 entries became 5.

- **两条新守卫**：接线可达性（玻璃线缆接在接口块外侧，能把主机、线缆、供电块并进同一个 92 节点 grid；主机自己六面都被结构占着，而 eco 不把它朝空气那一面暴露）与 CPU 列表可见性（成型主机必须出现在 `getCpus()` 里——这条从「记录缺陷」转回正式门禁）。

### 新增 / Added

- **F1 合成主机的稀有脸 `has_mind`**：每台主机成型时掷一次 1/16，结果存进主机，重新成型不重掷；掷中的那台成型面改用美术新加的 `_face` 贴图（4 张新模型：镜像 × 是否通讯接口）。
- **美术重绘的 F1 合成子系统整套并入**：外壳、监视器、总线、通风口、样板总线、成型面等 51 个贴图文件（28 张替换现有路径，23 张新增）。

### 变更 / Changed

- **C1 不再发布 `energized_threading_core` / `energized_parallel_core` 两个方块状态属性**（**这条更正 1.2.9 里「资源包与覆写」一节的说法**）。它们从来没有选出过不同的外观——那 14 张组合模型都是普通成型面的复制——换来的只有一个 512 条键的 blockstate 文件（其中 20 条说的是同一件事）和两个资源包可能撞上的属性名。现在成型面只按「镜像 × 是否通讯接口」选，文件 79 行 / 20 条键。带这两个键的第三方条目会匹配不到任何状态（键是子集匹配，没写的属性是通配），所以不报错，只是那层区分失效。
- **盈能强化计算机核心从主机旁边的外壳位挪到主机正后方**。旁边那一格是 eco 探测网络交换器的位置，核心站在那儿会让上游把主机点亮成「有交换器」。**玩家可见代价**：老世界里把核心摆在主机旁边的 C1 会散架（不是丢方块，是成型判定不再通过），把那一格换成对应外壳方块、或把核心挪到主机正后方即可。

### 资源包与覆写 / For resource pack authors

- C1 的成型外观只按 `formed` / `mirrored` / `communication_interface` 三个键选，`energized_threading_core` 与 `energized_parallel_core` 已删除。F1 新增 `has_mind`：`true` 且 `formed=true` 时选 `_face` 那张。
- 成员方块成型后仍然一律不画自己，外观由主机的方块状态决定。

### 需要 / Requires

- NeoForge `21.1.x` on Minecraft `1.21.1`，Applied Energistics 2 `19.2.17` 或更新，**Neo ECO AE Extension `21.2.0` 或更新**。依赖门槛与 1.2.8 相同，本版没有变化。

## 1.2.9.2 (2026-09-28) - 主机上下两格不再白送成员

### 修复 / Fixed

- **C1 计算子系统与 F1 合成子系统可以被"多塞一颗核心"**：几何有四条外壳走位，但**没有一条经过主机自己所在的那一列**，所以主机正上方与正下方两格从来没被检查过；而集群是按边界框收编成员的，只要把线程核心（或并行核心）摆在那两格，机器照样成型、成员照样算数 —— 等于白送线程数与吞吐，而这两格是一键搭建永远不会产生的位置。现在这两格必须是外壳方块，与 L1 存储主机早就有的规则一致，三台机器行为统一。盈能强化计算机核心此前那条"只挡这一种方块"的特例被这条通用规则包含，不再单独存在。 Four geometry walks cover every shell column but the one the controller stands in, so the two cells above and below the host were never looked at while the cluster still adopted anything inside its bounds: parking a threading core or a parallel core there was free threads and free throughput. Those two cells are now shell, exactly as the storage host has always required, so all three machines behave the same.
- **代价（玩家可见）**：老世界里如果有人把 C1 / F1 主机正上或正下方放过别的东西，升级后那台机器会**散架** —— 不是丢方块，是成型判定不再通过，把那一格换成对应的外壳方块即可重新成型。 Existing worlds that had a block above or below the computation or crafting host lose formation until that cell is a casing again.
- **四条新守卫测试**：C1 上下各一条（放线程核心）、F1 上下各一条（放并行核心），都要求机器拒绝成型；测试会先确认那一格在搭建方案里确实是外壳，所以"换掉它"是唯一变量。

### 需要 / Requires

- NeoForge `21.1.x` on Minecraft `1.21.1`，Applied Energistics 2 `19.2.17` 或更新，**Neo ECO AE Extension `21.2.0` 或更新**。

## 1.2.9.1 (2026-09-28) - Startup Crash In Large Packs

### 修复 / Fixed

- **修好 1.2.9 在大整合包里进不去游戏的启动崩溃**：我们把三个 AE2 部件模型注册放在 `FMLClientSetupEvent` 里，而 AE2 的 `PartModels.registerModels` 一旦集合被冻结就抛 `Cannot register models after the pre-initialization phase!`。关键点是**那个冻结是惰性发生的**（AE2 自己的 `ModelEvent.RegisterAdditional` 处理器，或任何一次 `CableBusModel.getDependencies()` 解析模型），而 NeoForge **并行派发 setup 事件**（崩溃报告里的 `ModLoader.dispatchParallelEvent`），所以"我们跑在冻结之前"从来不是保证，只是开发环境里抢跑赢了；包里有 `quick-pack` / RenderJS / KubeJS 这类会提前碰模型的模组时就会输。注册现在挪到 `FMLCommonSetupEvent` —— 所有模组的 common setup 必然早于任何模组的 client setup，也就必然早于任何模型解析，这是生命周期顺序给的保证而不是运气。旁证：MegaCells 在注册表定义期注册部件模型，ExtendedAE 也在更早的专用处理器里注册，没有人在 client setup 做这件事。**1.2.9 的用户请直接升这一版。**

### 说明 / Notes

- 依赖没有变化：NeoForge `21.1.x` / Minecraft `1.21.1`、Applied Energistics 2 `19.2.17` 或更新、Neo ECO AE Extension `21.2.0` 或更新。
- 这一版没有内容改动，1.2.9 的其余说明（接口语义互换、L1 新美术、内置旧美术回退包）仍然适用。

## 1.2.9 (2026-09-28) - Interface Roles Aligned With Their Names, L1 Artwork Replaced

### 变更 / Changed

- **「通讯接口」现在就是能打开界面的那块，三台机器一起**：显示名、右键行为、一键搭建默认放的方块、配方链、主机发布的 `communication_interface` 全部同时对齐。`simplify_<machine>_interface`（「…接口」）现在是有序合成的基础件、右键无反应、搭建时放的是它；`simplify_<machine>_network_interface`（「…通讯接口」）= 接口 + `ae2:terminal`，右键打开 eco 的界面。**注册 ID 与存档数据一个字未改**，老世界的方块不会丢。**玩家可见的代价**：库存里这两件物品的含义互换了 - 谁原来有一块能开界面的「接口」，升级后它是开不了界面的那一件，要去合成一份通讯接口。F1 这一对的名字在 1.2.8 与 1.2.9 之间各掉头一次，理由是 eco 那样叫只是因为它是唯一一个接口方块。**收益**：方块状态属性 `communication_interface` 的名字终于和显示名一致，美术为「放了通讯接口」画的那张主机成型面（`controller_l4_formed_network`）也绑到了它本该绑的状态上。
- **成型后的成员方块一律不画自己，外观由主机的方块状态决定**：C1 那个「接口成型后还自绘」的历史遗留钩子（`hideWhenFormed()` 覆写 + `computation_interface_formed` 模型 + `formed` 两条键）删掉了，三台六块接口现在行为一致。**这条更正 1.2.8 发版说明里的一句话**：那里写「谁覆写过这三份主机文件，他的副本缺新键 → 方块直接隐形」，实测是错的 - `variants` 的键是**子集匹配**，没写的属性是通配，所以旧副本照样能解析所有状态（v1.2.7 那份 16 条键覆盖当前 32 个状态，不重不漏），只是表达不出新的区分。

### 新增 / Added

- **旧美术做成了游戏里可切换的资源包**：上一个正式版的模型、贴图、blockstate 现在随 jar 一起发布，在资源包列表里叫「Neo ECO Prototype 旧美术（上一个正式版）」，**默认不启用**，所以升级不会改变任何人的机器外观。它带着自己的 blockstate，因此不需要为每次新增的模型路径补别名；`tools/legacy_pack.py <tag>` 一条命令从任意 tag 重切。
- **美术重绘的 L1 存储子系统整套并入**：驱动器、主机成型面（含镜像 × 是否通讯接口四格）、外壳、能源池、通风口、样板供应器部件、若干物品贴图。

### 修复 / Fixed

- **驱动器方块状态整份失效**：变体键里逗号后的空格（`"facing=east, formed=false"`）会让游戏把属性名读成 `" formed"` 并**整条丢弃**该键，所以成型与未成型共用一张图。去空格后 12 条键对 48 个状态恰好一对一（24 未成型 / 12 成型空 / 12 有盘）。GameTest 里新增守卫：任何键引用了方块没有的属性或非法取值就直接失败，正是这一类 bug。
- **盈能计算核心成型后闪烁**：它成型后不再画自己（`hideWhenFormed()`），因为控制器的成型模型实测覆盖整面 3×3（x/y −16..32），成员那一格与它严格共面、两个同深度 quad 抢深度。旧成型模型与其 blockstate 键一并删除，没有路径能再画出它。**已知代价**：这个方块外壳位与并行列两吃，而一个方块只有一个 render shape，所以两处成型后都会消失；要「站列里还画着」得拆成两个方块。

### 资源包与覆写 / For resource pack authors

- **成型后的外观请改主机的 blockstate，不要改成员方块**：成员方块成型后不画自己（eco 对所有外壳位成员都是这个规矩）。主机状态：`communication_interface`（格子里放的是通讯接口）、`energized_parallel_core` / `energized_threading_core`（C1）、`mirrored`、`formed`。
- **存储主机的成型外观挂点换了方案**：1.2.8 说明里写的 `controller_formed_a<后缀>`（后缀由 `c`/`t`/`p` 拼）对**存储主机已经不成立** - 现在按「镜像 × 是否通讯接口」分四张：`controller_formed` / `controller_formed_switch` / `controller_formed_network` / `controller_formed_network_switch`，灯层同名加 `_light_a`。计算与合成两台还是 `a`/`c`/`t`/`p` 那套。
- **`simplify_casing.json` 的几何是内联的**：它不再 parent `neoecoae:block/casing_base`（本仓库 CI 禁止 addon 资源引用上游命名空间），13 个元素直接写在文件里，出处记在 `THIRD_PARTY_NOTICES.md`。想改铝板外形改这个文件即可。

### 需要 / Requires

- NeoForge `21.1.x` on Minecraft `1.21.1`，Applied Energistics 2 `19.2.17` 或更新，**Neo ECO AE Extension `21.2.0` 或更新**。停在 `21.2.0-beta4` ~ 裸 `beta6` 的玩家请继续用 1.2.8 或更早。

## 1.2.8 (2026-09-27) - Interface Naming Alignment and Formed Appearance States

### 依赖门槛抬高 / Raised dependency floor

- **这一版起需要 Neo ECO AE Extension 21.2.0 或更新，且不再兼容 21.2.0-beta4 ~ beta6。** eco 在 21.2.0 之前把 `StorageHostActionUI$Config` 的最后一个参数从 `Runnable` 换成了 `Consumer<Boolean>`（实测 `beta6-hotfix2`、`beta7-hotfix1` 与正式版都已是新签名，裸 `beta4`~`beta6` 仍是旧签名），所以 1.2.7 配 21.2.0 打开 L1 存储主机会 `NoSuchMethodError` 崩服，1.2.8 反过来配裸 beta6 也会以同样方式崩。**停在 beta4 ~ beta6 的玩家请继续用 1.2.7。** 版本区间不是保险索：已有玩家用的启动器会跳过依赖检查，直接把不兼容的组合加载起来。 This build needs eco 21.2.0 or newer and drops plain 21.2.0-beta4 ~ beta6; the constructor type changed underneath us, so either direction mismatched crashes when the L1 storage host GUI opens.

### 新增 / Added

- **成型外观改由方块状态选择 / formed appearance picked by block state**：三台主机各自发布"接口格里放的是不是通讯接口"（`communication_interface`），C1 计算主机另外发布 `energized_threading_core` 与 `energized_parallel_core`；成型模型的挑选全部落在 blockstate 文件里，资源包不需要读世界。新增 18 个变体模型作为分图钩子，目前全部指向现有成型贴图，所以任何组合下看到的都还是现在的正式外观。 Publishes what the finished machine holds onto the host's own block state so the blockstate file picks the formed model; 18 variant models are the art hooks and all still point at the current sheet, so nothing looks provisional.
- **六门新守卫 / six new guards**：装配室往供应器推产物的能力面、主机正上下格的盈能核心、三台主机的状态发布，以及 `variants` 穷举与 multipart "每状态恰好命中一条"的文件覆盖检查。 Adds GameTests for the provider capability, the host-column geometry hole, the published shape of each host, and full blockstate coverage.
- **装配室批量样板说明 / guide section**：GuideMe 中英双语补充"整批进料一次合成、产物随份数翻倍、上限一叠 64、耗时不变"。 Documents batched patterns in both guide languages.

### 修复 / Fixed

- **装配室产物卡在供应器**：给 L1 样板供应器的方块实体与线缆部件注册 `AECapabilities.GENERIC_INTERNAL_INV`，交给 AE2 自己的最低优先级钩子包成 `ItemHandler`。此前只有 AE2 原版供应器回答该能力，装配室完工时推不出产物。 Registers the capability AE2 actually looks for, so assemblers can hand their product to our provider.
- **F1 合成子系统那对接口与 eco 的命名对齐**：`simplify_crafting_interface` 现在是"F1 合成子系统通讯接口"并且能打开接口界面，`simplify_crafting_network_interface` 变成"F1 合成子系统接口"（只挂网络端点、右键无反应）。**注册名与存档数据一个字未改**，只换行为与显示名，老世界的方块不会丢失，所以这两块的名字与界面归属会互换。 Aligns F1's interface pair with eco's own naming by swapping behaviour and display names only; registry names are untouched, so worlds keep their blocks but the two names trade places.
- **盈能强化计算机核心成型后闪烁**：删掉它的方块实体渲染器，成型外观改由 blockstate 换整块模型，与其余外壳位成员的 `RenderShape.INVISIBLE` 规矩一致，不再出现内外两侧同时可见。 Retires the block entity renderer behind the formed model, which also removes the flicker.
- **第二颗盈能核心被悄悄收编**：计算集群几何补上有界守卫，主机正上方/正下方或采纳盒内多出一颗盈能核心时不再成型。 Rejects an energized core standing in the host column or anywhere the cluster would adopt.
- **L1 样板供应器与两个接口的界面错位**：补回贴图里被贴错位置造成的整行透明空洞、把 ↓ 箭头只留在配置行、并修掉扩成两行后配置组与存储组抢同一行导致"存储行放不了东西"的问题。 Fixes the provider/interface GUI textures and slot layout.

### 已知 / Known

- **三个接口界面的标题未本地化**：eco 21.2.0-beta6 改了生成 lambda 的序号，我们钉 `lambda$create$1` 的 `@Redirect` 没命中（`require = 0`，所以不崩），标题仍显示 eco 默认文案。 The interface UI title redirects no longer match beta6; cosmetic only.

### 资源包与覆写 / For resource pack authors

- **三份主机 blockstate 结构变了，条目数必须跟着变**：`simplify_computation_system` 现在是 8 个属性穷举出 512 条，`simplify_storage_controller` 4 个属性 32 条，`simplify_crafting_system` 是 20 条 multipart 且每个 `formed` 条件都钉住 `communication_interface`（不钉住就会有一个状态同时命中两条、同一格画两遍模型）。`simplify_computation_interface` 拆成 `formed=false` / `formed=true` 两态，好让成型的通讯接口带上自己的成型外观；另外两份只是改指向自己的模型文件，不再与隔壁共用。 The three host files now have to name every state, and the interface files are split per role.
- **老世界不受影响**：方块状态是按"属性名=值"存进区块 palette 的，新增属性回读即默认 `false`，blockstate 文件本身不进存档；升上来看到的还是升级前那台机器。 Existing worlds are untouched - a property that did not exist yet reads back as its default.
- **但覆写会静默失效**：谁之前替换过这三份主机文件，他的副本没有新键，那些没被命名的状态**匹配不到任何模型 = 方块直接隐形**（不会回退到我们的文件）。请拿新版文件重做覆盖，不要在旧副本上打补丁。 Overrides break quietly: an unnamed state resolves to no model at all, which renders the block invisible rather than falling back.
- **成型外观的挂点**：`textures/block/<机器>_recolor/controller_formed/controller_formed_a<后缀>.png`，后缀由 `c`（接口格里是通讯接口）、`t`（盈能线程核心）、`p`（盈能并行核心）拼接，`_c_t_p` 是三者全有、无后缀是三者全无。18 个变体模型已经在 `models/block/<机器>_controller/` 下建好并且目前全部指向 `controller_formed_a`，所以给某个组合换外观只改一处贴图引用。 The 18 variant models exist and all still point at the current sheet, so a new look is one reference change.

## 1.2.7 (2026-09-24) - L1 Computation and Interface Update

### 新增 / Added

- **L1 计算系统扩展 / L1 computation system expansion**：新增盈能强化计算机核心、盈能强化线程核心和盈能 4M 计算盘；支持 L1 计算集群的专用成员槽位、并行能力和线程能力。 Adds the energized computation core, energized threading core, and energized 4M computation cell with dedicated L1 cluster slots, parallelism, and threading behavior.
- **L1 计算系统配方 / L1 computation recipes**：新增处理器装配室和 eco 集成工作站配方，使用超导处理器、盈能超导锭与极寒凌冰等材料。 Adds processor-assembler and ECO integrated-working-station recipes using superconducting processors, energized superconductive ingots, and Cryotheum Crystals.
- **L1 供电 ME 接口 / L1 powered ME interface**：新增方块版与线缆 Part 版，保持 18 个 CONFIG / 18 个 STORAGE 槽位，提供 200 AE/t 被动供电。 Adds block and cable-Part forms with 18 CONFIG slots, 18 STORAGE slots, and 200 AE/t passive generation.
- **盈能超导接口 / energized superconductive interface**：新增方块版与线缆 Part 版，每个标记槽最多配置 8192 个物品、512,000 mB 流体或化学品。 Adds block and cable-Part forms with a per-marker limit of 8,192 items or 512,000 mB of fluids or chemicals.
- **L1 接口配方 / L1 interface recipes**：L1 供电接口改由 L1 处理器装配室制作；盈能超导接口改由 eco 集成工作站制作并消耗 10,000 FE。 The powered interface is made in the L1 processor assembler; the superconductive interface is made in the ECO integrated working station and consumes 10,000 FE.
- **双语发布文档 / bilingual release documentation**：补充计算系统、接口、配方和兼容性说明。 Adds bilingual documentation for the computation system, interfaces, recipes, and compatibility requirements.
- **L1 数值配置项 / extra config entry**：`l1_computation.energized_cell_total_bytes`，默认 `5242880`（原 4 MiB 加 30%），单独控制盈能闪存增强（CE1R）的合成存储字节数。 Adds a config entry for the energized cell's storage bytes.
- **盈能盘物品栏贴图 / item-icon-only difference**：CE1R 不再与 CE1 共用同一张物品模型，改为一张只把绿色信号换成我们蓝色的独立贴图；插进驱动器之后两者外观仍然一致。 Gives the energized cell its own inventory texture instead of sharing the plain cell's model.
- **外壳位成员的成型外观 / shell member rendering**：盈能强化计算机核心成型后不再画方块模型（与 eco 所有外壳位成员一致：成型外壳是 `RenderShape.INVISIBLE` 且不再替邻居遮面，成员继续画就会从内外两侧同时可见而闪烁），那一格的表面改由 eco 的 section-geometry 钩子画回。 Hides the energized core's block model once formed, like every eco shell member, and paints the face back through eco's section-geometry hook.

### 修复 / Fixed

- **计算集群左右槽位判断**：改用 AE2 `IOrientationStrategy` 与 `RelativeSide.LEFT/RIGHT`，并正确处理镜像结构，不再使用手工逆时针方向推导。
- **beta6 启动兼容性**：移除 beta6 已不存在的 `onReady` Mixin 注入，保留 L1 计算系统的结构定义覆盖。
- **GameTest 异步失败处理**：失败断言改为显式 `fail + return`，避免失败后继续执行导致测试批次静默卡住。
- **接口与样板供应器界面**：修正自定义六行布局、物品栏背景贴图偏移和样板供应器物品栏位置。
- **线缆 Part 渲染**：按 AE2 Part 几何约定区分端面、侧面、背面和状态覆盖层，修正反向显示背板材质的问题。

### 变更 / Changed

- **处理器装配室输入数量**：从固定 3 个输入扩展为支持 3 或 4 个无序输入，兼容原有处理器配方。
- **Part 模型风格**：样板供应器、L1 供电接口和盈能超导接口沿用 AE2 的端盖、主体侧面和状态灯分层模型。
- **版本与开发基线**：项目版本为 `1.2.7`（先前误写的 `1.2.8` 从未打过 tag、从未发布，已更正），开发环境同步 NeoForge `21.1.251` 与 Neo ECO AE Extension `21.2.0-beta6`；**发布下限仍是 eco `21.2.0-beta4`**，我们没有用到任何比 beta4 更新的 API。
- **开发规范**：新增 eco 多方块朝向、镜像位置和 GameTest 失败处理规则，要求先读 eco，再读 AE2，最后参考原版。

### 验证 / Verification

- `./gradlew build` 通过；`runGameTestServer` **37 条全部通过**（1.9 秒）。
- 客户端在 beta6 下正常启动到标题界面，日志中 `MixinApplyError` / `InvalidInjectionException` /
  `Missing config translation` 均为 0 次。
- 未覆盖：eco 接口 GUI 的标题重定向只在**打开界面**时才会被应用，GameTest 从不加载这些类，
  所以那一条还需要实机开一次存储/合成接口确认。

## 1.2.6 (2026-09-24)

### 新增 / Added

- **L1 供能接口**（`simplify_powered_me_interface`）与**盈能超导接口**（`superconductive_interface`）：
  各带方块与线缆部件两种形态。供能接口 18 标记槽 / 18 库存槽（原版 9），通电且有频道时以
  200 AE/t 被动发电；超导接口同样的翻倍布局，单个标记可囤 8192 个物品 / 512,000 mB 流体 /
  512,000 mB 化学品，输出功率 4000 AE/t。AE2 每个网格只保留输出功率最高的一台被动发电源。
- **盈能强化计算机核心**（`energized_computation_core`）：每块 **1024 并行**，可以顶掉一格外壳
  站位，也可以进并行核心列。
- **盈能强化线程核心**（`energized_computation_threading_core`）：每块 **16 线程**，只接受离控制器
  最近的那一格 —— 放在别处整条线校验不过，结构直接不成形。
- **L1 计算数值进服务端配置**：`l1_computation` 段下 `cpu_threads`（默认 2）、
  `cpu_accelerators`（默认 24）、`cpu_total_bytes`（默认 1,572,864，即 4M 盘位加量 50%）。
  只影响 L1 一档，加量不加配方价格。
- **盈能 4M 计算盘**（`energized_computation_cell_4m`）。
- 上面三件盈能计算成员（核心 / 线程核心 / 4M 盘）**只有创造模式入口，暂无合成配方**，
  配方等数值定下来再补。
- **样板供应器方块 ↔ 线缆部件互转配方**（`pattern_provider_alt.json`），补全原来只有单向的一对。

### 修复 / Fixed

- **计算驱动器成型动画缺失**：`simplify_computation_drive` 的 blockstate 把 `formed` 的取值写成了
  `False`/`True`，属性匹配不上 → 驱动器变成紫黑缺失方块，成型后也没有点亮动画。已改为小写
  `true`/`false` 并补上 `computation_drive_full` 模型。
- **超导接口指南页两条红字**：`<ItemIcon>` 不能作为 `<Row>` 的块级子元素（本 GuideMe 版本会报
  Unhandled MDX element），已移进 `<ItemGrid>`；`<RecipeFor>` 查不到配方是因为集成工作站的
  `neoecoae:integrated_working_station` 类型渲染不出来，已补上方块与部件之间的无损互转配方，
  并在页面里写明配方图显示的是哪一条。

### 变更 / Changed

- **盈能核心成型后不再由方块模型绘制**：eco 成型时所有外壳都是 `RenderShape.INVISIBLE` 且不再替
  邻居遮面，成员站在外壳位上会同时从内外两侧可见，任何角度都闪。现在与 eco 一致，成型即隐身，
  那一格的外观由 eco 自己的 section-geometry 钩子（`FixedBlockEntityRenderers`）画回来。

### 清理 / Housekeeping

- 删除中途做过的两件强化成员（CM1R 线程核心 / CT1R 并行核心）与 AE2 侧的
  `EnergizedComputationCoreType`：它们被 AE2 自家的 256K 档位与 16 并行硬上限完全压住。
  存档里已经 `/give` 出来的这两件会变成未知方块，启动日志报一次后消失。
- 美术分层源图（盈能核心的外壳 / 灯 / 成型灯三层）移到仓库根 `artsrc/`，只作交接源文件，
  不再随 jar 打包。

### 指南 / Guide

- 新增中英文「L1 供能接口」「盈能超导接口」两页，并从首页链过去。

## 1.2.4 (2026-09-20)

### 新增 / Added

- **玩家皮肤玩偶**：`fumo_reliqwq` 升级为通用玩偶，脸、衣服、袖子取自指定玩家的皮肤档案
  （复用原版头颅的 `ResolvableProfile` 解析，无自定义网络），坐姿几何由 `ModelPart` + BER 渲染，
  自动匹配经典 / slim 两种臂宽；放置朝向决定面朝方向，方块自带 15 级亮度。
- **`/prototypefumo <玩家名>`**：创造模式或 OP 直接取任意玩家的玩偶，对方不要求在线，
  查不到时退回占位皮肤不报错；服务端配置 `fumo_command_enabled`（默认开启）关掉后该指令拒绝一切使用。
- **三条装配室配方**：reliqwq 玩偶（3 台 L1 子系统主机）、Yang120 玩偶（3 台 L4 子系统主机）、
  kouooki 玩偶（粉色染料 + 干海带 + 棕色蘑菇）。
- **佩戴效果**：所有玩偶放进头盔栏给 30 秒夜视并自动续期；三个具名玩偶额外护甲
  （reliqwq 与 Yang120 为 +4 护甲值 / +2 韧性，kouooki 为 +1 护甲值 / +5 韧性），名字以本模组绿色显示。
- **皮肤覆盖口子**：资源包放 `textures/block/fumo/skins/<玩家名小写>.png` 即可替换某位玩家的玩偶皮肤，
  本模组不再内置任何别人的皮肤。

### 清理 / Housekeeping

- 删除一次性脚本 `bump_version.py`、`inspect_eco_housing.py`、`generate_small_bulk_housing.py`、
  `generate_script_matrix_housing.py`、`import_eco_cell_textures.py`（产物已入库，脚本已用完），
  以及过期审计文档 `docs/asset-index-audit.md`、`docs/quality-review.md`（1.2.0/1.2.1 的快照）。
- `参考模组/` 下的第三方 jar 取消 git 跟踪，`.gitignore` 补 `/参考模组/`、`__pycache__/`、`*.pyc`。
- 内置默认玩偶皮肤换成原创占位图 `block/fumo/placeholder_skin.png`，
  不再随 jar 分发别人的玩家皮肤（原 `mita_skin.png` 移除）。

### 指南 / Guide

- 新增中英文「玩偶」页：三件套主机与 kouooki 材料配方、`/prototypefumo` 取玩偶、
  佩戴加成与 `fumo_command_enabled` 开关说明。

## 1.2.3 (2026-09-19)

### 修复 / Fixed

- **化学品矩阵驱动器贴图缺失**：资产修剪误删了化学品矩阵驱动器模型引用的旧路径贴图
  （`cell_type` / `cell_housing`），盘芯片在驱动器里闪烁紫黑。已改指
  `storage_recolor/drive/` 下的现存贴图。

### 变更 / Changed

- **MegaCells 改回可选依赖**：1.2.2 的 jar 将其声明为必选。现小宗家族
  （物品/流体/化学品盘、扩展卡、三个外壳）仅在 MegaCells 在场时注册，
  全部配方带 `neoforge:conditions`；未安装 MegaCells 时小宗家族不出现，其余功能不受影响。

### 指南 / Guide

- 存储系统、计算系统、合成系统三页嵌入可旋转多方块场景（`ae2guide/scenes/*.nbt`，
  实机捕获的最小结构）。
- 新增 L1 计算系统与 L1 合成系统页：基础 1 条合成线程（eco L4 的 1/4），
  合成并行 16 / 超频 32（独立于计算侧）。
- 修复装配室页长段落渲染重叠；索引页注明三大系统耗电为 eco L4 同级的 1/8。
- 物品提示事件对小宗盘供应商判空（无 MegaCells 时不 NPE）。

## 1.2.2 (2026-09-19)

### 新增 / Added

- **L1 处理器装配器**：分子装配室的绿色变体（游戏内方块名为「L1 处理器装配室」），用处理样板
  合成处理器。内置逻辑/计算/工程三条配方，支持从 AE2 压印器自动推导（可配置、可按输出排除）；
  配套绿色样板供应器。KubeJS 可按字段删除/匹配装配器配方，新增走 `event.custom` 原始 JSON，
  详见 `docs/processor-assembler-recipes.md`。
- **元件工作台升级卡**：全部可分区存储矩阵登记 AE2 升级卡关联，模糊/反相卡可直接插入；
  卡片提示中的"可用于"清单按分组显示为一行「L1 存储元件」。
- **小宗流体/化学品矩阵**：新增小宗家族的流体与化学品盘（3 型，扩展卡升至 10 型），
  复用 eco 的 `mega_fluid`/`mega_chemical` 元件类型；外壳采用 eco 的 MEGA 外壳贴图
  （已拷入本模组命名空间）；标记通过元件工作台分区编辑；附组装/升级/拆解配方回路。
- **拆解配方**：补齐全能/量子矩阵拆解（外壳 ×2 + 组件 ×2 + 家族处理器）。

### 修复 / Fixed

- **无限盘进入元件工作台崩溃**：AE2 的 `isEditable` 默认实现对空配置库存直接解引用；
  三个固定无限源（自定义无限矩阵、无限混凝土矩阵、Beyond 适配器）显式声明不可编辑，
  工作台明确拒收而不是崩溃。
- **无限矩阵主机显示**：挂载的无限盘上报无限类型容量，主机指标按「无限」展示，
  替代无意义的「类型: 1 / 字节: 0」。
- **小宗扩展升级找零**：3 型 → 10 型升级配方原先退回无关的物品矩阵外壳，
  现按 AE2 惯例退回 1M MEGA 元件。
- **全能/量子拆解缺斤短两**：拆解产物的存储组件数量与组装对齐（×2）。
- **测试残留清理**：移除逻辑处理器的默认禁用（装配器现接受全部三条内置处理器配方）
  与 KubeJS 示例脚本的启动日志打印。

### 变更 / Changed

- **小宗物品盘启用压缩链后端**：改用 eco 的 MEGA 长桶存储后端，标记物品按存储链语义
  工作（链上任意形态折算入标记形态，压缩变体需安装 MEGA 压缩升级卡）。
  切换后旧引擎写入的存量不再被读取，更新前请先取回盘内内容。

### 依赖 / Dependencies

- 兼容基线改为 Neo ECO AE Extension **21.2.0-beta4**（小宗标记接口所在构建），
  `build.gradle`、`neoforge.mods.toml`、README 与 CHANGELOG 同步；区间收紧为
  `[21.2.0-beta4,21.2.0-preview)`。
- **MEGA Cells 保持可选依赖**：小宗家族（物品/流体/化学品盘、三个外壳与扩展卡）依赖其 mega 元件类型与
  长桶后端，未安装时不注册、其余功能不受影响；`neoforge.mods.toml` 与 README 同步。
- 上游复用贴图（mega 流体/化学品外壳、驱动器底壳）已拷入本模组命名空间，
  模型不再跨命名空间引用上游资产。

## 1.2.1 (2026-09-18)

### 修复 / Fixed

- **依赖区间失守导致 NoSuchMethodError**：有玩家反馈
  `NoSuchMethodError: cn.dancingsnow.neoecoae.gui.storage.StorageHostUI$Config.<init>(...)`。
  根因是 Maven 把未知限定符 `preview` 排在 `beta` 之后，原先的 `[21.2.0-beta3,)` 会放行
  `21.2.0-preview10` 直到 `21.2.0-preview16-hotfix1`，而这些构建的 `StorageHostUI.Config`
  仍是 11 参数旧签名（只有 beta3 / beta4 是 13 参数）。区间改为
  `[21.2.0-beta3,21.2.0-preview)`：preview 整条线被拒绝，不兼容版本会在加载阶段明确报错，
  而不是进游戏后崩溃。
- **KubeJS 可选加载**：客户端此前无条件读取 `InfiniteMatrixBuilder.DEFAULT_DRIVE_MODEL`，
  未安装 KubeJS 时会触发该 builder 类加载。现改用普通 `ResourceLocation`，并只在
  `ModList.isLoaded("kubejs")` 为真时扫描脚本矩阵，与「KubeJS 可选」的承诺一致。
- 移除两个配方文件的 UTF-8 BOM（`simplify_universal_storage_cell_1k` / `_1m`）。
- 删除从未注册进 `KJSPlugin` 的 `ChemicalMatrixBuilder`；其能力已由统一的
  `StorageMatrixBuilder.type('chemical')` 覆盖，保留两套 API 只会误导脚本作者。

### 依赖 / Dependencies

- 兼容基线改为 Neo ECO AE Extension **21.2.0-beta3**（本地最新），
  `build.gradle`、`neoforge.mods.toml`、README 与 CHANGELOG 同步。

## 1.2.0 (2026-09-17)

### 新增 / Added

- **KubeJS 自定义容量与类型数**：新增 `.bytes(256)` 与 `.totalTypes(400)`，可不走固定档位
  直接声明存储字节数与类型上限；两者会给定义打上 `MatrixSize.CUSTOM`，不再受预设档位约束。
- **KubeJS 指定矩阵材质**：`.type(...)` 现在同时接受存储类型（`item` / `fluid` / `chemical`）
  与家族名（`pigcat` / `universal` / `quantum` / `small_bulk` / `other`），也可用
  `.material(...)` 单独指定外观。给了类型就会自动匹配对应外壳与驱动器内模型，无需手写贴图。
- **KubeJS 全能与量子后端**：`.type('universal')` 走 eco OmniCells 后端（默认 256 类型，
  可用 `.totalTypes(...)` 覆盖）；`.type('quantum')` 走 OmniCells 且**无限类型**。
- **小宗标记面板**：L1 存储主机新增标记面板，可切换小宗矩阵、编辑 3 或 10 个标记槽；
  配套服务端 RPC 校验槽位、距离、维度与压缩链。
- **自动标记**：新增配置 `mega_bulk_auto_mark_threshold`（默认 `20000`），按存储量与压缩链
  写入空标记槽，自动去重；提示中明确说明实际压缩仍需安装 MEGA 压缩升级卡。
- **三合一状态提示**：三合一主机与存储/计算/合成三大模块的物品提示新增红字「暂未实现」/
  "Not implemented yet"（语言键 `item.neoecoprototype.not_implemented`）。

### 材质 / Materials

- 新增 `MatrixMaterials` 作为**唯一材质映射表**，九个家族（物品/流体/化学品/无限/其他/小宗/
  猪咪/全能/量子）的物品栏模型与驱动器内模型都在此声明，脚本不再各自乱放贴图。
- **小宗（MEGA 家族）**：基础（3 类型）与扩展（10 类型）两张外壳都改为从 eco 的 MEGA 外壳
  派生——基础提升为深灰、扩展保持原件；此前驱动器内用的是通用物品外壳压暗版，已修正。
- **全能 / 量子**：物品栏外壳改用 eco 的 `omni_cell_housing` / `quantum_omni_cell_housing`，
  量子按 eco 顺序叠四层（外壳、容量灯、状态灯、量子叠层）。
- **贴图本地化**：上述 eco 兼容贴图复制进本项目（`eco_omni_cell_housing`、
  `eco_quantum_omni_cell_housing`、`eco_quantum_omni_cell_layer`），模型不再引用
  `neoecoae:` 资源路径，运行时不再依赖上游资源布局。
- **其他**材质沿用原量子外壳作为统一后备外观，驱动器内为棕色
  （`brown_cell_housing`，由 `tools/generate_script_matrix_housing.py` 生成）。

### 修复 / Fixed

- **全能矩阵在存储主机显示原始翻译键**：全能 `ECOCellType` 此前从未注册，导致 L1 主机翻译
  取不到、显示 `eco_cell_type.neoecoprototype.universal`。现已注册进 `neoecoae:cell_type`，
  并补齐中英文名称。
- **L9 存储主机不认识本模组矩阵**：eco 的主机通过 `NERegistries.CELL_TYPE` 枚举类型行并按
  注册 ID 归集数据，未注册的类型会被静默跳过。注册后全能（注册 ID 0）与量子（ECO 自带
  `quantum_omni`）在 L1 与 L9 主机都能正常统计，且两者互不混淆。
- **KubeJS 矩阵紫黑缺失贴图**：脚本创建的矩阵此前由 KubeJS 默认流程追加
  `layer0=kubejs:item/<id>`（不存在的图集贴图）。现在 builder 会短路模型生成，按类型指向
  真实模型。
- **量子叠层动画丢失**：`quantum_omni_cell_layer` 是动画贴图，此前只复制了 PNG、漏掉
  `.png.mcmeta` 导致动画变静态，现已补入；并全项目审计确认没有其他漏配。
- **语言文件不对称与硬编码文本**：补齐 `simplify_chemical_storage_cell_4k` 等缺失中文键，
  中英各 100 条且无单向键；标记结果提示由硬编码中文改为语言键
  `gui.neoecoprototype.storage.bulk_mark.result`。

### 变更 / Changed

- KubeJS 矩阵的默认材质改为**按类型自动匹配**，不再统一套用同一种外观；显式设置
  `inventoryModel` / `cellModel` / `parentModel` / `textures` 时仍以脚本为准。
- 材质推断规则修正为「只有物品 + 无限才使用无限银灰配色」，流体/化学品即使无限也保留各自
  家族材质。
- 排查用的诊断日志已删除，其余降为 `debug` 级别。

### 兼容性 / Compatibility

- **KubeJS 无限矩阵语法保持不变**：`event.create('id', 'neoecoprototype:infinite_storage_matrix')`
  与 `.itemType(...)` / `.fluidType(...)` / `.cellModel(...)` / `.displayName(...)` 均无改动，
  仅新增可选的 `.material(...)`；默认物品栏与驱动器模型与旧版一致。
- 小宗矩阵继续继承 eco 的 `ECOStorageCellItem`，使用 `neoecoae:mega_item` 类型，未转换为
  全能/量子家族。
- 未安装 AE2 Omni Cells 时，`.type('universal')` / `.type('quantum')` 会给出明确报错，
  而不是创建出损坏矩阵。

### 工具 / Tooling

- `tools/audit_assets.py`：模型贴图缺失、跨命名空间引用、孤立贴图与孤立 `.mcmeta` 审计。
- `tools/audit_item_models.py`：已注册物品是否都有物品模型（防紫黑格）。
- `tools/generate_small_bulk_housing.py`：从小宗 MEGA 原件派生基础/扩展两张外壳。
- `tools/import_eco_cell_textures.py`：导入 eco 兼容贴图并同步 `.png.mcmeta`。
- `tools/generate_script_matrix_housing.py`：生成「其他」材质的棕色驱动器外壳。

## 1.1.1 (2026-09-15)

### Release visibility

- Trinity is still experimental. Its controller, module items, multiblock preview, and recipe catalyst are hidden from player-facing JEI in this release; the registered content remains available for development and compatibility testing.

### Dependencies

- Follow Neo ECO AE Extension **21.2.0-beta3**.
- This release targets the beta3 storage, UI, and compatibility baseline.
- FastPath task submission is not enabled in this release; Trinity remains hidden while its design is being researched.

## 1.1.0 (2026-09-13)

### 新增 / Added

- **KubeJS 无限存储矩阵**：整合包作者可在 `startup_scripts` 中用
  `event.create('id', 'neoecoprototype:infinite_storage_matrix')` 注册自定义无限矩阵，
  支持 `.itemType(...)` / `.fluidType(...)`（延迟解析，目标内容不存在时报错可读）、
  `.cellModel('...')`（驱动器内格子渲染模型，需格子芯片规格）与 `.displayName(...)`。
  KubeJS 为可选依赖，不安装时模组行为不变。
  详见 `docs/kubejs.md`；安装 ProbeJS 可获得脚本自动补全。
- 绑定流体的自定义矩阵现在会显示在存储主机 GUI 的流体行。

### 美化 / Changed

- 猪咪存储矩阵外壳（彩蛋物品）：+2 实体交互距离、6.6 攻击伤害、支持横扫、自带附魔光泽。
- 存储主机、合成系统、计算系统主机与绿晶晶格、闪存晶阵使用统一的主题绿物品名。
- 特质机壳（铝合金机壳）重制贴图：银色铝框 + 淡绿内板。
- 修正冷却控制器成型面遗漏的蓝转绿染色。

### 依赖 / Dependencies

- 上游同步至 Neo ECO AE Extension **21.2.0-beta1**（正式发布渠道），
  依赖区间调整为 `[21.2.0-beta1,)`。
- 不再需要 Advanced AE / GeckoLib 运行时兜底（上游已用 `requiredMods` 条件加载兼容 mixin）。

### 兼容性 / Compatibility

- Minecraft 1.21.1 / NeoForge 21.1.233 / AE2 19.2.17。
- 已验证客户端与专用服务端加载；14 个 mixin 注入点已对照 21.2.0-beta1 逐一复核。

### 已知上游问题 / Known upstream issues

- 上游 21.2.0-beta1 反复切换超频可能导致合成派发停滞（作者已在 tag 后提交派发一致性修复，
  预计随下一个 beta 发布）；实测拆卸并重新成型多方块可解除卡死。
