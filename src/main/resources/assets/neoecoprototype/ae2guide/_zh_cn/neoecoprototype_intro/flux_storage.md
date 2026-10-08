---
navigation:
  title: FE存储矩阵
  icon: neoecoprototype:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype_intro/storage_system.md
item_ids:
  - neoecoprototype:simplify_fe_cell_housing
  - neoecoprototype:simplify_fe_storage_cell_1m
  - neoecoprototype:simplify_fe_storage_cell_4m
---

# FE存储矩阵

这类矩阵把**能量**当成网络内容来存：往网格里灌 FE 就存进去，要用的再从任意接口取出来。它们是 eco 自家FE存储矩阵之下的 L1 档。

这一族只有在装了 **AppliedFlux（应用通量，modid `appflux`）** 时才存在——那个 mod 自己会带上 GuideME 与 Glodium。没装它，这三件物品根本不会被注册：不进创造栏、没有配方、也不进世界。

## 容量按字节算，不是按 FE 算

和所有 AE2 存储单元一样，FE仓的尺寸单位是**字节**，而一个字节值多少 FE 由 AppliedFlux 决定：它的 `flux_cell.amount` 配置，出厂是**每字节 1,048,576 FE**。

| 矩阵 | 字节 | 按默认换算等于 |
| --- | --- | --- |
| L1 1M FE存储矩阵 | 1,048,576 | 约 1.1 万亿 FE |
| L1 4M FE存储矩阵 | 4,194,304 | 约 4.4 万亿 FE |

这两个字节数正好等于 AppliedFlux 自己的 `fe_1m_cell` 与 `fe_4m_cell`，所以我们的矩阵与它们同尺，而不是低一档。把 AppliedFlux 配置里的 `flux_cell.amount` 调大，游戏里每一只FE仓（包括我们的）都会变得更宽松，材料不变。

## 怎么做

先用绿晶矩阵、红石和 AppliedFlux 的硬化绝缘树脂做外壳，再拿外壳与我们对应档位的存储成分（1M 或 4M）合成。另有一条拆回配方，把这两半还给你。

## 能插在哪

两只矩阵都报 L1 档位，所以 [L1 存储系统](storage_system.md) 的一台主机直接收，不需要任何白名单条目；eco 更高档的主机也照样收得下。悬停提示里 FE 实际数量走 eco 的FE仓那一行，旁边同时给出字节数。

在 L1 存储主机面板里它们**自己占一行**，那一行的名字不是我们写的，用的是 eco 注册的那个通量单元类型自带的名（"能源"）——同一格插在 eco 自己的主机里也是这个名。行里的两根条仍然是字节口径。
