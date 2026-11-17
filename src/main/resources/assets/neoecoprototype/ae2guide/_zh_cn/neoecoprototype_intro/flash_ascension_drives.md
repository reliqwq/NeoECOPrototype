---
navigation:
  title: 闪速升格驱动
  icon: neoecoprototype:migration_drive_1_4
  position: 33
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:migration_drive_empty
  - neoecoprototype:migration_drive_1_4
  - neoecoprototype:migration_drive_4_6
  - neoecoprototype:migration_drive_6_9
  - neoecoprototype:migration_drive_creative
---

# 闪速升格驱动

闪速升格驱动可将已成型且达到满配边长的存储、计算或合成系统就地升档。手持对应驱动，对主机按住 Shift+右键。

| 驱动 | 升级范围 |
|---|---|
| <ItemImage id="neoecoprototype:migration_drive_1_4" scale="3" /> | L1 → L4 |
| <ItemImage id="neoecoprototype:migration_drive_4_6" scale="3" /> | L4 → L6 |
| <ItemImage id="neoecoprototype:migration_drive_6_9" scale="3" /> | L6 → L9 |
| <ItemImage id="neoecoprototype:migration_drive_creative" scale="3" /> | 创造模式驱动：L1、L4 或 L6 → L9 |

机器必须已成型且达到最大边长。档位不匹配、机器未满配或已经是 L9 时，操作会被拒绝且驱动不会消耗；升格成功后，驱动会被消耗。

驱动器中安装的存储矩阵和样板总线中的样板会带到新机器中；接口设置不会保留。默认不会返还被换下的方块。可在服务端配置中启用 `migration_drive.refund_replaced_blocks`，将它们装入备份驱动返还。此开关不会影响已安装的矩阵或已存储的样板。

空迁移驱动由 L1 结构扩容卡与 1k ME 存储组件合成。各档驱动在 eco 的大集成工作站中合成，每档需要 8 颗对应档位的计算并行核心、8 颗合成并行核心和 11 颗对应档位的 ECO 存储组件；从 4–6 档开始还必须投入上一档驱动：

- **1–4 驱动**：空迁移驱动 + L4 计算并行核心 ×8 + L4 合成并行核心 ×8 + 16M ECO 存储组件 ×11。
- **4–6 驱动**：1–4 驱动 + L6 计算并行核心 ×8 + L6 合成并行核心 ×8 + 64M ECO 存储组件 ×11。
- **6–9 驱动**：4–6 驱动 + L9 计算并行核心 ×8 + L9 合成并行核心 ×8 + 256M ECO 存储组件 ×11。
- **创造模式驱动**：1–4、4–6、6–9 驱动各一只。具体配方可在 JEI 查看。
