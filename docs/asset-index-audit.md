# 资源索引审计报告

- 审计时间：2026-09-18
- 审计对象：`src/main/resources/assets/neoecoprototype`（贴图 / 模型 / blockstates）+ `src/main/java` 中的资源引用
- 审计方式：只读扫描 + 可达性分析，**未修改任何文件**
- 结论前提（已确认）：`_a/_b/_c` 是 eco 原版 4 / 6 / 9 三个等级的区分，本项目只做 4 级（`_a`），
  不打算做 L2 及其他等级

---

## 1. 结论摘要

资产树里混进了**一整档 eco 原版的高等级内容**（6 级 / 9 级），这部分是复制残留，确认不做即属死资产：

| 类别 | 数量 | 判定 |
|---|---|---|
| `*_l6_*` / `*_l9_*` 模型 | 42 个 | 死（blockstates 只接 `l4`） |
| `*_b` / `*_c` 贴图 | 58 张 | 死（对应 6 级 / 9 级） |
| `textures/block/storage/`（eco 原图本地副本） | 44 张中 42 张 | 死（`storage_recolor` 才是在用的） |
| 字节完全重复的贴图副本 | 19 张 | 冗余 |

合计可清理量级约 **120 张贴图 + 42 个模型**，占现有贴图总数（244）的将近一半。

**关键判断：Java 侧是干净的**——全仓库只有 6 处硬编码资源引用，模型路径已收敛在
`api/MatrixMaterials`。因此治理不需要动代码，只需要动资产树 + 让审计可信。

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
| 贴图总数 | 244（唯一内容 225，重复副本 19） |
| 其中 `*_recolor`（改色产物） | 191（78%） |
| 模型总数 | 202（`models/item` 71 + `models/block` 131） |
| blockstates | 31 |
| Java 硬编码资源引用 | 6 处 |
| **可达模型 / 不可达模型** | **158 / 44** |
| **可达贴图 / 不可达贴图** | **128 / 116** |

命名现状：
- 物品栏：`simplify_item_storage_cell_1k`（种类在前、容量在后）
- 驱动器内：`storage_cell_l1_item`（等级在前、种类在后）
- 两者字段顺序相反，是"索引难记"的直接来源。

---

## 4. 发现

### F1（P0）eco 6 级 / 9 级整档残留

`models/block/` 下存在三档模型：`controller_l4_*`（32 个）、`controller_l6_*`（17 个）、
`controller_l9_*`（25 个）。而 `blockstates` **只引用了 `l4`**，没有任何 `l6` / `l9`。

贴图侧与之对应：`_a`（4 级，在用）、`_b`（27 张）、`_c`（31 张），共 58 张只被 l6/l9 模型引用，
而这些模型本身不可达 → 连带全死。Java 与 blockstates 中均无 `_b` / `_c` 的独立引用。

**判定：确认不做 L2 及以上的前提下，这 58 张 + 42 个模型可以直接删。** 清单见附录 A、B。

### F2（P0）`textures/block/storage/` 是 eco 原图的本地副本

该目录 44 张贴图里 42 张不可达，实际在用的是 `textures/block/storage_recolor/`。
推测是当年从 eco 复制出来做改色母版的原图，改完忘了清。
而 computation / crafting 两个系列**只有 `*_recolor` 没有原图目录** —— 说明这个"保留母版"的做法
本身就执行得不一致。

**判定：源图在上游 `neoecoae` jar 里，本地这份母版可删；如确实要留，应移出 `assets/`
（不要被打进产物 jar），或统一到 `tools/` 下的源图目录。**

### F3（P1）19 张字节完全重复的副本

按 MD5 统计，244 张里只有 225 份唯一内容。典型：
`storage/controller/controller_side_a.png` ≡ `storage/energy_cell/cell_side_a.png`、
`storage_recolor/drive/quantum_cell_housing.png` ≡ `storage/drive/quantum_omni_cell_housing.png`。

其中 `_b` / `_c` 相关的重复会在 F1 清理时一并消失。**删 F1 后需重跑一次哈希统计再清理剩余。**

### F4（P1）7 张 `_a` 贴图 + 2 个模型不可达，需人工确认

这 7 张属于**在用的 4 级档**，却被判为不可达，很可能有动态引用，删前必须逐个确认：

```
block/computation_recolor/controller/screen_on_a
block/computation_recolor/controller_formed/controller_formed_a
block/storage/controller/controller_north_a
block/storage/controller/controller_side_a
block/storage/controller_formed/controller_formed_a
block/storage/energy_cell/cell_north_layer_a
block/storage/energy_cell/cell_side_a
```

另有 2 个不可达模型不属于 l6/l9：`block/computation_drive_full`、
`block/crafting_controller/controller_l4_formed_auto`。后者名字是 `l4`，可能是**还没接线的功能**
（自动合成控制器），删之前要确认是不是在做的功能。

### F5（P2）命名双轨

见第 3 节。Java 侧只有 6 处引用，改名成本低，建议放在最后统一做。

### F6（P2）文档与代码漂移

