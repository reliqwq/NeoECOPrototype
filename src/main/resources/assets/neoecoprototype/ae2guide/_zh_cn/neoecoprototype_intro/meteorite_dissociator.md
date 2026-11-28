---
navigation:
  title: 陨石核心解离器
  icon: neoecoprototype:meteorite_dissociator
  position: 25
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:meteorite_dissociator
---

# 陨石核心解离器

**陨石核心解离器**把陨石核心拆成天石、粉尘、晶体等多种产物。把核心放进中间的大槽，按罐中显示的流体装满两侧浴液，再给机器供电即可。核心旁的小槽放催化剂，可提供常驻加成且不会被消耗。

<ItemGrid>
  <ItemIcon id="neoecoprototype:meteorite_dissociator" />
  <ItemIcon id="neoecoprototype:meteorite_core" />
</ItemGrid>

## 自动化面

面板第一个按钮打开本指南，第二个按钮打开自动化面配置弹窗。六个面的按钮各自在四种状态间循环：

| 颜色 | 状态 | 自动化行为 |
|------|------|------------|
| 灰色 | 无 | 该面不提供物品、流体或能量连接 |
| 蓝色 | 输入 | 可放入物品和流体，也可向机器输入 FE |
| 橙色 | 输出 | 可抽出产物，也可排空废液 |
| 紫色 | 输入输出 | 输入与输出都允许 |

新机器的六个面**默认全部为蓝色（输入）**。弹窗里的问号打开本指南，关闭按钮收起弹窗，齿轮按钮把所有面一次性设为灰色。
