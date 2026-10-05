# Changelog

## 3.0.0-beta1 (2026-10-05)

本版重点：把 eco 的多方块、存储与合成真正下放到 L1，并新增天外寒冰 / Frigit 一族与末地浮空彗星。

### ⚠️ 升级前必读

- **需要 Neo ECO AE Extension 21.2.1-beta2 或更新。** 装上旧版会直接启动失败，不是可选项。
- **`fumo_enabled` 配置项已删除。** 玩偶的护甲与效果、苦力怕躲避、`/prototypefumo` 现在默认生效；旧配置文件里那一行会被静默忽略。如果你之前靠它关掉玩偶，升级后这些行为会恢复——不想恢复就把玩偶从世界里收走。
- **`pigcat_storage_cell` 与 `pigcat_storage_matrix_housing` 改名为 `pigmee_*`，故意没有挂注册表别名。** 旧存档里按旧 id 存的那两格**会变成空气**。更新前先把里面的东西倒出来。
- **天外寒冰 / Frigit 一族现在整族默认不进世界。** 矿脉由 `cryotheum_ore_dimensions`（出厂是空表）管，彗星由新键 `cryotheum_meteorite.enabled` 管，**默认 `false`**。想打开矿脉就列维度：`minecraft:the_nether`（整个下界）或 `minecraft:the_end`（**只落在 `end_highlands` 这一个群系**）；主世界那颗矿没有自己的生成文件，只能从彗星里来，所以要见到它得把彗星打开。两条都在生成时生效：**已经生成的区块不受影响**，改完要飞新区块才看得见。
- **批量样板的天花板改成"九格摆得下"，摆不下的会被拒收。** 1.2.12 那种"往一格塞 64 个一组只能装 8 个的细胞"的做法能塞下，但塞进去的是原版存档不认的超组数量。升级后同一张样板（例如 64 倍的 CE1R→4M 增强）会被装配室拒收，原料留在网络里不动；处理器这类原料一组 64 个的样板不受影响，上限仍是 192 倍。

### 新增

- 四只具名玩偶各自成为一个方块、一件物品：`fumo_doll_reliqwq` / `_yang120` / `_kouooki` / `_tedxenon`，放下时按玩家朝向转四向。
- 每只玩偶的护甲与效果分别在注册处给定：reliqwq 与 yang120 是 4 护甲 / 2 韧性，kouooki 是 1 护甲 / 5 韧性，tedxenon 是 6 护甲 / 1 韧性并额外给生命恢复（30 秒，戴着自动续期）。所有玩偶共有的夜视不显示图标，tedxenon 的恢复图标刻意显示。
- 天外寒冰现在可以采到：三种矿（主世界 / 下界 / 末地）与 Frigit 晶体一族，共 12 个方块、1 件物品。母岩有四档，破坏时按战利品表逐级降级。
- 末地新增我们自己的浮空彗星结构：一颗带尾巴的彗星，头上是母岩与芽，尾巴是冰与石英。
- 末地浮空彗星现在也能关了：新配置键 `cryotheum_meteorite.enabled`，默认 `false`。以前它不受任何开关管——矿脉关掉之后，它是一族里唯一还会自己进世界的东西。
- 上一版的美术做成一个可以直接开关的资源包，随 jar 一起发，不需要另外下载。升级后它默认是关着的。
- JEI 的转移按钮现在能把装配室的配方编码成样板。
- 驱动器上会显示当前挂载的是哪只矩阵（图标 + 数量 × 名字）。
- 集成工作站多了一张压雪的配方：1 个雪块出 4 个雪球。
- L1 处理器装配室也能压雪：4 个雪块出 4 个雪球（装配室的配方至少要三个原料，所以按四份写）。

### 变更

