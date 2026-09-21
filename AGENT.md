# AGENT.md

本文件是 Neo ECO Prototype 仓库的开发约束与协作准则，供后续参与本项目的开发者与 AI agent 使用。

新加入本项目时，请先读完本文件，再动代码。

---

## 1. 项目定位

Neo ECO Prototype 是 **Neo ECO AE Extension（eco）的非官方附属模组**，同时依赖 Applied Energistics 2（AE2）。

一句话定位：

```text
我们是 eco 的附属。
我们要发挥 eco 的优势，再加上自己的东西。
```

这意味着：

- eco 已有的能力，我们**复用**，不重写。
- AE2 已有的能力，我们**调用**，不重写。
- 只有 Trinity 独有的形态与交互，才值得写新代码。

Trinity（本项目新增的 3x3x3 多方块）是一个 **eco 多方块形态的设备**，不是一套独立的合成系统。

---

## 2. 铁律：不重复造轮子

### 动手前必须自问

```text
这件事 eco 或 AE2 已经做了吗？
```

如果答案是「做了」，Trinity 只允许做三件事：

```text
调用它   -> 走它公开的 API
展示它   -> 把它的结果呈现给玩家
解释它   -> 把失败原因翻译成可执行的下一步
```

如果答案是「没做」，才考虑写新逻辑，并且必须说明为什么 eco/AE2 无法覆盖。

### 具体的职责划分

| 能力 | 归属 | Trinity 的做法 |
| --- | --- | --- |
| 存储体系 | eco | 复用，不实现自己的存储引擎 |
| 计算体系 | eco | 复用，不实现自己的 CPU |
| 合成树递归计算 | AE2 `ICraftingService` | 请求并展示结果，不自己展开 |
| 样板选择 | AE2 planner | 以 AE2 最终计划为准，不伪造选择 |
| 替代输入选择 | AE2 planner | 只做只读提示，不替代 AE2 决策 |
| 多方块注册与校验 | eco | 复用 eco 的多方块框架 |
| 任务语义 | **Trinity** | 目标、数量、进度、结果由 Trinity 拥有 |
| 统一界面 | **Trinity** | 一个面板完成设置与查看 |
| 诊断与建议 | **Trinity** | 失败原因的翻译与下一步建议 |
| 紧凑形态 | **Trinity** | 把存储/计算/合成收进一个 3x3x3 |

判断标准很直接：**表里归属不是 Trinity 的行，就不该出现 Trinity 自己写的算法。**

---

## 3. 明确禁止

### 不要复制 eco 或 AE2 的内部实现

以下类型的类**禁止复制或照搬**到本仓库：

```text
ECOCraftingCPULogic
ECOCraftingThread
ECOExecutionRuntime
FastPath 及其内部结构
ECO 的合成计划实现
AE2 的合成树展开算法
```

也不要把 eco 的类反编译后整体搬进来改。

### 不要接管 AE2 的 CPU

Trinity 的「计算字节」是 Trinity 自己的容量，**不是** AE2 的 `ICraftingCPU`。

不要在 Trinity 里注册假的 `ICraftingCPU`，也不要绕过 `submitJob` 自己调度合成。

### 不要修改依赖 jar

`neoecobeta/`、`libs/` 下的依赖是只读参考，不提交、不修改、不打补丁。

需要兼容性处理时，优先使用公开 API；确实需要 Mixin 时必须有明确理由并单独记录。

### 不要新增第二套生命周期

一轮弯路的具体教训：

> 曾经为了做「递归原料预检」，在 `TrinityCraftingExecutor` 里新增了一套 `previewFuture / previewPlan / tickPreview` 的并行计划生命周期。

这是错的，原因有两点：

1. AE2 的 `beginCraftingCalculation` 已经负责递归展开，Trinity 再算一遍必然与真实提交不一致。
2. 两套生命周期会让「界面显示的计划」和「实际提交的计划」产生分歧。

已撤回。**任何「预检/预览」都不应该复制一套计划生命周期。**

---

## 4. 已知的设计限制

### 预检不是最终计划

Trinity 的只读预检只能展示本地观察结果：目标/数量格式、网络连接、已加载样板候选和直接库存可见性。它不能选择最终样板、替代输入、递归材料树或 CPU，也不能据此宣称任务已经可执行。Trinity 计算模块的细胞容量只用于配置展示，不是 AE2/eco CPU 容量。

因此：

```text
预检  ->  Trinity 的 advisory 提示
提交  ->  以 AE2 ICraftingService 的最终计划为准
```

界面文案必须诚实，使用「预检候选」「材料预检」「提交时由 AE2 最终规划」等措辞，不能暗示 Trinity 已经锁定选择或保证可执行。

这个限制**不应该靠 Trinity 自己再写一套选择逻辑来解决**。真实的样板选择、替代输入、递归展开、材料缺失和 CPU 选择都交给 AE2/eco 的统一 planner；Trinity 只翻译结果并给出下一步建议。

---

## 5. Trinity 的技术事实

### 结构

固定 3x3x3，控制器位于中心。

```text
控制器偏移   (1, 1, 1)
存储模块     (0, 1, 1)
计算模块     (2, 1, 1)
合成模块     (1, 1, 0)
```

### 物理要求

