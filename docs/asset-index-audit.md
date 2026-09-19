# 资源索引审计报告

- 审计时间：2026-09-18（1.2.1 beta3 资产清理后）
- 审计对象：`src/main/resources/assets/neoecoprototype`（贴图 / 模型 / blockstates）+ `src/main/java` 中的资源引用
- 审计方式：只读扫描 + 可达性分析 + beta3 客户端启动回归
- 本轮已删除：42 个确认废弃的 `l6/l9` 模型、58 张确认废弃的 `_b/_c` 贴图；删除清单见 `build/asset-removal-l6-l9-bc.txt`
- 结论前提（已确认）：`_a/_b/_c` 是 eco 原版 4 / 6 / 9 三个等级的区分，本项目只做 4 级（`_a`），
  不打算做 L2 及其他等级

---

## 1. 结论摘要

本轮已完成第一批高置信度清理。此前确认的高等级复制残留已删除，剩余资产不再默认视为可删：

| 类别 | 清理前 | 本轮动作 / 当前状态 |
|---|---:|---|
| `*_l6_*` / `*_l9_*` 模型 | 42 | 已删除；当前剩余 0 |
| `*_b` / `*_c` 贴图 | 58 | 已删除；当前剩余 0 |
| `textures/block/storage/` 原图副本 | 44 | 未删除；需先确认是否为生成器输入 |
| 字节完全重复贴图 | 19 组旧统计 | 当前重新统计为 11 组、22 个文件，待审 |

清理后资源总量为 186 张贴图、160 个模型、31 个 blockstates。资源审计报告为模型引用的贴图缺失 0，
但仍有 56 个“无模型直接引用”的贴图候选；其中可能包含动态 Java/KubeJS/GUI 引用，不可直接批量删除。

beta3 客户端回归结果：无 `Missing texture`、`Unable to load model` 或语言加载错误；KubeJS startup/client/server
脚本分别为 2/2、1/1、2/2 成功加载。

**关键判断：Java 侧的静态资源入口相对集中，但动态路径仍需白名单化**——当前治理重点是建立可信索引，
不是继续根据文件名批量删除。

---

## 2. 审计方法

从入口出发做**可达性分析**（而不是简单地扫 model JSON 的 `textures` 字段）：

- 入口 = `blockstates/*.json` 的 model 引用 + `models/item/*.json` 全部 + Java 里登记的模型
  （字符串字面量、`MatrixMaterials` 常量、`block/cell`、`block/computation_cell`、`block/computation_cable` 三个目录）；
- 沿 `parent` 链递归，收集所有 `textures` 字段引用；
- 不可达的贴图 / 模型即为死资产候选。

### 已知盲区（报告中标注"需人工确认"的部分）

1. **Java 动态拼路径**：如 `ResourceLocation.fromNamespaceAndPath("neoecoprototype", "block/cell/storage_cell_l1_" + kind)`，
   静态扫描看不到，可能把在用资产误判为死；
2. **GUI 直接加载贴图**：如 `gui/tier/l1.png` 这类按等级拼名的资源；
3. **渲染器硬编码**：BlockEntity 渲染器里直接指定的模型；
4. **KubeJS 脚本引用**：`run/kubejs/**` 不在扫描范围内。

因此：**删除前必须跑一次游戏验证**（见第 5 节）。

---

## 3. 现状数据

| 指标 | 数值 |
|---|---|
| 贴图总数 | 186（当前 11 组、22 个字节重复文件） |
| 其中 `textures/**/recolor/` 改色产物 | 143 |
| 模型总数 | 160（清理后） |
| blockstates | 31 |
| Java 硬编码资源引用 | 需结合动态白名单审计 |
| **模型引用缺失贴图** | **0** |
| **无模型直接引用的贴图候选** | **56** |
| **孤立 `.mcmeta`（无同名 PNG）** | **0** |

命名现状：
- 物品栏：`simplify_item_storage_cell_1k`（种类在前、容量在后）
- 驱动器内：`storage_cell_l1_item`（等级在前、种类在后）
- 两者字段顺序相反，是"索引难记"的直接来源。

---

## 4. 发现

### F1（已完成）eco 6 级 / 9 级整档残留

此前 `models/block/` 中存在 42 个 `l6/l9` 模型，贴图侧对应 58 张 `_b/_c` 资源。静态引用扫描显示
blockstates、Java 动态路径和 KubeJS 均未使用它们；beta3 客户端启动也没有出现模型或贴图错误。

**判定：这 42 个模型和 58 张贴图已删除。** 删除清单保存在 `build/asset-removal-l6-l9-bc.txt`，
后续如需回退可按清单从 Git 恢复。

### F2（P1）`textures/block/storage/` 混合了运行时资产与待审副本

当前生成器 `tools/generate_matrix_textures.py` 不读取这个目录，配置只处理物品矩阵贴图；但该目录也
不是可以整体移出的纯生成源目录。`models/block/cell/storage_cell_l1_chemical.json` 仍直接引用：

```text
neoecoprototype:block/storage/drive/cell_type
neoecoprototype:block/storage/drive/cell_housing
```