- 苦力怕躲玩偶改成**按玩偶扫**：放下去的方块和戴在头上的玩家各自每 5 tick 扫周围 6 格。**拿在手里不再算**——上一版算。
- 小宗存储矩阵的档位口径统一为 `1k / 16k / 64k / 1M / 4M`（物品、流体、化学品三介质一致）；化学品那档 `4k` 整档撤掉，它自 1.2.0 起就只能靠 `/give` 拿到。
- 盈能水晶一族与那颗生存里拿不到的矿不再出现在创造栏里，物品本身仍然注册着。
- 配置项 `green_pattern_provider_slots` 删除：L1 样板供应器的样板槽固定 27 个，因为贴图就画了三行九格。旧配置文件里残留的那一行不再有作用。
- 四张配方的原料换掉了。绿铝机壳的四个角由 `ae2:quartz_vibrant_glass` 换成 `minecraft:iron_ingot`；超导晶振的中心由 `ae2:quartz_glass` 换成 `minecraft:copper_block`；冷却控制器的四个角由 `ae2:quartz_glass` 换成 `minecraft:powder_snow_bucket`。这三张的图案与产量都没动。晶阵驱动器换的是下面中间那一格——原来上下两格都是计算处理器，现在下面换成 `minecraft:copper_block`，图案变成 `ADA / BCB / AEA`，产量仍是 2 台。
- 冷却控制器用的细雪桶**不会返还空桶**，这是原版本来的行为：`powder_snow_bucket` 注册时没有 `craftRemainder`，水、岩浆、牛奶那三只桶才有。
- 计算主机里那截线缆现在**按宿主分**：我们自己的 1 级机壳里 CE1 与 CE1R 都是普通护套（深色、不发光），插进 eco 的更高等级主机（c4 及以上）时两颗都换成会发光的那对。
- 模组列表里的图标换成了新的标题图（966×294）。

### 修复

- L1 样板供应器、L1 供能接口、盈能超导接口三张面板被整块压扁的问题修掉了：AE2 按 256 像素的参考高度缩放 GUI 贴图，真实高度不是 256 的面板会被拉伸并在底部回绕。现在样式里声明了各自的高度，槽位坐标一个都没动。
- 上面那次压缩结掉了另一笔账：两张接口的贴图比目标槽行低 2–3 像素，曾经被记成"等美术重画贴图"。贴图与绑定都没错，不需要重画，那条已知项撤销。
- L1 存储子系统通讯接口不再显示上游的默认标题。现在存储通讯接口、C1 计算通讯接口、F1 合成通讯接口、F1 智能样板总线四个界面各自报自己的名字。
- 创造栏里的四只具名玩偶不再共用同一张默认皮肤（看起来像"都变成了 Alex"）。现在按名字给不同的默认皮肤；**真人皮肤仍然只有 `/prototypefumo <玩家名>` 拿得到**。
- 挖掉放下去的玩偶，掉出来的仍是带主人的那一只：以前没有绿色名字、没有护甲、也没有额外效果。
- 具名玩偶的 tooltip 会说清它的第二样效果，不再只写夜视。
- 驱动器的信息整块不显示的情况修好了：空驱动器返回的是 `null` 而不是空物品，一处判空把整块数据写没了。
- L1 样板供应器补回一个被漏掉的标签段（覆盖槽位段不会连带覆盖它）。
- 装配室被推入摆不进九格的批量样板时会抛异常，而那个异常穿出 AE2 的网格 tick，**整个存档掉线**。这种样板现在直接被拒收：原料退回网络，日志写明是哪一条规则卡的（例如处理器样板 192 倍是天花板，再高就摆不下）。同一条抛出在已发出的 1.2.12 里也在，玩家可触发。
- 批量样板一轮交不出的那部分产出现在**直接进网络**，不再连同已经收走的原料一起消失。一轮往机器前面放的仍然只是一组，其余走网络。
- 写了两行的物品说明现在真的分成了两行。原版不会拆开一个 Component 里的 `\n`，它把那三个字符画成方框；说明文本仍留在语言文件里，只是每行各变成一个 Component 交给 tooltip。

### 移除

