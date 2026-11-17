---
navigation:
  title: L1 供能接口
  icon: neoecoprototype:simplify_powered_me_interface
  position: 80
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_powered_me_interface
  - neoecoprototype:cable_powered_me_interface
---

# L1 供能接口

<Row>
  <BlockImage id="neoecoprototype:simplify_powered_me_interface" scale="3"></BlockImage>
</Row>

<ItemGrid>
  <ItemIcon id="neoecoprototype:cable_powered_me_interface" />
</ItemGrid>

**L1 供能接口**相当于槽位翻倍的<ItemLink id="ae2:interface" />，还会给网络供电：分区槽与库存槽都是 18 个（原版各 9 个），其余行为与原版接口完全一致，凡是接受 ME接口的地方都能用它。

通电且有频道时它输出 **200 AE/t**。AE2 每个网格只保留输出功率最高的那台被动发电源，所以最多生效一个。

方块版在 [L1 处理器装配室](processor_assembler.md) 组装。四台它合成一台[盈能超导接口](superconductive_interface.md)。

<RecipeFor id="neoecoprototype:simplify_powered_me_interface" />
<RecipeFor id="neoecoprototype:cable_powered_me_interface" />