- AE2 分子装配室必须**紧贴合成模块**（模块外壳唯一没被覆盖的面）。
- 网络中必须存在 AE2 合成存储器（如 `ae2:1k_crafting_storage`），否则无法提交任务。
- 计算模块提供的计算字节**不能**充当 AE2 CPU。

### 执行状态机

```text
IDLE -> PLANNING -> RUNNING -> WAITING_OUTPUT -> COMPLETED / FAILED
```

主要实现位于 `TrinityCraftingExecutor`。

---

## 6. 环境与版本

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251（开发/测试用加载器；发布物下限仍是 `[21.1.0,)`，抬版本只为让 `ae2_pattern_disk` 能在 dev 里加载） |
| AE2 | 19.2.17（`ae2`） |
| Neo ECO AE Extension | 21.2.0-beta4（`neoecoae`） |
| Java | 21 |

本模组 id 为 `neoecoprototype`。

---

## 7. 构建与验证

### 常规验证

```powershell
.\gradlew.bat test --offline
.\gradlew.bat runGameTestServer --offline
git diff --check
```

当前基线：

```text
test                BUILD SUCCESSFUL
runGameTestServer   All 18 required tests passed
git diff --check    通过
```

### 客户端

启动客户端前，必须先关闭旧客户端，然后**只启动一个**新客户端：

```powershell
.\gradlew.bat runClient
```

不要同时开多个客户端实例，也不要在未关闭旧实例的情况下反复启动。

### 运行时预期告警

以下告警是环境性的，**不是 Trinity 的故障**：

- ExtendedAE、Omni Cells、UselessMod 的 Mixin class-not-found 告警。
- Mojang 会话服务器超时（`sessionserver.mojang.com` 读取超时）。
- `VanillaPackResourcesBuilder` 的 `unexpected schema` 告警。

不要把上述内容当成回归。

---

## 8. GUI 约定

- 玩家可见文本使用**中文**；日志保留英文原文，便于排查。
- 状态行按**像素宽度**换行（中文字符约 9px，ASCII 约 6px），不要按字符数换行。
- 面板高度必须能在 1280x720 / GUI scale 2 下完整显示。
- 颜色只标真正的问题行：

```text
红色  失败 / 无效 / 未连接 / 缺少 / 不足 / 不可执行
绿色  已完成 / 就可以执行
```

- 不要为了「看起来一致」把正常信息也标成绿色。例如「任务设置有效」不应使用绿色，否则会和紧随其后的红色「失败」冲突。
- 区分字段校验和材料检查：

```text
任务设置  只校验目标 ID 格式与数量
材料      真正的库存检查
```

- 保留 eco 原生侧边按钮栏（`HostSideButtonBar`）。它是**竖排设计**，属于正常形态，不要误判为布局溢出而删除。

---

## 9. 代码风格

- 现有的 `TrinityPatternCheck` / `TrinityResourceCheck` / `TrinityTaskRuntime` 都是**只读**模型，不要在预检路径里做提取或变更操作。
- `storageParts` / `computationParts` / `craftingParts` 仅用于 advisory 扫描和展示，不代表 Trinity 拥有 eco 子系统，也不构成 CPU/存储接入。
- `TrinityServiceAdapters` 只适配 Trinity 自有模块；不要重新加入扫描 L1/eco 部件的服务工厂。
- 库存查询使用 `Actionable.SIMULATE`；只有真实回收才使用 `MODULATE`。
- 新增可测试的纯逻辑时，优先做成不依赖 Minecraft 世界的结构，便于单元测试覆盖。
- 涉及世界状态的行为，用 GameTest 覆盖，不要只写单元测试。

---

## 10. 完成任务前必须确认

在声称「完成」之前，逐条确认：

```text
[ ] 没有复制 eco / AE2 的内部算法
[ ] 没有新增第二套合成计划或生命周期
[ ] 复用了 eco / AE2 的公开 API
[ ] 界面文案没有夸大 Trinity 的能力
[ ] .\gradlew.bat test --offline 通过
[ ] .\gradlew.bat runGameTestServer --offline 通过
[ ] git diff --check 通过
[ ] 涉及界面改动时，已在真实客户端中确认显示正常
```

如果某个功能无法在保持以上约束的前提下实现，应当先回到定位问题讨论，而不是先把代码写出来再说。

---

## 11. 反面教材速查

| 错误做法 | 正确做法 |
| --- | --- |
| 自己写递归合成树 | 请求 AE2 计划并展示 |
| 自己选样板和替代输入并当作最终结果 | 只做只读提示，以 AE2 计划为准 |
| 为预检新增一套 future/plan 生命周期 | 复用 AE2 计算，最多做一次只读查询 |
| 把 eco 的类反编译搬进来改 | 使用 eco 公开 API 接入 |
| 自己实现存储引擎或 CPU | 复用 eco 的存储与计算体系 |
| 把竖排的 eco 侧边栏当布局 bug 删掉 | 保留，它是原生形态 |

---

## 12. 我们要加的「自己的东西」

不是再写一个合成算法，而是：

1. **紧凑形态**：把存储、计算、合成收进一个 3x3x3 多方块。
2. **统一界面**：一个面板完成目标设定、状态查看和结果确认。
3. **任务语义**：目标是「任务」而非「持续运行的设备」。
4. **诊断与建议**：失败时给出玩家可直接执行的下一步。
5. **边界清晰**：不接管 CPU、不复制 FastPath、不改 eco 内部实现。

这五点才是 Trinity 的价值所在。