- 小宗仓的手动过滤器面板整块删掉（主机 GUI 里那排幽灵槽和左右换驱动器的两个箭头）。**标记能力一点没少**：AE2 自己的盘配置界面照样能标，主机上那个"一键标记"也照样批量填，两者写的是同一份配置。旧存档里相关的两个字段会被直接忽略，不报错、不炸档。
- 天外寒冰的深板岩档删除。
- 三张"直接做出线缆部件形态"的配方删掉了：供能接口和样板供应器在合成台上的 shaped 配方，加上超导接口在集成工作站里的那一张。这三台机器的线缆部件形态现在只有一个来源——把方块形态放进合成栏转。两种形态之间的无损互转三条都还在，两个方向都能转。

### 已知问题

- 天外寒冰 / Frigit 一族整族默认不进世界：矿脉靠 `cryotheum_ore_dimensions` 的空默认，彗星靠 `cryotheum_meteorite.enabled` 的 `false` 默认。原因是这一族通向的水晶还没有可用的下游用途（配方故意没写），而母岩、芽与簇只有彗星这一个来源——两头都关住，新世界的末地才不会挂着一串用不上的半成品。已经在世界里的方块照常渲染、照常能采；这两条都在生成期读取，改完要飞新区块，老区块不会被补上，也不会被抹掉。
- Trinity 多方块（三合一联合体）仍属实验内容：它不进 JEI，物品上带一行"未实现"提示。
- `tedxenon` 至今没有配方，所以指南里具名玩偶是四只、能合成的是三只。
- `/prototypefumo` 手打能执行，但不出现在 Tab 补全里（旧现象，未改）。

### 依赖

- Neo ECO AE Extension `21.2.1-beta2` 或更高；Applied Energistics 2 `19.2.17` 或更高；Java 21。
- MegaCells `4.11.0+` 是小宗存储矩阵整族的门槛；化学品单元与化学品矩阵另需 Mekanism 与 Applied Mekanistics。
- 这一版标 beta 而不是正式版：上游最新的公开发布仍是 21.2.0，`21.2.1` 还没有公开下载文件。
- 两条版本下限为什么正好是这两个数字，详见 `docs/ae2-extension-playbook.md`。

## 1.2.12 (2026-10-01)

从 `v1.2.11` 切 `hotfix/1.2.12` 单独发的紧急修复，只带两条，不跟着 3.0.0-beta1 的依赖地板走。

- **十个方块没有战利品表，拆下来什么都不掉。** 生存挖掘和扳手右键都会让机器直接消失而不下落任何东西。现在每个方块都有自己的一张表。
- **装配室拒收"重复同一种原料"的样板，批量样板也一起失效。** 现在按样板自己的输出量归一化，两种形状都能正常合成。

## 1.2.11 (2026-09-29) - 主机不再带着成型状态落地，镜像主机的动画转向改正

### 修复 / Fixed

- **手放计算主机不再闪一下"已成型"的外壳**：主机的默认状态带着四个布尔旗标落地，成型面显示一帧才被集群纠正回去。现在落地就是全 false。
- 加了一条常驻守卫：我们注册的任何一个方块，默认状态里不许有布尔量为 `true`。
- **镜像的 C1 主机动画转向改正**：镜像模型把两张动画贴图按反方向采样，一张转动的图被水平翻转就成了反转转向，所以镜像机转得和正装机不一样。现在几何与静态底图继续镜像，8 个动画面改用正装机那套 uv，两台就同向了。
- **这一条是故意偏离 eco**：eco 自己的 C4 镜像机同样会反，是他拍板"镜像机也该正转"。散热控制器的 mirrored 模型有同一处翻转，但那是另画的一套几何（面板位置与 north/south 都换过），要逐面重新配对，这次没动。

### 已知 / Known

- **成型后机壳仍然全部不画（计算家族）**：这条是承重的，试过改成"只藏够得着的"之后远端机壳画成一整块普通立方、和散热控制器成型模型外伸的面板共面闪，所以改动已撤回。要真修，得先给远端机壳一个成型后可看的模型（美术活），或者让散热控制器的成型模型不伸进那一格。

## 1.2.10 (2026-09-29) - 计算主机回到 eco 的方块实体，CPU 面板恢复

### 修复 / Fixed

