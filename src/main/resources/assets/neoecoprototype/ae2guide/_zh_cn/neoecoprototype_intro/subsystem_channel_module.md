---
navigation:
  title: ECOP 网络子系统频道通讯核心
  icon: neoecoprototype:subsystem_channel_module
  position: 100
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:subsystem_channel_module
  - neoecoprototype:wireless_link_card
  - neoecoprototype:wireless_unlink_card
  - neoecoprototype:l1_signal_booster_card
---

# ECOP 网络子系统频道通讯核心

<Row>
  <BlockImage id="neoecoprototype:subsystem_channel_module" scale="4"></BlockImage>
</Row>

## 简介

ECOP 提供方便快捷的无线方案。将 **ECOP 网络子系统频道通讯核心**（简称“无线通讯核心”）接入 ME 控制器网络，再用 <ItemLink id="neoecoprototype:wireless_link_card" /> 绑定设备，即可获得相当于 **6 个 ME 控制器单面的频道数**的无线出口（当前共 **192 频道**）。

## 怎么搭

1. 将无线通讯核心贴着 ME 控制器或它的密集线缆放置，接入控制器网络。
2. 手持 <ItemLink id="neoecoprototype:wireless_link_card" />，Shift+右键无线通讯核心进行标记。
3. 再 Shift+右键任意网络设备，建立无线连接。

## 界面查看

右键无线通讯核心打开面板：

| 列 | 用途 |
|----|------|
| 左列（选节点） | 选择一个无线出口，查看该出口当前承载网络的频道占用情况。 |
| 中列（看总览） | 查看整台核心的频道使用情况、剩余总数和所在维度。 |
| 右列（精确控制） | 高亮所选接入点与远端设备、刷新频道状态，或断开选中的设备。 |

## 必读规则

- **放卡自动连。** 放置网络设备时，若快捷栏里有已标记的链路复用连接卡，设备节点建好后会自动连接到卡片标记的出口。安装 Curios 后，卡片放在饰品栏位也会触发自动连接。
- **距离限制。** 默认 50 格。无线通讯核心右侧升级槽可放 <ItemLink id="neoecoprototype:l1_signal_booster_card" />：每张使范围翻倍，依次为 100、200、400 格；放满 4 张后不再限制距离。调整范围不会断开已经建立的链路。
- **耗电。** 默认每条在线链路消耗 50 AE/t，由连接后的网络承担。
- **会连成环的链路会被拒绝。** AE2 建立链路时会合并网络；若两端已在同一张网，闭环链路不会建立。