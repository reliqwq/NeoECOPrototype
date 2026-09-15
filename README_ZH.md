# Neo ECO Prototype

Neo ECO Prototype 是 Neo ECO AE Extension 和 Applied Energistics 2 的非官方附属模组。它为 Minecraft 1.21.1 / NeoForge 增加更低层级的 L1 存储、计算和合成多方块结构。

本项目是 Neo ECO AE Extension 和 Applied Energistics 2 的非官方附属模组，为 Minecraft 1.21.1 / NeoForge 增加 L1 存储、计算和合成子系统。

## 功能

- L1 存储、计算和合成多方块结构定义。
- 为三种 L1 结构提供 JEI 多方块搭建预览。
- L1 存储元件、驱动器、能源元件、接口和辅助方块。
- 为 Neo ECO AE Extension 预览运行时提供专用服务器安全的合成接口回退方案。
- 附属模组自有的模型、纹理、翻译和配方文件组织在 `neoecoprototype` 命名空间下；部分视觉资源改编自上游 Neo ECO AE Extension 资源，并保留其上游许可证。

## 兼容性

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233 或兼容的 21.1.x 版本 |
| Applied Energistics 2 | 19.2.17 或兼容的 19.2.x 版本 |
| Neo ECO AE Extension | 21.2.0-beta2 或兼容版本 |
| Java | 21 |

### 平台环境

本模组需要同时安装在客户端和服务端：CurseForge 项目及每个上传文件都应配置为 `Client: Required` 和 `Server: Required`。NeoForge 元数据将所有必需依赖声明为 `side = "BOTH"`；CurseForge 环境标签是发布平台元数据，不会存储在 `neoforge.mods.toml` 中。

Neo ECO Prototype 不隶属于 Mojang、Microsoft、Applied Energistics 2 或 Neo ECO AE Extension，也未获得其认可。Minecraft 及相关名称是其各自所有者的商标。

## 安装

1. 安装带 NeoForge 的 Minecraft 1.21.1。
2. 安装必需的运行时依赖：Applied Energistics 2、GuideME、LowDragLib2 和 Neo ECO AE Extension。
3. 将 Neo ECO Prototype 发布 JAR 放入 `mods` 目录。
4. 启动游戏，并确认已安装所需依赖版本。

发布 JAR 不捆绑上述依赖 JAR。请从各自维护者处获取，并遵守其许可证和分发规则。

## KubeJS 支持

KubeJS 为可选。使用 `ServerEvents.recipes` 来添加、移除或替换附属模组配方，包括 Neo ECO AE Extension 的集成工作站配方类型。脚本示例见 [docs/kubejs.md](docs/kubejs.md)。KubeJS 脚本不会改变多方块结构定义或控制器行为。

## 开发构建

当前开发配置使用已发布的 Neo ECO AE Extension `21.2.0-beta2` JAR，位于 `neoecobeta/` 下，用于离线兼容性测试。本地开发副本有意从 GitHub 排除。GitHub 上稳定的 `v1.1.0` 发布版本仍是与本开发树分离的独立产物；这条跟随 beta2 的开发线版本为 `1.1.1`。

所需的本地开发文件列于 `build.gradle`。将精确兼容依赖的合法副本放入 `libs/`，然后运行：

```powershell
.\gradlew.bat build --offline
```

输出写入 `build/libs/neoecoprototype-1.1.1.jar`。

不要提交 `libs/`、`run/`、`build/`、参考检出或本地世界数据。依赖许可信息见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

## 源码与发布政策

本仓库仅包含 Neo ECO Prototype 的源码和资源。本地运行时产物、修改过的 Neo ECO AE Extension 开发副本、参考检出和依赖二进制文件不属于本仓库。

发布应包含：

- 模组 JAR；
- 对应的源码修订；
- 所需依赖版本；
- 本仓库的许可证和第三方声明。

## 贡献

在提交拉取请求前，请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。Bug 报告应包含 Minecraft、NeoForge、Neo ECO AE Extension、Java 和模组版本，以及相关日志摘录。

## 许可证

Neo ECO Prototype 代码仅按 GNU 通用公共许可证第 3 版发布。见 [LICENSE](LICENSE)。

纹理和模型属于美术资源；除非 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 中声明了特定上游许可证，否则保留**所有权利（All Rights Reserved, ARR）**。未经许可，不得单独提取、复用或再分发美术资源。必须保留必需的署名。

版权所有 (C) 2026 reliqwq。

## 鸣谢

- `reliqwq`：附属模组作者和维护者。
- `Neo-TiX`：原首席美术师和美术贡献者；经许可保留署名。
- `寒冰`：原美术贡献者；经许可保留署名。
- `DancingSnow`：Neo ECO AE Extension 的原作者，该模组是必需的上游模组。
- `Yang120`：特别感谢原 Neo ECO AE Extension 团队的大力帮助。