- **成型并且接了网的 C1 计算主机重新出现在 AE2 的 CPU 列表里**：1.2.8 为了在主机自己的方块状态上发布成型外观，把它的方块实体换成了 eco 那个类的子类，结果它从 AE2 的机器表里消失了。现在方块实体退回 eco 的类，那两件事改由 `publishShape()` 加方块自己的定时任务接手。
- 附带回来的：eco 在节点入网时按同一个类读的「快速规划 / 循环规划」开关，现在在我们的主机上也生效了（tooltip 里能看到那两行）。
- **老世界里被点亮成「有交换器」的 C1 会自愈**：我们的计算器整条替换了 eco 的 `verifyInternalStructure`，而 eco 只在那里面清 `network_switch` / `high_energy_network_switch`，所以 1.2.8 之前留下的 true 会一直留着，把 CPU 送进一条不存在的逻辑网络。现在每次集群重算都会投递一次纠正，方块在下一个 tick 把 true 写成 false（已经是 false 时一个字都不写）。守卫：把两个布尔人为设成 true 打在成型主机上，要求 120 tick 内被清掉。
- **五个方块的方块状态不再空转四个朝向**：`energized_computation_core`（整方块、六面同图）、`fumo_reliqwq`（模型里没有元素，画面由渲染器给）、以及三件 Trinity 模块（`cube_all`）——它们的模型四个朝向看起来完全一样，却各写了 4 到 8 条带旋转的条目，合计 32 条永远不可能有差异的键。现在每个文件一条通配键。Five

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

- **C1 计算子系统与 F1 合成子系统可以被"多塞一颗核心"**：几何有四条外壳走位，但**没有一条经过主机自己所在的那一列**，所以主机正上方与正下方两格从来没被检查过；而集群是按边界框收编成员的，只要把线程核心（或并行核心）摆在那两格，机器照样成型、成员照样算数 —— 等于白送线程数与吞吐，而这两格是一键搭建永远不会产生的位置。现在这两格必须是外壳方块，与 L1 存储主机早就有的规则一致，三台机器行为统一。盈能强化计算机核心此前那条"只挡这一种方块"的特例被这条通用规则包含，不再单独存在。
- **代价（玩家可见）**：老世界里如果有人把 C1 / F1 主机正上或正下方放过别的东西，升级后那台机器会**散架** —— 不是丢方块，是成型判定不再通过，把那一格换成对应的外壳方块即可重新成型。
- **四条新守卫测试**：C1 上下各一条（放线程核心）、F1 上下各一条（放并行核心），都要求机器拒绝成型；测试会先确认那一格在搭建方案里确实是外壳，所以"换掉它"是唯一变量。

### 需要 / Requires

- NeoForge `21.1.x` on Minecraft `1.21.1`，Applied Energistics 2 `19.2.17` 或更新，**Neo ECO AE Extension `21.2.0` 或更新**。

## 1.2.9.1 (2026-09-28) - Startup Crash In Large Packs

### 修复 / Fixed

- **修好 1.2.9 在大整合包里进不去游戏的启动崩溃**：三个 AE2 部件模型的注册从 client setup 挪到 common setup，不再依赖"抢在模型集合被冻结之前跑完"这件事。

### 说明 / Notes

- **正在用 1.2.9 的玩家请直接升这一版。** 那条崩溃只在包里有会提前碰模型的模组（`quick-pack`、RenderJS、KubeJS 这类）时才出现，所以开发环境测不出来。
- 依赖没有变化：NeoForge `21.1.x` / Minecraft `1.21.1`、Applied Energistics 2 `19.2.17` 或更新、Neo ECO AE Extension `21.2.0` 或更新。
- 这一版没有内容改动，1.2.9 的其余说明（接口语义互换、L1 新美术、内置旧美术回退包）仍然适用。

## 1.2.9 (2026-09-28) - Interface Roles Aligned With Their Names, L1 Artwork Replaced

### 变更 / Changed

