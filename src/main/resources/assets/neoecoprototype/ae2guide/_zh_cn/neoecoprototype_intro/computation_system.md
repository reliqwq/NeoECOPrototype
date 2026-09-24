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

L1 计算系统是 eco 计算系统的前期版本：一个为 ME 网络提供并行合成线程的多方块合成 CPU 集群。

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

## 两件盈能强化成员

- **盈能强化计算机核心**（`energized_computation_core`）：每块 **+1024 并行**。它既能进并行核心列，
  也能顶掉一格外壳站位。站在外壳位上时，成型后它不再画自己的方块模型 —— 这与 eco 所有外壳位成员一致：
  成型的外壳是不可见的，也不再替邻居遮面，成员若继续画自己就会同时从内外两侧可见。那一格的表面由
  渲染通道补画回来。
- **盈能强化线程核心**（`energized_computation_threading_core`）：每块 **+16 合成线程**，而且**只接受
  离控制器最近的那一格**。放在别处那条线校验不过，整个结构直接不成形。线程是真的：eco 的合成 CPU 数组
  按这个数定长分配，所以这是处理器真正能跑的并发，不是面板数字。

## 怎么合成

| 物品 | 在哪合成 | 原料 |
| --- | --- | --- |
| 盈能强化计算机核心 | Neo ECO AE Extension 的集成工作站 | 3 x C4 可扩展计算系统主机、8 x 绿晶晶格、4 x 没那么神秘的方块，耗能 66,600 FE |
| 盈能强化线程核心 | L1 处理器装配室 | 2 x CM4A 线程核心、1 x C1 计算子系统结构外壳 |
| 盈能闪存增强（CE1R） | L1 处理器装配室 | 2 x CE1 闪存增强、1 x L4 存储组件 |

两只计算盘**只在物品栏贴图上有区分** —— CE1 保留 eco 的绿色信号，CE1R 换成我们的蓝色；
一旦插进驱动器，两者外观重新变得一致。

## L1 数值可以在服务端配置里改

`neoecoprototype-server.toml` 的 `l1_computation` 段只作用于 L1 一档，更高档沿用 eco 自己的数值。

| 配置项 | 默认 | 含义 |
| --- | --- | --- |
| `cpu_threads` | 2 | 一台普通 L1 子系统的合成线程数 |
| `cpu_accelerators` | 24 | 每块普通并行核心提供的并行数 |
| `cpu_total_bytes` | 1572864 | 该档标称的合成存储字节数，原 1 MiB 的 1.5 倍 |
| `energized_cell_total_bytes` | 5242880 | 每只盈能闪存增强（CE1R）的字节数，原 4 MiB 加 30% |

改完要重进世界。线程是在集群构造时分配的，所以已经站着的结构要拆掉重新成型才吃到新值。

