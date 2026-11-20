---
navigation:
  title: 通讯核心
  icon: neoecoprototype:communication_base
  position: 91
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:communication_base
  - neoecoprototype:communication_core
  - neoecoprototype:communication_range_module
  - neoecoprototype:communication_efficient_module
---

# 通讯核心

<Row>
  <BlockImage id="neoecoprototype:communication_base" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:communication_core" />
  <ItemIcon id="neoecoprototype:communication_range_module" />
  <ItemIcon id="neoecoprototype:communication_efficient_module" />
</ItemGrid>

无线家族的中期枢纽：一座 1xN 的塔，把**大量**无线链路集中在一台机器上。[链路卡](subsystem_channel_module.md)仍是早期点对点工具——两者共存：卡连接任意两台设备，塔则是一次建好、持续吞设备的枢纽。

## 建塔

**通讯核心基座**是控制器：右键一次看面板，**空手潜行右键**让塔自行升起（预览跟随建造长度档位，默认最高 **9 格**）。在基座上手工堆叠**通讯核心塔身**，之后可把任意一节换成模块——拆掉一节塔会散架，补回塔柱才重新成型。

| 方块 | 作用 | 每座上限 |
|------|------|----------|
| 基座 | 控制器；所有链接与计费都挂在它身上 | 1 |
| 塔身 | +4 台容量，+10 AE/t 待机 | 8 |
| 范围增幅模块 | 半径 +8 格 | 2 |
| 高效通讯模块 | 待机耗电 ×0.6 | 1 |

## 设备入网

用[无线链接卡](subsystem_channel_module.md)**潜行右键已成型**的塔完成标记，再逐台点设备——与卡对卡连接的手势完全相同，且标记在每次连接后保留，一张卡就能把一整个房间接到塔上。未成型的塔会回答"未成型"；半径外的设备会回答"距离过远"。

## 代价

成型后的塔从自身网络抽取**每格塔柱 10 AE/t**（高效通讯模块把这一数字乘以 0.6），每条在线链路在此之上照常按条计费。高度同时购买射程：塔的半径从 **16 格**起步，每根范围增幅模块 +8；全局 `wireless_max_range_blocks` 与增强卡只能加成，不会削减。

塔的出口与 [ECOP 网络子系统频道通讯核心](subsystem_channel_module.md)同款——六个虚拟密集端口，每个 32 频道——所以通常先耗尽的是频道而不是设备数。两座塔之间不能互连，正如两个频道核心不能互连。

<RecipeFor id="neoecoprototype:communication_base" />
<RecipeFor id="neoecoprototype:communication_core" />
<RecipeFor id="neoecoprototype:communication_range_module" />
<RecipeFor id="neoecoprototype:communication_efficient_module" />