- **「通讯接口」现在就是能打开界面的那块，三台机器一起对齐**：显示名、右键行为、一键搭建默认放的方块、配方链与主机发布的 `communication_interface` 全部同时改过来。
- `simplify_<machine>_interface`（「…接口」）是有序合成的基础件，右键无反应；`simplify_<machine>_network_interface`（「…通讯接口」）= 接口 + `ae2:terminal`，右键打开 eco 的界面。
- **注册 ID 与存档数据一个字未改**，老世界的方块不会丢。
- **玩家可见的代价**：库存里这两件物品的含义互换了——原来那块能开界面的「接口」，升级后是开不了界面的那一件，要去合成一份通讯接口。
- 收益是属性名终于和显示名一致：美术为「放了通讯接口」画的那张主机成型面（`controller_l4_formed_network`）绑到了它本该绑的状态上。
- **成型后的成员方块一律不画自己，外观由主机的方块状态决定**：C1 那个「接口成型后还自绘」的历史遗留钩子（`hideWhenFormed()` 覆写 + `computation_interface_formed` 模型 + `formed` 两条键）删掉了，三台六块接口现在行为一致。
- **旧世界里残留的主机方块副本不会隐形**：`variants` 的键是子集匹配，没写的属性走通配，所以 1.2.7 那份 16 条键照样覆盖当前的 32 个状态——只是表达不出新的区分。（1.2.8 的发版说明说反了，那句现在作废。）

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

- **这一版起需要 Neo ECO AE Extension 21.2.0 或更新，且不再兼容 21.2.0-beta4 ~ beta6。** eco 在 21.2.0 之前把 `StorageHostActionUI$Config` 的最后一个参数从 `Runnable` 换成了 `Consumer<Boolean>`（实测 `beta6-hotfix2`、`beta7-hotfix1` 与正式版都已是新签名，裸 `beta4`~`beta6` 仍是旧签名），所以 1.2.7 配 21.2.0 打开 L1 存储主机会 `NoSuchMethodError` 崩服，1.2.8 反过来配裸 beta6 也会以同样方式崩。**停在 beta4 ~ beta6 的玩家请继续用 1.2.7。** 版本区间不是保险索：已有玩家用的启动器会跳过依赖检查，直接把不兼容的组合加载起来。

### 新增 / Added

- **成型外观改由方块状态选择 / formed appearance picked by block state**：三台主机各自发布"接口格里放的是不是通讯接口"（`communication_interface`），C1 计算主机另外发布 `energized_threading_core` 与 `energized_parallel_core`；成型模型的挑选全部落在 blockstate 文件里，资源包不需要读世界。新增 18 个变体模型作为分图钩子，目前全部指向现有成型贴图，所以任何组合下看到的都还是现在的正式外观。
- **六门新守卫 / six new guards**：装配室往供应器推产物的能力面、主机正上下格的盈能核心、三台主机的状态发布，以及 `variants` 穷举与 multipart "每状态恰好命中一条"的文件覆盖检查。
- **装配室批量样板说明 / guide section**：GuideMe 中英双语补充"整批进料一次合成、产物随份数翻倍、上限一叠 64、耗时不变"。

### 修复 / Fixed

- **装配室产物卡在供应器**：给 L1 样板供应器的方块实体与线缆部件注册 `AECapabilities.GENERIC_INTERNAL_INV`，交给 AE2 自己的最低优先级钩子包成 `ItemHandler`。此前只有 AE2 原版供应器回答该能力，装配室完工时推不出产物。
- **F1 合成子系统那对接口与 eco 的命名对齐**：`simplify_crafting_interface` 现在是"F1 合成子系统通讯接口"并且能打开接口界面，`simplify_crafting_network_interface` 变成"F1 合成子系统接口"（只挂网络端点、右键无反应）。**注册名与存档数据一个字未改**，只换行为与显示名，老世界的方块不会丢失，所以这两块的名字与界面归属会互换。
- **盈能强化计算机核心成型后闪烁**：删掉它的方块实体渲染器，成型外观改由 blockstate 换整块模型，与其余外壳位成员的 `RenderShape.INVISIBLE` 规矩一致，不再出现内外两侧同时可见。
- **第二颗盈能核心被悄悄收编**：计算集群几何补上有界守卫，主机正上方/正下方或采纳盒内多出一颗盈能核心时不再成型。
- **L1 样板供应器与两个接口的界面错位**：补回贴图里被贴错位置造成的整行透明空洞、把 ↓ 箭头只留在配置行、并修掉扩成两行后配置组与存储组抢同一行导致"存储行放不了东西"的问题。