因此至少这两张必须继续留在运行时 `assets/`，当前运行时白名单为：

```text
textures/block/storage/drive/cell_housing.png
textures/block/storage/drive/cell_type.png
```

其余 storage 原图需逐文件核对模型入口、Java 动态路径和上游来源，确认后才能删除或移入
`tools/assets-source/`。不能按目录整体移动。

### F3（P1）剩余字节重复贴图

清理后重新统计为 11 组、22 个文件存在字节完全重复。旧报告中的 19 张已过时，其中一部分随 F1 一并删除。
典型重复仍需结合模型用途确认，不能只按哈希批量删除；相同内容的不同路径可能服务于不同运行时入口。

### F4（P1）剩余候选已完成第一轮分级

清理后审计工具仍列出 56 张没有模型直接引用的贴图，但其中 7 张 `_a` 已确认属于 L4 模型链，不是死资产。
审计工具此前只把它们列为孤儿，是因为没有展开完整模型入口/动态状态链。

```
block/computation_recolor/controller/screen_on_a
block/computation_recolor/controller_formed/controller_formed_a
block/storage/controller/controller_north_a
block/storage/controller/controller_side_a
block/storage/controller_formed/controller_formed_a
block/storage/energy_cell/cell_north_layer_a
block/storage/energy_cell/cell_side_a
```
runtime-static（已确认使用，保留）：
block/computation_recolor/controller/screen_on_a
block/computation_recolor/controller_formed/controller_formed_a
block/storage/controller/controller_north_a
block/storage/controller/controller_side_a
block/storage/controller_formed/controller_formed_a
block/storage/energy_cell/cell_north_layer_a
block/storage/energy_cell/cell_side_a

另有 2 个非 l6/l9 模型暂列待确认：

review-required:
models/block/computation_drive_full.json
models/block/crafting_controller/controller_l4_formed_auto.json

`computation_drive_full` 当前没有 blockstate 入口，可能是旧备用模型；
`controller_l4_formed_auto` 是自动合成控制器外观，但当前 `simplify_crafting_system.json` 没有接入它。
两者暂不删除，等功能注册和运行时用途确认后再处理。

### F5（暂缓）模型目录与命名双轨

本轮不继续处理模型清理。当前审查重点限定为存储矩阵相关贴图和模型；控制器、计算、合成等历史模型
即使暂未接入，也先保留，避免误删未完成功能。

后续新增模型采用按功能独立目录的方式，例如：

```text
models/block/storage_matrix/
models/block/computation_matrix/
models/block/crafting_matrix/
models/item/storage_matrix/
```

新功能不得继续把模型直接堆入旧的 `controller/`、`cell/` 或混合目录。命名双轨改造暂缓，等模型目录
重组有实际需求时一起处理。

### F6（P2）文档与代码漂移

`docs/kubejs.md` 里"固定家族材质"表格与 `api/MatrixMaterials` 已全面不一致
（小宗 / 猪咪 / 全能 / 量子四项全对不上）。只要映射存在第二份，就一定过期。
建议表格删除或改为指向代码。

---

## 5. 建议与优先级

### P0 —— 当前无待执行项

1. `l6/l9` 模型与 `_b/_c` 贴图已清理，并完成 beta3 启动回归。

### P1 —— 下一批需要逐个确认

2. 逐文件检查 `textures/block/storage/`，保留运行时使用的贴图；确认只是历史副本后再考虑移入源图目录。
3. 对剩余 56 个贴图孤儿候选建立动态资源白名单，再决定删除或保留。
4. 清理 11 组、22 个文件的字节重复贴图，前提是路径用途已确认。
5. 保留已确认使用的 7 张 `_a` 贴图；模型候选清理暂缓。

### P2 —— 制度性收尾

6. 把本次的**可达性分析固化进 `tools/audit_assets.py`**（当前版本仍主要扫 model JSON 的 `textures`
   字段，清理后报出 56 个孤儿候选，里面可能混有动态引用）。新版本应覆盖 blockstates + parent 链 + Java 常量，
   并把不可达项分级为“确认死 / 动态待审 / 生成源”；
7. 统一命名秩序；
8. 产物分层：改色目录中的资源属于生成结果，应显式标记或移出 `assets/`，避免手改被生成器覆盖。

### 验证方法（每次清理后必做）

1. `python tools/audit_assets.py` 前后对比数字；
2. 启动客户端，检查 `run/logs/latest.log` 是否出现
   `Failed to load texture` / `Unable to load model` / `missing texture`；
3. 进世界把存储 / 计算 / 合成三系控制器各放一次，看外观与成型态是否正常；
4. `/give` 一遍 `simplify_*_storage_cell_*` 与脚本矩阵，确认物品栏与驱动器内模型都在。

---

## 6. 不做的事

- **暂停继续扩展资产维度**（新容量档、新家族、新材质组合）：现有 KubeJS builder 已是发布能力，
  但在索引治理完成前继续增加组合会放大维护成本；
- **不重写 Java 侧索引**：`MatrixMaterials` 是对的，别动。

---

