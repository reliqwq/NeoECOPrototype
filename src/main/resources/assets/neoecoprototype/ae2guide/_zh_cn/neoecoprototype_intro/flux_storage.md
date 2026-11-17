---
navigation:
  title: FE存储矩阵
  icon: neoecoprototype:simplify_fe_storage_cell_1m
  position: 21
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_fe_cell_housing
  - neoecoprototype:simplify_fe_storage_cell_1k
  - neoecoprototype:simplify_fe_storage_cell_1m
  - neoecoprototype:simplify_fe_storage_cell_4m
---

# FE存储矩阵

它需要 **AppliedFlux（应用通量，modid `appflux`）**。

## 容量

| 矩阵 | 字节 | 默认换算 |
| --- | --- | --- |
| ECO - L1 1K FE存储矩阵 | 1,024 | 约 10.7 亿 FE |
| ECO - L1 1M FE存储矩阵 | 1,048,576 | 约 1.1 万亿 FE |
| ECO - L1 4M FE存储矩阵 | 4,194,304 | 约 4.4 万亿 FE |


## 配方

> 这一族**只有装了 AppliedFlux 才会注册**。

- **ECO - L1 FE存储矩阵外壳**：合成台 `ABA / B B / CCC` —— 2 个绿晶晶格 + 3 个红石 + 3 个 `appflux:harden_insulating_resin`。
- **ECO - L1 1K FE存储矩阵**：外壳 + AE2 的 1K 元件，无序。
- **ECO - L1 1M FE存储矩阵**：外壳 + ECO - L1 存储组件，无序。
- **ECO - L1 4M FE存储矩阵**：外壳 + ECO - L4 存储组件，无序。
