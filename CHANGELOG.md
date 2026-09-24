# Changelog

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