### 已知 / Known

- **三个接口界面的标题未本地化**：eco 21.2.0-beta6 改了生成 lambda 的序号，我们钉 `lambda$create$1` 的 `@Redirect` 没命中（`require = 0`，所以不崩），标题仍显示 eco 默认文案。

### 资源包与覆写 / For resource pack authors

- **三份主机 blockstate 结构变了，条目数必须跟着变**：`simplify_computation_system` 现在是 8 个属性穷举出 512 条，`simplify_storage_controller` 4 个属性 32 条，`simplify_crafting_system` 是 20 条 multipart 且每个 `formed` 条件都钉住 `communication_interface`（不钉住就会有一个状态同时命中两条、同一格画两遍模型）。`simplify_computation_interface` 拆成 `formed=false` / `formed=true` 两态，好让成型的通讯接口带上自己的成型外观；另外两份只是改指向自己的模型文件，不再与隔壁共用。
- **老世界不受影响**：方块状态是按"属性名=值"存进区块 palette 的，新增属性回读即默认 `false`，blockstate 文件本身不进存档；升上来看到的还是升级前那台机器。
- **但覆写会静默失效**：谁之前替换过这三份主机文件，他的副本没有新键，那些没被命名的状态**匹配不到任何模型 = 方块直接隐形**（不会回退到我们的文件）。请拿新版文件重做覆盖，不要在旧副本上打补丁。
- **成型外观的挂点**：`textures/block/<机器>_recolor/controller_formed/controller_formed_a<后缀>.png`，后缀由 `c`（接口格里是通讯接口）、`t`（盈能线程核心）、`p`（盈能并行核心）拼接，`_c_t_p` 是三者全有、无后缀是三者全无。18 个变体模型已经在 `models/block/<机器>_controller/` 下建好并且目前全部指向 `controller_formed_a`，所以给某个组合换外观只改一处贴图引用。

## 1.2.7 (2026-09-24) - L1 Computation and Interface Update

### 新增 / Added

- **L1 计算系统扩展 / L1 computation system expansion**：新增盈能强化计算机核心、盈能强化线程核心和盈能 4M 计算盘；支持 L1 计算集群的专用成员槽位、并行能力和线程能力。
- **L1 计算系统配方 / L1 computation recipes**：新增处理器装配室和 eco 集成工作站配方，使用超导处理器、盈能超导锭与极寒凌冰等材料。
- **L1 供电 ME 接口 / L1 powered ME interface**：新增方块版与线缆 Part 版，保持 18 个 CONFIG / 18 个 STORAGE 槽位，提供 200 AE/t 被动供电。
- **盈能超导接口 / energized superconductive interface**：新增方块版与线缆 Part 版，每个标记槽最多配置 8192 个物品、512,000 mB 流体或化学品。
- **L1 接口配方 / L1 interface recipes**：L1 供电接口改由 L1 处理器装配室制作；盈能超导接口改由 eco 集成工作站制作并消耗 10,000 FE。
- **双语发布文档 / bilingual release documentation**：补充计算系统、接口、配方和兼容性说明。
- **L1 数值配置项 / extra config entry**：`l1_computation.energized_cell_total_bytes`，默认 `5242880`（原 4 MiB 加 30%），单独控制盈能闪存增强（CE1R）的合成存储字节数。
- **盈能盘物品栏贴图 / item-icon-only difference**：CE1R 不再与 CE1 共用同一张物品模型，改为一张只把绿色信号换成我们蓝色的独立贴图；插进驱动器之后两者外观仍然一致。
- **外壳位成员的成型外观 / shell member rendering**：盈能强化计算机核心成型后不再画方块模型（与 eco 所有外壳位成员一致：成型外壳是 `RenderShape.INVISIBLE` 且不再替邻居遮面，成员继续画就会从内外两侧同时可见而闪烁），那一格的表面改由 eco 的 section-geometry 钩子画回。

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
