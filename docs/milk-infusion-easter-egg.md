# 2i 牛奶灌注彩蛋（1.21.1）

本体 Neo ECO AE Extension 注册 `neoecoae:eco_infinite_sand_cell`，并负责资源盘的存储行为、纹理和机架模型。Prototype 负责彩蛋配方、牛奶化学品、金属灌注机的牛奶流体接口，以及 JEI/EMI 的配方隐藏。本体不包含彩蛋配方和机器兼容代码。

同时安装 ECO、Mekanism、Useless Stretcher 和 Prototype，并且 ECO 已注册沙子盘时，加载 `neoecoprototype:compat/useless_stretcher/infinite_sand_cell`：

`useless_stretcher:two_i_doll` ×1 + 1000 mB 牛奶 → `neoecoae:eco_infinite_sand_cell` ×1。

- 对应机器为普通金属灌注机，沿用原版加工时间和能耗，不扩展灌注工厂。
- 输入面跟随机器原有的化学品输入配置，默认右侧。输入面关闭、设为仅输出或槽内有其他化学品时拒收牛奶。
- 接受 NeoForge 启用的 `minecraft:milk`，也接受 `c:milk` 标签中的其他牛奶流体。AE2 的流体输出总线和 Mek 机械管道使用同一流体能力接口。
- 每 1 mB 牛奶转换成 1 单位 `neoecoprototype:milk`，直接保存到原有灌注槽。配方每次消耗 1000 单位，`per_tick_usage` 为 `false`，不会每刻消耗一桶。
- 牛奶流体接口只允许输入；灌注槽仍可用原界面清空。物品输出总线送入牛奶桶不会自动转换。
- JEI 隐藏真实配方，EMI 同时过滤原始 ID 和 JEI 桥接改写的 ID，不移除服务端机器配方。
- 老版本 ECO 缺少沙子盘时，`neoforge:item_exists` 条件使配方跳过，保持原有最低 ECO 依赖兼容。

沙子盘只无限供应普通沙子，复用本体原有无限基础资源盘的纹理；原盘仍供应水、圆石和岩浆。

## 验证记录

2026-10-27：ECO 本体的 6 项针对性测试及 JAR 构建通过；Prototype 全部 50 项单元测试及离线构建通过。新增测试直接调用 AE2 的 `HandlerStrategy.FLUIDS`，验证注册名为 `minecraft:milk` 的流体能够模拟和实际插入兼容接口，模拟过程不修改灌注槽；并覆盖容量限制、清空以外不允许排液、异种流体与化学品拒收、原生化学槽存档恢复、输入面配置、三模组的八种加载组合及 JEI/EMI 配方标识过滤。

已检查两份 JAR 的功能归属：本体包含沙子盘注册、模型和语言资源，不含彩蛋配方或牛奶兼容类；Prototype 包含配方与兼容类，不打包 ECO 或 Mek 的类。尚未启动客户端或专服进行实际机器加工和 JEI/EMI 界面验证。