## 附录 A：已删除贴图 `*_b` / `*_c`（58 张）

```
block/computation_recolor/cable/cable_b                    block/computation_recolor/cable/cable_c
block/computation_recolor/cable/plug_b                     block/computation_recolor/cable/plug_c
block/computation_recolor/controller/controller_side_layer_b    block/computation_recolor/controller/controller_side_layer_c
block/computation_recolor/controller/screen_on_b           block/computation_recolor/controller/screen_on_c
block/computation_recolor/controller_formed/controller_formed_b    block/computation_recolor/controller_formed/controller_formed_c
block/computation_recolor/controller_formed/controller_network_switch_formed_c
block/computation_recolor/controller_formed/controller_power_network_switch_formed_c
block/computation_recolor/cooling_controller/controller_east_b    block/computation_recolor/cooling_controller/controller_east_c
block/computation_recolor/core/core_side_b                block/computation_recolor/core/core_side_c
block/computation_recolor/core/parallel_core_light_b      block/computation_recolor/core/parallel_core_light_c
block/computation_recolor/core/threading_core_light_b     block/computation_recolor/core/threading_core_light_c
block/computation_recolor/drive/cell_north_b              block/computation_recolor/drive/cell_north_c
block/computation_recolor/drive/cell_side_b               block/computation_recolor/drive/cell_side_c
block/crafting_recolor/controller/controller_north_b      block/crafting_recolor/controller/controller_north_c
block/crafting_recolor/controller/controller_side_b       block/crafting_recolor/controller/controller_side_c
block/crafting_recolor/controller/screen_on_b             block/crafting_recolor/controller/screen_on_c
block/crafting_recolor/controller_formed/controller_formed_b    block/crafting_recolor/controller_formed/controller_formed_c
block/crafting_recolor/controller_formed/controller_network_switch_formed_c
block/crafting_recolor/controller_formed/controller_power_network_switch_formed_c
block/crafting_recolor/core/core_side_b                   block/crafting_recolor/core/core_side_c
block/crafting_recolor/core/parallel_core_light_b         block/crafting_recolor/core/parallel_core_light_c
block/storage/controller/controller_north_b               block/storage/controller/controller_north_c
block/storage/controller/controller_side_b                block/storage/controller/controller_side_c
block/storage/controller_formed/controller_formed_b       block/storage/controller_formed/controller_formed_c
block/storage/energy_cell/cell_north_layer_b              block/storage/energy_cell/cell_north_layer_c
block/storage/energy_cell/cell_side_b                     block/storage/energy_cell/cell_side_c
block/storage_recolor/controller/controller_north_b       block/storage_recolor/controller/controller_north_c
block/storage_recolor/controller/controller_side_b        block/storage_recolor/controller/controller_side_c
block/storage_recolor/controller_formed/controller_formed_b    block/storage_recolor/controller_formed/controller_formed_c
block/storage_recolor/energy_cell/cell_north_layer_b      block/storage_recolor/energy_cell/cell_north_layer_c
block/storage_recolor/energy_cell/cell_side_b             block/storage_recolor/energy_cell/cell_side_c
```

## 附录 B：已删除模型 `*_l6_*` / `*_l9_*`（42 个）

```
block/computation_controller/controller_l6_{formed, formed_mirrored, off}
block/computation_controller/controller_l9_{formed, formed_mirrored, network_switch_formed,
                                            network_switch_formed_mirrored, off,
                                            power_network_switch_formed,
                                            power_network_switch_formed_mirrored}
block/computation_cooling_controller/controller_l6_{formed, formed_mirrored, off}
block/computation_cooling_controller/controller_l9_{formed, formed_mirrored, off}
block/computation_core/parallel_core_l6{,_formed}      block/computation_core/parallel_core_l9{,_formed}
block/computation_core/threading_core_l6{,_formed,_working}
block/computation_core/threading_core_l9{,_formed,_working}
block/crafting_controller/controller_l6_{formed, formed_auto, formed_mirrored, off}
block/crafting_controller/controller_l9_{formed, formed_auto, formed_mirrored,
                                        network_switch_formed, network_switch_formed_mirrored,
                                        off, power_network_switch_formed,
                                        power_network_switch_formed_mirrored}
block/crafting_core/parallel_core_l6{,_formed}         block/crafting_core/parallel_core_l9{,_formed}
```

## 附录 C：本轮已删除资产

本轮删除了 42 个 `l6/l9` 模型和 58 张 `_b/_c` 贴图。完整路径清单保存在构建过程文件：

```text
build/asset-removal-l6-l9-bc.txt
```

这些文件不再属于当前资源树，因此不应继续出现在“当前重复贴图”或“当前不可达模型”统计中。

## 附录 D：当前待审摘要

```text
贴图总数：186
模型总数：160
blockstates：31
模型引用缺失贴图：0
无模型直接引用的贴图候选：56
孤立 .mcmeta：0
字节重复贴图：11 组 / 22 个文件
```

其中 56 个贴图候选仍需结合 GUI、Java 动态路径、KubeJS 与生成器输入逐项确认，不能直接当作删除清单。