`docs/kubejs.md` 里"固定家族材质"表格与 `api/MatrixMaterials` 已全面不一致
（小宗 / 猪咪 / 全能 / 量子四项全对不上）。只要映射存在第二份，就一定过期。
建议表格删除或改为指向代码。

---

## 5. 建议与优先级

### P0 —— 确认后即可清理

1. 删 58 张 `_b` / `_c` 贴图 + 42 个 `l6` / `l9` 模型（附录 A、B）；
2. 删 `textures/block/storage/` 下 42 张原图母版（**先确认上游 jar 里有同源图**）。

### P1 —— 需要逐个确认

3. 逐条核对 F4 的 7 张 `_a` 与 2 个模型，确认是动态引用还是真的没接线；
4. 清理 F1 之后剩余的字节重复副本。

### P2 —— 制度性收尾

5. 把本次的**可达性分析固化进 `tools/audit_assets.py`**（当前版本只扫 model JSON 的 `textures`
   字段，报出的 80 条"孤立"里混着大量假阳性，导致报告没人敢信）。
   新版本应：覆盖 blockstates + parent 链 + Java 常量，并把不可达项**分级**
   （确认死 / 疑似动态引用 / 等级残留）；
6. 统一命名秩序；
7. 产物分层：`*_recolor` 是生成物，应显式标记或移出 `assets/`，避免手改被生成器覆盖。

### 验证方法（每次清理后必做）

1. `python tools/audit_assets.py` 前后对比数字；
2. 启动客户端，检查 `run/logs/latest.log` 是否出现
   `Failed to load texture` / `Unable to load model` / `missing texture`；
3. 进世界把存储 / 计算 / 合成三系控制器各放一次，看外观与成型态是否正常；
4. `/give` 一遍 `simplify_*_storage_cell_*` 与脚本矩阵，确认物品栏与驱动器内模型都在。

---

## 6. 不做的事

- **不新增对外扩展点**（KubeJS builder、新容量档、新家族）：当前无真实需求，
  而每加一个维度，外观与后端的组合数翻倍，在索引治理完成前属于给混乱加杠杆；
- **不重写 Java 侧索引**：`MatrixMaterials` 是对的，别动。

---

## 附录 A：可删贴图 `*_b` / `*_c`（58 张）

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

## 附录 B：可删模型 `*_l6_*` / `*_l9_*`（42 个）

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

## 附录 C：字节完全重复的贴图（19 张多余副本）

```
[2] block/computation_recolor/casing_side.png == block/computation_recolor/casing_side_west.png
[2] block/storage_recolor/controller/controller_side_a.png == block/storage_recolor/energy_cell/cell_side_a.png
[2] block/storage_recolor/controller/controller_side_b.png == block/storage_recolor/energy_cell/cell_side_b.png
[2] block/storage_recolor/controller/controller_side_c.png == block/storage_recolor/energy_cell/cell_side_c.png
[2] block/storage_recolor/drive/cell_type.png == block/storage/drive/cell_type.png
[2] block/storage_recolor/drive/quantum_cell_housing.png == block/storage/drive/quantum_omni_cell_housing.png
[2] block/storage_recolor/drive/small_bulk_cell_housing_expanded.png == block/storage/drive/mega_cell_housing.png
[2] block/storage/controller/controller_side_a.png == block/storage/energy_cell/cell_side_a.png
[2] block/storage/controller/controller_side_b.png == block/storage/energy_cell/cell_side_b.png
[2] block/storage/controller/controller_side_c.png == block/storage/energy_cell/cell_side_c.png
[2] block/crafting_recolor/controller/controller_side_a.png == block/crafting_recolor/core/core_side_a.png
[2] block/crafting_recolor/controller/controller_side_b.png == block/crafting_recolor/core/core_side_b.png
[2] block/crafting_recolor/controller/controller_side_c.png == block/crafting_recolor/core/core_side_c.png
[2] block/crafting_recolor/core/parallel_core_light_a.png == block/computation_recolor/core/parallel_core_light_a.png
[2] block/crafting_recolor/core/parallel_core_light_a_on.png == block/computation_recolor/core/parallel_core_light_a_on.png
[2] block/crafting_recolor/core/parallel_core_light_b.png == block/computation_recolor/core/parallel_core_light_b.png
[2] block/crafting_recolor/core/parallel_core_light_b_on.png == block/computation_recolor/core/parallel_core_light_b_on.png
[2] block/crafting_recolor/core/parallel_core_light_c.png == block/computation_recolor/core/parallel_core_light_c.png
[2] block/crafting_recolor/core/parallel_core_light_c_on.png == block/computation_recolor/core/parallel_core_light_c_on.png
```

## 附录 D：不可达模型分布（44 个）

```
13  block/crafting_controller          10  block/computation_controller
10  block/computation_core              6  block/computation_cooling_controller
 4  block/crafting_core                 1  block/computation_drive_full（非 l6/l9，需确认）
```
（`block/crafting_controller/controller_l4_formed_auto` 为 l4 却不可达，疑似未接线功能，需确认）
