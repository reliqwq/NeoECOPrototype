---
navigation:
  title: L1 计算系统
  icon: neoecoprototype:simplify_computation_system
  position: 50
  parent: neoecoprototype_intro/index.md
item_ids:
  - neoecoprototype:simplify_computation_system
  - neoecoprototype:simplify_computation_casing
  - neoecoprototype:simplify_computation_parallel_core
  - neoecoprototype:simplify_computation_threading_core
  - neoecoprototype:simplify_computation_cooling_controller
  - neoecoprototype:simplify_computation_drive
  - neoecoprototype:simplify_computation_interface
  - neoecoprototype:simplify_computation_network_interface
---

# L1 计算系统

L1 计算系统提供 eco 计算系统的前期版本：可为ME 网络提供并行合成线程的合成多方块 CPU 结构。

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_min.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

## 所需组件

<ItemGrid>
  <ItemIcon id="neoecoprototype:simplify_computation_system" />
  <ItemIcon id="neoecoprototype:simplify_computation_casing" />
  <ItemIcon id="neoecoprototype:simplify_computation_parallel_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_threading_core" />
  <ItemIcon id="neoecoprototype:simplify_computation_cooling_controller" />
  <ItemIcon id="neoecoprototype:simplify_computation_drive" />
  <ItemIcon id="neoecoprototype:simplify_computation_interface" />
  <ItemIcon id="neoecoprototype:simplify_computation_network_interface" />
  <ItemIcon id="neoecoprototype:energized_computation_core" />
  <ItemIcon id="neoecoprototype:energized_computation_threading_core" />
  <ItemIcon id="neoecoprototype:energized_computation_cell_4m" />
</ItemGrid>


概念与 eco 计算系统一致：基础提供 **1 条合成线程**（eco L4 的 1/4），可用并行核心与线程核心按 eco 同款规则扩展。上图即最小可运行结构。
在最新版中，对并行核心与线程核心的能力进行增强，并行核心现在可提供 24 并行，线程核心提供 **2 条合成线程**。

## 两件盈能强化成员

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../scenes/l1_compute_energized.nbt" />
  <IsometricCamera yaw="45" pitch="30" />
</GameScene>

- **盈能强化计算机核心**（`energized_computation_core`）：每块 **+1024 并行**。最多在固定位置安装一个 —— 计算主机控制器
  正后方那一格外壳站位。
- **盈能强化线程核心**（`energized_computation_threading_core`）：每块 **+16 合成线程**，而且**只接受
  离控制器最近的那一格**。这个数值与 CM6A 线程核心一致。

## 怎么合成

| 物品 | 在哪合成 | 原料 |
| --- | --- | --- |
| 盈能强化计算机核心 | Neo ECO AE Extension 的集成工作站 | 3 x C4 可扩展计算系统主机、8 x 绿晶晶格、4 x 没那么神秘的方块，耗能 66,600 AE |
| 盈能强化线程核心 | L1 处理器装配室 | 2 x CM4A 线程核心、1 x C1 计算子系统结构外壳 |
| 盈能闪存增强（CE1R） | L1 处理器装配室 | 2 x CE1 闪存增强、1 x L4 存储组件 |


## L1 数值可以在服务端配置里改

`neoecoprototype-server.toml` 的 `l1_computation` 段只作用于 L1 一档，更高档沿用 eco 自己的数值。

| 配置项 | 默认 | 含义 |
| --- | --- | --- |
| `cpu_threads` | 2 | 一台普通 L1 子系统的合成线程数 |
| `cpu_accelerators` | 24 | 每块普通并行核心提供的并行数 |
| `cpu_total_bytes` | 1572864 | 该档标称的合成存储字节数，原 1 MiB 的 1.5 倍 |
| `energized_cell_total_bytes` | 5242880 | 每只盈能闪存增强（CE1R）的字节数，原 4 MiB 加 30% |

改完要重进世界。线程是在集群构造时分配的，所以已经存在的结构要拆掉重新成型才更新值。

