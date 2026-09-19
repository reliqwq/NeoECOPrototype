---
navigation:
  title: L1 处理器装配室
  icon: neoecoprototype:simplify_stonecutting_assembler
  position: 30
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_stonecutting_assembler
  - neoecoprototype:simplify_pattern_provider
---

# L1 处理器装配室

L1 处理器装配室是 AE2 分子装配室的绿色变体，用 AE2 处理样板合成处理器。搭配一台 <ItemLink id="neoecoprototype:simplify_pattern_provider" />，即可像平常一样向网络请求处理器。

## Components

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>


## 配方

三条内置配方覆盖 AE2 处理器：

| 处理器 | 材料 |
|--------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 计算处理器 | 硅 + 红石 + 石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |

装配器还接受从 AE2 压印器 press 配方自动推导的处理器配方——其他模组给压印器加的配方无需任何数据文件即可使用。服务器管理员可在服务端配置中关闭推导（`derive_processor_recipes_from_inscriber`）或按输出排除（`disabled_processor_recipes`）。

插入速度卡与能量卡加速——各有 5 个卡槽。

## 样板供应器

绿色样板供应器存放装配器的处理样板，并把工作推进装配室，与 AE2 样板供应器驱动分子装配室的方式相同。
