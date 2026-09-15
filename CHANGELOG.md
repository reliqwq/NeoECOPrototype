# Changelog

## 1.1.1 (2026-09-15)

### Dependencies

- Follow Neo ECO AE Extension **21.2.0-beta2**.
- This update follows the upstream beta2 FastPath, crafting recovery, storage responsibility, and UI compatibility changes.
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
