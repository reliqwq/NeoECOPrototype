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

L1 处理器装配室是 AE2 分子装配室的绿色变体，只接受处理样板，用任意样板供应器与L1 处理器装配室即可合成处理器与本模组提供的物品。

## Components

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_stonecutting_assembler" />
  <ItemIcon id="neoecoprototype:simplify_pattern_provider" />
</ItemGrid>


## 配方

有三条内置基础配方覆盖 原版AE2 处理器：

| 处理器 | 材料 |
|--------|------|
| 逻辑处理器 | 硅 + 红石 + 金锭 |
| 计算处理器 | 硅 + 红石 + 石英水晶 |
| 工程处理器 | 硅 + 红石 + 钻石 |

装配器还接受从 AE2 压印器 press 配方自动推导的处理器配方，其他模组给压印器加的配方无需任何数据文件即可使用。
服务器管理员可在服务端配置中调整：关闭推导（`derive_processor_recipes_from_inscriber`），或按输出排除（`disabled_processor_recipes`）。

插入速度卡与能量卡加速——各有 5 个卡槽于原版分子装配室加速规则一致，满卡即是每2刻一次合成。

## 批量合成

装配室接受同一份样板的多倍材料：一次推送里带了几份完整材料，就在**一个**合成周期里一次做完，产出按份数翻倍。产出上限是一组（64 个），**耗时不变**——多倍材料不会让这一次合成更久，要更快仍然只能加速速度卡。
例如你可以写一份64个红石粉，64个硅，64个金锭处理合成64个逻辑处理器，装配室会一次合成，需知样板产物不可超过装配室输出槽上限64个。

## 样板供应器

绿色的L1样板供应器存放装配器的处理样板，并把工作推进装配室，与 AE2 样板供应器驱动分子装配室的方式相同。
提供更多槽位（27 个，AE2 是 9 个），而且它的合成配方一次给出 **2 台**，实际上这是最有性价比的样板供应器。
