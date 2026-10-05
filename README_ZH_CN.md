# Neo ECO Prototype

Neo ECO Prototype 是 Neo ECO AE Extension 与 Applied Energistics 2 的非官方附属模组，面向 Minecraft 1.21.1 / NeoForge。它把 eco 的多方块机器下放到普通生存网络前期就能搭起的 L1 档，并补上这一档需要的接口、存储单元与样板供应器。

[English](README.md)

## 提供什么

存储侧从 L1 起：驱动器、能量单元与小宗存储矩阵。小宗矩阵默认标记 3 种资源，在工作台上用扩展卡升到 10 种；容量按 long 计数，字节上限就是 `Long.MAX_VALUE`。整个家族需要 MegaCells；物品变体沿用 eco 的 MEGA 长储后端，所以被标记的物品按压缩链存储。

L1 驱动器默认只挂载本家 L1 单元与小宗单元。别的元件要服务端管理员显式放行才会挂载。

合成与计算沿用 eco 的结构、L1 的数值。处理器装配室保留 AE2 分子装配室的九格原料布局，另外接受本模组的处理器配方：一次推送里带几份完整材料，就在一个合成周期里全部做完，产物上限一组（64 个）。

任意一台样板供应器都能喂它，包括 AE2 原版的。我们那台样板槽 27 个（AE2 是 9 个），配方一次出 2 台。

两台接口负责供电与大额缓存。供能接口有 18 个标记槽，注入 200 AE/t。盈能超导接口布局相同，单个标记槽可缓存 8192 个物品或 512,000 mB 流体，注入 4000 AE/t。AE2 每个网格只运行一个被动发电源，并永远选功率最高的那台，所以两者不会叠加。

Trinity 多方块（三合一联合体）把存储、配置与合成收进同一个结构，目前仍属实验内容：它不进 JEI，物品上带一行"未实现"提示，等设计定稿再向玩家开放。

## 环境要求

| 组件 | 版本 |
| --- | --- |
| Minecraft | 1.21.1 |
| NeoForge | 21.1.251 或兼容的 21.1.x |
| Applied Energistics 2 | 19.2.17 或兼容的 19.2.x |
| Neo ECO AE Extension | 21.2.1-beta2 或更高的兼容版本 |
| Java | 21 |
| MegaCells | 可选；启用小宗存储矩阵时必需 |
| Mekanism 与 Applied Mekanistics | 可选；启用化学品单元与化学品矩阵时必需 |

客户端和服务端都要装本模组。

## 安装

先安装 Minecraft 1.21.1 和 NeoForge，再装 Applied Energistics 2、GuideME、LowDragLib2 与 Neo ECO AE Extension，版本按上表。要小宗矩阵就加 MegaCells，要化学品存储就加 Mekanism 与 Applied Mekanistics。最后把本模组的发布 JAR 放进 `mods` 目录，启动游戏。

发布 JAR 不打包上面任何一个依赖，请从各自的维护者处获取，并遵守其许可条款。

## L1 驱动器挂载哪些元件

更高级的 eco 元件可以插进去，但在服务端管理员点名之前不会挂载。要放行指定元件，改服务端配置：

```toml
[l1_storage]
additional_storage_cells = ["neoecoae:eco_mega_long_bulk_cell"]
```

## KubeJS

KubeJS 是可选的。脚本在启动事件与配方事件里注册存储矩阵与配方，包括 eco 的集成工作站配方类型；`neoecoprototype:infinite_storage_matrix` 构建器可以做自定义的无限物品或流体矩阵。脚本改不了多方块结构定义与控制器行为。示例见 [docs/kubejs.md](docs/kubejs.md)。

## 从源码构建

按 `build.gradle` 列出的清单，把合规获取的、版本完全对应的依赖放进 `libs/`，然后执行：

```powershell
.\gradlew.bat build --offline
```

从全新克隆直接构建会停下来，报它缺的是哪几个 JAR——本仓库不转发任何依赖。产物是 `build/libs/neoecoprototype-3.0.0-beta1.jar`。`libs/`、`run/`、`build/`、参考检出与本地世界数据都不要提交进仓库。

依赖下限为什么这么写、eco 那条为什么要带预发布版本号，记在 [docs/ae2-extension-playbook.md](docs/ae2-extension-playbook.md)。一次发布要带什么、怎么发，记在 [docs/publishing.md](docs/publishing.md)。

## 参与

提 PR 之前请先读 [CONTRIBUTING.md](CONTRIBUTING.md)。问题报告请给出 Minecraft、NeoForge、Neo ECO AE Extension、Java 与本模组的版本，外加一段相关日志。

## 许可

代码以 GNU General Public License 3.0 only 发布，见 [LICENSE](LICENSE)。

贴图与模型属美术资源，除 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 另注明者外保留所有权利。未经许可不得单独提取、复用或再分发美术资源，需保留的署名必须保留。

版权所有 (C) 2026 reliqwq。

## 鸣谢

reliqwq 编写并维护本模组。Neo-TiX 是原主美与美术贡献者，寒冰亦贡献了美术内容，两处的署名均已获许可保留。DancingSnow 是本模组必需的前置 Neo ECO AE Extension 的作者，同团队的 Yang120 提供了大量帮助。

## 免责声明

Neo ECO Prototype 与 Mojang、Microsoft、Applied Energistics 2、Neo ECO AE Extension 均无隶属或背书关系。Minecraft 及相关名称为其各自所有者的商标。
