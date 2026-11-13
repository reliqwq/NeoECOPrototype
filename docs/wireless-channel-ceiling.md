# AE2 的频道上限写在哪只节点上，三家上游各自怎么绕过它

> 日期：2026-11-13。基线：AE2 19.2.17（编译与运行同一颗）／eco 21.2.17／MC 1.21.1。
> 起因：无线链路"接了 11 台只吃上 8 个频道"，以及随后那句"模块为什么不能给更多频道——
> 数据能源的均分塔、水晶科技的末影发信器、EAE Plus 不是都做到了吗"。
>
> **结论：频道上限只写在 `GridNode` 上，`GridConnection` 那个 32 是死数（全 jar 0 个调用点）。**
> 所以"一条边给超过 32 频道"只有一条路：抬落点节点，而 AE2 没有 setter ⇒ 必须 mixin（AE2CS 就这么干的）。
> "总量给超过 32"则有第二条路，不用碰上游：**多开几条边，每条落在各自的宽节点上**（EAE Plus 的加边、
> 以及 DE 那套"控制器面数 → 预算"的算术可以搬过来当端口数）。
> 三家参考各占一格：AE2CS **抬上限**（mixin 打进 `appeng.me.GridNode`），EAE Plus **加边**（一次都没抬过容量），
> Data Energistics **自己按"控制器 6 面 × 每面 32"建模一份预算**，把额度花在自己的虚拟频道上，AE2 的算术一行没动。

---

## 1. AE2 19.2.17 自己的算术（四条现量）

| 读数点 | 字节码给的规则 |
|---|---|
| `appeng.me.GridNode#getMaxChannels` | `CANNOT_CARRY` → 0；`ChannelMode.INFINITE` → `Integer.MAX_VALUE`；否则 `(DENSE_CAPACITY ? 32 : 8) × cableCapacityFactor` |
| `appeng.me.GridConnection#getMaxChannels` | `INFINITE` → MAX，否则 `32 × cableCapacityFactor`，**与两端节点的 flag 无关**。全 jar **0 个调用点** ⇒ 这个数不参与任何拒绝 |
| `appeng.me.GridConnection#hasFlag` | `iconst_0; ireturn` —— 边永远不报 `REQUIRE_CHANNEL`，因此永远进不了拒绝分支 |
| `appeng.me.pathfinding.PathingCalculation#tryUseChannel(GridNode)` | 唯一真正做拒绝的地方：沿 `getHighestSimilarAncestor()` 往上逐层比 `channelBottlenecks(layer) >= layer.getMaxChannels()`，任一层到了它自己的上限就整条拒 |

`ChannelMode` 的因子表（`javap -c` 读 `<clinit>` 的常量，`(adHocNetworkChannels, cableCapacityFactor)`）：

| 模式 | adHoc | factor | 普通节点 | 密线节点 |
|---|---:|---:|---:|---:|
| `INFINITE` | MAX | 0 | MAX | MAX |
| `DEFAULT` | 8 | 1 | **8** | **32** |
| `X2` | 16 | 2 | 16 | 64 |
| `X3` | 24 | 3 | 24 | 96 |
| `X4` | 32 | 4 | 32 | 128 |

三条口径：

1. **AE2 里没有"全网络频道池"这个东西。** 全 jar `setMaxChannels` / `setChannelCapacity` /
   `setCustomMaxChannels` **0 处点名**（同一把尺在 `getUsedChannels` 上命中 13 处，证明尺子在走）；
   `IPathingService` 只有 `isNetworkBooting / getControllerState / repath / getChannelMode / getUsedChannels`，
   0 个 setter；`IManagedGridNode` 只有 `setFlags(...)`。**抬上限只有一条路：mixin 打进 `appeng.me.GridNode`。**
2. **控制器不是频道源。** `ControllerBlockEntity` 构造子（offset 33-52）只登记**一只**主节点，
   flags = `{CANNOT_CARRY, DENSE_CAPACITY}` ⇒ `getMaxChannels()` 第一条分支就答 **0**。
   上面那条算术里**没有任何"面数"这一项**——"控制器的空闲面 = 可以求和的频道池"这个前提
   在字节码层面不成立。旁证很硬：DE 自己算这份预算时走的是
   `supplyFromControllerNode(node, mode)`——先读 `node.getMaxChannels()`，**`<= 0` 就退回
   `32 × factor`**，再乘它自己的 `FACES_PER_CONTROLLER = 6`。
   也就是说 **DE 是被 AE2 答了 0 之后才自己造出"6 面 × 32"这个数的**，不是从 AE2 读到的。
3. **祖先链是压缩过的。** `highestSimilarAncestor` 只在 `subtreeMaxChannels` 变化时换桶，
   所以一段等宽的路径只算一个瓶颈、宽度一变就分桶。"链到第 9 台就掉线"是这条规则的形状，不是 bug。

### 1.1 没有控制器时走的是另一条分支，那条上 8 是"台数"不是"宽度"

`PathingService.onServerEndTick` 的 `NO_CONTROLLER` 分支不调 `PathingCalculation`，
而是 `calculateAdHocChannels()`（offset 164-185，现量）：

```
count = 需要频道的节点数（MULTIBLOCK/COMPRESSED 先折叠）
count > ChannelMode.getAdHocNetworkChannels()  ->  AdHocNetworkError.TOO_MANY_CHANNELS, 返回 0
否则                                             ->  返回 count
```

然后 `AdHocChannelUpdater(count)` 把**每个节点和每条边**的 `usedChannels` 一律设成这个数。
⇒ 无控制器网络的天花板是**"吃频道的设备台数"和 `adHocNetworkChannels` 比大小**
（`DEFAULT` 8、`X2` 16、`X3` 24、`X4` 32），不是每只节点的 8/32 宽度；
而且**超了不是第 9 台离线，是整网 0 频道**。
本节上面所有关于 `DENSE_CAPACITY`、落点宽度、端口分桶的话，**都只适用于 `CONTROLLER_ONLINE` 那条分支**。

### 1.2 实机读数（2026-11-13，dev 客户端，同一存档）

一只模块上 4 条链路，`sweep` 重建时仪器行原文：

```
edge=virtual max=32 used=0  a(max=8,used=1)  b(max=32,used=1)  sameGrid=true  mode=DEFAULT  controller=NO_CONTROLLER  gridUsed=8
edge=virtual max=32 used=0  a(max=8,used=8)  b(max=32,used=1)  sameGrid=true  mode=DEFAULT  controller=NO_CONTROLLER  gridUsed=8
wireless link: 4 link(s) charged at 0.5 AE/t each, 200.0 AE taken over the last 100 ticks, 0 could not be paid
wireless link card: 子系统频道通讯模块@… already carries 4 links (max 4, from wireless_max_links_per_device in this world's config)
```

三件事当场定了：

1. **`controller=NO_CONTROLLER`** ⇒ 这张测试网络走的是 1.1 那条分支，天花板是 8 **台**，
   跟落点宽度无关——模块换上去当然看不见效果，因为那条路上根本没有"宽度"这个量。
2. **四条边全部 `used=0`、对面 `max=8`** ⇒ 在这个存档里无线边没有在扛任何频道；
   远端那 8 个频道是从实体线缆走的。这条不是"边不计数所以没事"，而是**它确实没被用到**。
3. **`200.0 AE / 100 tick / 4 条 / 0.5 AE·t⁻¹`** 对得上，账单面没有漂移。

⇒ 想在实机看见 A/D 的价值，先给这张网络放控制器；否则读到的永远是 ad-hoc 的 8 台上限。

**同一天、同一存档、同一只模块（`-66,-59,15`），放了控制器之后重取：**

```
linked L1 样板供应器@-70,-60,25 <-> 模块 | edge=virtual max=32 used=0 a(max=8,used=1)  b(max=32,used=10) mode=DEFAULT controller=CONTROLLER_ONLINE gridUsed=11
linked L1 样板供应器@-69,-60,26 <-> 模块 | edge=virtual max=32 used=0 a(max=8,used=1)  b(max=32,used=11) mode=DEFAULT controller=CONTROLLER_ONLINE gridUsed=12
linked 猪咪装配室@-72,-60,14   <-> 模块 | edge=virtual max=32 used=0 a(max=8,used=8)  b(max=32,used=2)  mode=DEFAULT controller=CONTROLLER_ONLINE gridUsed=3
```

四件事一次定死：

1. **`gridUsed` 从 8 跳到 11、12**，`b(max=32,used=11)` —— 模块那只节点实机扛到 11 条，
   **8 那堵墙随控制器消失**。这直接证实 1.1 的诊断：先前读到的 8 是 ad-hoc 台数上限，不是宽度。
2. **`a(max=8,used=8)`**（猪咪装配室那一路）—— 第 2 节那堵"落点宽度"的墙在实机里也看见了：
   那一路被装配室自己那只 8 档节点卡住，跟模块的 32 无关。**这才是 D 要解的东西。**
3. **`edge=… used=0` 在四条边上都是 0，同时 `b` 报 11** —— 这不是"链路空闲"。
   `GridConnection.usedChannels` 只由 `propagateChannelsUpwards()` 在**控制器路径上的那条连接**里填，
   虚拟边基本不填。所以仪器行的 `a/b` 两个数才是能看的，`edge=` 那两个不能单独读；
   套件里也只有 `edge=virtual` 这个**身份**被断言过，`max=/used=` 没有任何守卫依赖。
4. **当时真正卡住加线的是 `at_limit`**：`already carries 4 links (max 4, from wireless_max_links_per_device)` —— 这台存档的配置是 4，
   当时出厂值是 8。这条后来被 §5.0 那次改动取代：默认改 0，拒人交给落点读数。
   另外两条守卫也在同一分钟里活过：`wired_already`（"already on one network of 127 nodes"）拒了 2 次、
   `already linked` 拒了 1 次；分组在无链路时 apply 报 `0 link(s) moved`，解绑 `dropped=4` 之后账单归 0。

### 1.3 退出面：7 条 AEKey 报错，一条都不是无线的

客户端 23:34 自己退出（exit 0，跑前跑后都只有 1 颗 daemon java）。日志里 `ERROR/FATAL` 带相关字样的 7 条
**全部是 AE2 在反序列化存档里的 AEKey**，没有一条来自无线：

| 条数 | 内容 | 归属 |
|---:|---|---|
| 2 | `Unknown registry key ... neoecoprototype:music` | **我们的**——音乐 keytype 只在 `MusicDataEnergisticsPlugin` 里注册，走 DE 的注解扫描；2026-11-12 把 DE 从 dev 运行期摘掉后这个 keytype 就不注册了，而这个测试存档早先存过音乐 key |
| 2 | `No component with type: 'ae2_pattern_disk:disk_contents'` | 别人的 mod 留下的 key，那颗 jar 现在没装 |
| 3 | `Unknown registry key ... extendedae_plus:wireless_transceiver` / `labeled_wireless_transceiver` | 同上 |

三点结论，写下来免得下次再查一遍：

1. **这不是 bug，是"摘依赖"的既有代价**，而且 5/7 条来自别人的 mod —— 同一条机制我们只是站在镜像两侧。
2. **玩家侧走不到**：音乐族 1.4.10 起整族隐藏（创造栏、3 条配方出 jar、JEI 页、红字），
   唯一还留在 jar 里的 `recipe/data_reassembler/data_flow_from_music_data.json` 带
   `"neoforge:conditions": [{ "type": "neoforge:mod_loaded", "modid": "data_energistics" }]`，
   没装 DE 时整条跳过 ⇒ **拿不到音乐 key，也就存不出这个 key**。
   （我第一遍把这 2 行读成"我们的 ERROR=0"被打了脸：那次只数了 `[neoecoprototype/]` 这个 logger 的行，
   口径要说清 —— AE2 用**它自己的** logger 报我们的 key。）
3. **所以不要为了消这 2 行开新存档**：这一档是 §1.2 那组对照（8→12）和 `a(max=8,used=8)` 那台卡满落点的
   唯一来源，#129 的验收就是看这些数变不变。该开干净新档的时机是 D 实现**之后**，
   那时要验的是"无 DE / 无 pattern disk / 无 EAEP 能不能启动进世界"，那是控制组，不是读数环境。

---

## 2. 我们自己量到的是什么（GameTest）

守卫 `aWirelessEndpointNodeIsTheChannelBottleneck`（batch `wireless_endpoint_capacity`，模板 `empty`）：
两组各 12 台机器、各一条无线边的对照组，唯一差别是边落在什么节点上。日志前缀
`wireless endpoint capacity: gridUsed=… charged=…/12 devices=[i=max/used,…]`。

| 落点 | 落点节点 `getMaxChannels()` | `getUsedChannels()` | 真吃到频道的设备 |
|---|---:|---:|---:|
| 普通接口 | 8 | 9 | **8 / 12** |
| 两端都是子系统频道通讯模块 | 32 | 14 | **12 / 12** |

同一条无线边，把落点从 8 换成 32，就从"掉 4 台"变成"一台不丢"。
⇒ 玩家报的"多出的离线"**不是链路的属性，是货那边那只节点的宽度**。

**D 的前提探针（batch `wireless_port_probe`，2026-11-13，随 289 条套件全绿）**
守卫 `threePortsOnOneControllerEachCarryTheirOwnChannels`：一只控制器 + **三只**虚拟密线节点
（`setInWorldNode(false)`、只给 `DENSE_CAPACITY`、`idle 0`），每只直接
`GridHelper.createConnection(controllerNode, portNode)`，然后每只端口各带一个**独立的 12 台远端网**
（三个远端群放在 y=1/3/5，故意不互相贴着，否则"每端口 32"会退化成一个网络的 32）。

| 断言 | 读数 |
|---|---|
| 需求必须超过单只节点的 32 | 3 × 12 = **36 台**，超了 |
| 36 台全部吃到频道 | 绿（共享一桶只会给到 32） |
| 拆掉三只端口之后频道归零 | 绿（`charged == 0`，没有留在网络里的幽灵频道） |

⇒ "每只挂在控制器节点上的密线端口自占一个 32 桶"从推演变成读数，D 可以往下写实现；
而且这条探针不经过 `WirelessLinks`（端口和边都在测试里直接用 AE2 公开 API 摆），
所以它测的纯粹是 AE2 的分配行为，不是我们自己的绑定代码。

---

## 3. 三家参考各自的做法（全部字节码现量）

### 3.1 EAE Plus 1.6.3 —— 不抬上限，改的是边的拓扑

- `content/wireless/WirelessTransceiverBlockEntity` 构造子：flags 数组**只有** `DENSE_CAPACITY`
  （没有 `REQUIRE_CHANNEL`）、`setInWorldNode(true)`、`setExposedOnSides(allOf(Direction))` ⇒ 它就是一只 32 档节点。
- `ae/wireless/WirelessSlaveLink.updateStatus()` offset 282-329：先看 `findExistingConnection`，
  没有就 `GridHelper.createConnection(hostNode, masterNode)` —— **每张卡一条独立边**，
  并把 `IllegalStateException`（AE2 对"这对节点已经连过"的答复）三段全包进异常表。
- `items/materials/ChannelCardItem extends UpgradeCardItem`（NBT：channel / ownerUUID / teamName），
  插进接口/样板供应器/IO 与存储母线；宿主由一批 `ChannelCard*Mixin` 提供。
- 全 jar `setMaxChannels` / `customMaxChannels` / `CustomChannelProvider` **0 命中**。

⇒ 它一次都没抬过任何节点的宽度。它的"多"是**每台设备自己一条边、落点永远是那只密线收发器**，
于是没有任何一只 8 档节点替别人挡路。它的天花板还是 32——想更多就再摆一台收发器。

### 3.2 AE2CS 1.3.0 —— 真抬上限

- `mixin/GridNodeMixin`：`@Mixin(appeng.me.GridNode.class, remap = false)`，
  `@Inject(method = "getMaxChannels", at = @At("HEAD"), cancellable = true)`；体只有四步——
  `getOwner() instanceof CustomChannelProviderHost` 且 `isEnabledCustomChannel()` ⇒
  `cir.setReturnValue(host.getMaxChannels())`。
- `api/CustomChannelProviderHost`：`getMaxChannels / setMaxChannels / isEnabledCustomChannel /
  setEnabledCustomChannel` 四条。
- 上限的数：`computeSenderVirtualNodeTarget()` = `min( max(0, 32 × factor) × max(1, 六面里贴着控制器的方块数), 768 )`。
  ⇒ **DEFAULT 下最多 32×6 = 192；768 只有 `X4`（factor 4）且六面贴满才够到。** 这个"面数"是 AE2CS
  自己定的**计价规则**，不是 AE2 给控制器的容量——第 1 节已经量过控制器节点报 0。
- 额度是拿本地频道买的：`ensureSenderVirtualNodes()` 造 target 只虚拟节点
  （`setInWorldNode(false)`、`setIdlePowerUsage(10.0)`、`setFlags(REQUIRE_CHANNEL)`、各自一个 `setTagName`），
  每只 `GridHelper.createConnection` 挂到主节点；`succeedVirtualSenderNodes`（`isOnline()` 的只数）
  就是 `getCouldSendChannels()` 报给频带的数。
- 收货侧：`BroadcastFrequencyBand.applyReceiver(...)` 先 `setEnabledCustomChannel(true)` +
  `setMaxChannels(allocated)`，再把接收节点**直接连到 `controllerNode`**（`AECSGridHelper.getControlNode(bindGrid)`），
  分配额来自 `computeAllocChannels(usable, …)` = `min(usable, requested)`。

⇒ 形状 = ①mixin 抬落点节点 ②N 只虚拟 `REQUIRE_CHANNEL` 节点把额度真的占住 ③边直接搭到控制器节点。

### 3.3 Data Energistics 3.4.0 均分塔 —— 把"控制器的面"**自己算成一份预算**，再套在自己的资源上

这条最值得记，因为它正是"拿走控制器的面求和"那个思路的**成熟实现**，而且**没有 mixin、没有虚拟节点**：

- `integration/ae/ae2/grid/ControllerChannelCapacity`（`TowerChannelCapacity` 的现量实现）
  两个常量：`FACES_PER_CONTROLLER = 6`、`CHANNELS_PER_CONTROLLER_FACE = 32`，
  `nativeControllerSupply(mode) = 6 × (32 × factor)` ⇒ **DEFAULT 下一只控制器方块 = 192 频道**。
  （`Math.multiplyExact` 全程防溢出。）
- `supplyFromControllerNode(node, mode)`：读 `node.getMaxChannels()`，**`<= 0` 就退回 `32 × factor`**。
  这就是第 1 节那条读数的直接后果：控制器节点带 `CANNOT_CARRY`、答 0，
  所以 DE 不是"从 AE2 读到每面 32"，而是**自己按 6 面 × 32 建模**，AE2 那边根本没有这个数。
- 分支现量（`calculate(ControllerState, ChannelMode, LongCollection)`）：
  `CONTROLLER_CONFLICT` → 0；`ChannelMode.INFINITE` → 无限；`NO_CONTROLLER` 或控制器位置表空
  → `controllerlessCapacity = 32 × factor`；否则 → `控制器方块数 × 6 × (32 × factor)`。
- 这份预算喂给 `tower/virtual/FifoVirtualChannelLedger`：`setTotalCapacity(...)` +
  `setPhysicalChannelUsage(long)`（把 AE2 **真的用掉的**频道减掉），然后按 FIFO 给每个绑定目标
  分"虚拟频道"。虚拟频道管的是**塔自己的分配**，不是 AE2 的 pathing。

⇒ DE 没有把 AE2 的 32 变大，也没有骗过 pathing。它做的是：**用同一条"控制器面"的算术算出一个额度，
扣掉 AE2 的物理占用，然后把这个额度花在自己的资源上**。AE2 的频道语义一点没动。

---

### 3.4 四条路线的共同形状

| 参考 | 有没有动 AE2 的算术 | "更多"是靠什么 |
|---|---|---|
| EAE Plus | 没有 | 每台设备一条自己的边，落点是宽节点 |
| AE2CS | **有**（mixin `GridNode.getMaxChannels`） | 单只节点报 `32 × factor × 相邻控制器数`，并用 N 只虚拟 `REQUIRE_CHANNEL` 节点真的占住 |
| DE 均分塔 | 没有 | 读 AE2 的 `ChannelMode` 与 `node.getMaxChannels()`，按 `6 × 32 × factor × 控制器方块数` **算出自己的预算**，花在自己的虚拟频道台账上 |
| （我们） | 没有 | 一只密线模块 = 一条 32 档边 |

---

## 4. 宝贵经验（下次碰到"别人已经做到了"照这几步判）

1. **先问"它的『多』落在哪本账上"。** 三把尺各扫一遍，30 秒能省一整轮猜：
   ```
   grep -rl "setMaxChannels\|CustomChannelProvider" <解包目录>   # 命中 = 它改的是 AE2 那本账（要 mixin）
   grep -rl "GridHelper.createConnection"           <解包目录>   # 命中 = 加边路线
   grep -rl "ChannelMode\|getMaxChannels"           <解包目录>   # 命中但前一把尺空 = 它只【读】AE2 的数，自己记自己的账
   ```
   三家各占一格：AE2CS 第一把尺命中（`mixin/GridNodeMixin`），EAE Plus 只有第二把尺，
   DE 前两把都空、第三把命中（`ControllerChannelCapacity` + 自己的 `VirtualChannelLedger`）。
   **只有第一把尺会真的改变 AE2 的分配；第三把尺改变的是那个模组自己的资源。**
   这条区别是关键：DE 的"更多"是"我的塔能同时服务更多目标"，不是"AE2 允许更多频道穿过你那根线"。
2. **要抬只有一处可抬。** 拒绝发生在 `GridNode.getMaxChannels()`；抬 `GridConnection.getMaxChannels()`
   是白抬（0 调用点）。而 AE2 自己没给 setter ⇒ 一定是打进 `appeng.me.GridNode` 的 mixin，
   这是热路径（每次 repath、每个节点都被问），按 §8 要在类头写清去留判据：
   **AE2 哪天给这个数开了 setter 或注册口，就撤。**
3. **加边要配宽落点。** 每台设备一条独立边没用，如果它们仍串在同一只普通接口后面——
   那只接口报 8，整条支线就还是 8（第 2 节左列就是这个形状）。
4. **"面数求和"是上游的价，不是上游的容量。** AE2CS 的 `32 × 相邻控制器数` 后面跟着
   "一只虚拟节点 = 一条本地 REQUIRE_CHANNEL + 10 AE/t 空闲"。我们若跟，必须同时跟价，
   否则是凭空造容量。
5. **别拿 `getUsedChannels()` 当"几台吃上电"。** 它是整网数（我们那组里 14 = 12 台 + 两只模块自己）；
   判掉线要逐台读 `node.getUsedChannels() > 0`（守卫里的 `chargedDevices` / `chargedTable`）。
6. **频道行为只有 GameTest 有面。** 三条读数口子是 `getControllerState()`、`getUsedChannels()`、
   `node.getMaxChannels()`；客户端眼睛看不见拒绝，套件看不见渲染，两者都要各自的读数。
7. **先读 `controller=`，再谈频道。** 我们差点把一条 ad-hoc 的 8 台上限当成落点宽度的 8 来修（第 1.2 节）。
   `CONTROLLER_CONFLICT` 又是第三件事：它走物理控制器连通性，虚拟边不会把两个控制器结构并成一组
   （`twoOnlineControllerPilesStayOnlineAcrossAWirelessEdge`）。三条分支、三种"8"，
   分开量才有解；`status` 仪器行里 `mode=` 与 `controller=` 两个字段就是用来区分它们的。
8. **借思路之前先量它的许可证，而且要量发布件里那一份，不是记忆里的。** DE 的
   `META-INF/neoforge.mods.toml` 写的是 `AGPL-3.0-or-later (code); All Rights Reserved (resources/assets)`，
   并且代码与资源**分别适用**（它自己 `LICENSE.RESOURCE` 第 4 条）。所以本文档抄的是一个 AGPL 上游的建模思路，
   这条线要写死：**读字节码看形状可以，把它的代码或资源搬进 `src/` 不行** —— 一搬，我们整份产物就得按
   AGPL-3.0-or-later（含 §13 远程服务条款）来传，而我们是 `GPL-3.0-only`，接不住。
   我们现在与 DE 的实际链接面已经量过并记进 `THIRD_PARTY_NOTICES.md`：发布 jar 里**只有 1 只 class** 点名
   `com/fish_dan_`（`integration/dataenergistics/MusicDataEnergisticsPlugin`，implements 它公开的
   `api.entrypoint.DataEnergisticsPlugin`），引用全在它的 `api/` 包内、不碰 `common/**`，
   jar 里 0 个 `data_energistics` 命名空间条目、0 个依赖 jar。

---

## 5. 我们现在停在哪，往前四条路

模块当前 = 一只 `REQUIRE_CHANNEL | DENSE_CAPACITY` 节点 ⇒ 32；两端各一只模块已经实测 12/12。

### 5.0 2026-11-13 已经把数字闸换成读数闸

`wireless_max_links_per_device` 的出厂值从 **8 改成 0**（范围也从 `1~32` 变成 `0~32`），语义降级成
"整合包作者额外想要的天花板"；`0` 时**不做任何账面判断**。取而代之拒人的是两条 AE2 自己的数：

| 闸 | 什么时候拒 | 拒的时候说什么 |
|---|---|---|
| `WirelessLinks.fullLanding` | `CONTROLLER_ONLINE` 且落点节点 `getUsedChannels() >= getMaxChannels()`（`max <= 0` 的 `CANNOT_CARRY` 节点不算"满"，否则碰到控制器就全拒） | `endpoint_full`：这一格已经把它能承载的 N 个频道全承载了，再接一条拿不到频道 |
| `WirelessLinks.starvationRisk`（原有） | 两张**都无控制器**的网合并后 ad-hoc 需求 > 较小天花板 | `starvation`：合起来会 0 频道、每台都显示离线 |

读数闸**故意只在受控网络上成立**：无控制器时 `AdHocChannelUpdater` 把同一个全网数写进每个节点，
那时 `used` 不是这一格的用量，照它拒会把所有普通设备一律判成满。

夹具 `aFullLandingSquareIsRefusedByItsOwnChannelWidth`（batch `wireless_landing_headroom`，
**单独占一个批次**：`empty` 模板只有 3 格宽，同批并排放两条八格半径的夹具会让两张网并成一张，
读数变成 `controller=CONTROLLER_CONFLICT wirelessLinks=2`——那是夹具撞车不是频道结论）：

- 断言：控制器网络上 8 台机器挂在一只普通接口后面 ⇒ 该接口 `8/8`；从它接过去被 `endpoint_full` 拒，
  且拒的话里**不许**出现账面措辞；同一对网络改从密线模块接过去必须成功（控制）。
- 控制组跑过：把 `fullLanding` 短路成 `return null` ⇒ **290 条里正好红这一条**，其余 289 绿。
- 恢复后 290/290 绿，JUnit 全绿。

**文案这一半不靠"它绿了"过关**（同日补）：`wireless_reply_keys` 那条守卫只把 `en_us` 渲染回来跟日志句子
对字节的，是**手点得到的四条**（档位答复 + `same_block` + `not_a_device` + 解绑）；两条闸都在 `bindReply` 里，
要点两下真方块、还要配置与第二张网才走得到，所以当年没人替它们对过。结果我照着"意思一样"写了 `endpoint_full`
的 `en_us`（"is already carrying all %3$s of the channels it can carry (%2$s/%3$s)"），而 Java 那边是
"is already carrying %2$s of the %3$s channels it can carry"——**套件全绿、两句不同**。落法：

- 两句都以 Java 那句为准（`at_limit` 也把它多出来的那句"设成 0 剩下的唯一天花板是这一格自己的宽度"补进两份语言）；
- 新增 `cardLineDriftedFromLog(helper, reply)`，由**已经触发到这条拒话的两条夹具各自比对一次**
  （`en_us` 用 reply 自己的参数渲染出来 == `english()`），红的时候把 log / rendered / 原始 value 三段一起打出来；
- 控制组：故意把 `en_us` 的 `endpoint_full` 改回旧那句（少一个 `%2$s`）⇒ **290 条里正好红 `afulllandingsquare…`**，
  报的就是 `log=[… 8 of the 8 …] rendered=[… all 8 …]`。反向替换回去后 290/290 绿。
- 顺带量到并修掉的：先前那批加键的脚本把两份语言文件的**结尾换行**写掉了（`git diff` 报
  `\ No newline at end of file`），换行补回来了；`en_us` 仍是纯 LF、`zh_cn` 纯 CRLF、各 382 键、JSON 都能解析。

教训一句话：**"这条文案在守卫范围内"要按"夹具能不能触发到它"判，不是按"守卫名字里有没有 reply"判。**
同一条规矩在 §4 已经为频道写过一次（别拿"套件绿了"当功能被验过），这里是它第二次在文案上收账。

| 路线 | 内容 | 代价 |
|---|---|---|
| **A 维持** | 一只模块 = 一根密线 = 32；要更多就再摆一对模块（= 再一条边） | **0 项**，已完成、已绿（12/12） |
| **B 抬上限（照 AE2CS 的形状，代码自己写）** | 1 条 `GridNode.getMaxChannels` 的 HEAD 可取消注入 + 我们自己的 host 接口（四条方法，别叫 `CustomChannelProviderHost`，避免与 ae2cs 撞名）+ 模块按"六面里贴着控制器的方块数"报 `min(32 × factor × n, 上限)` + N 只虚拟 `REQUIRE_CHANNEL` 节点把额度占住 + **收货侧也要一份配额账**（否则对面那只普通节点还是 32）+ 配置项 + 1 条 GameTest + `mixinListMatchesShippedMixinClasses` 同步 | **≈7 项**；风险集中在第一条：AE2 最热路径、我们的第 8 个注入点，跟版要重量 `getMaxChannels` 的形状。**只有 B 能让"一条边喂一个 96 台的远端网"** |
| **C 加边（照 EAE Plus 的形状）** | 不动任何上限，把每台远端设备各挂一条边这条路走宽。**2026-11-13 之后这条的前半已经做掉了**（§5.0：默认 0 + 文案改口径），剩下的只是"要不要继续把边数往开放方向走" | 已并入 §5.0；天花板仍是 32／只模块 |
| **D 分面开端口（读 DE 的算术，不用它的账本）** | 模块按贴着的控制器方块数造 N 只虚拟节点（`setInWorldNode(false)` + `REQUIRE_CHANNEL | DENSE_CAPACITY`），**每只直接 `createConnection` 到控制器节点**（控制器节点 `CANNOT_CARRY` ⇒ 按 `setControllerRoute` 的分支，它是 BFS 的根、不进任何 `channelBottlenecks` 桶），于是一只方块上有 N 个**互相独立**的 32 档落点，无线边一条落一只端口。"边数 > 端口数"已经被 §5.0 的读数闸接住：端口满 = `endpoint_full` 拒，不需要再发明一条规则 | **≈4 项**（前提探针已经绿了，见第 2 节末），**零 mixin、全公开 API**；但 N 条边要 N 个对面落点（对面也得 N 只密线口），所以它是"一只模块服务 N 个远端网 × 32"，不是"一个远端网 × 32N" |

判断依据只有两条，别混着问：

- **要不要"一条边喂一个超过 32 台的远端网"？** 只有 B 办得到（对面那只节点的上限必须被抬，而 AE2 没有口子）。
- **要不要"一只模块服务多个远端网、总量超 32"？** D 就够，且不碰上游。

D 的算式（`6 面 × 32 × factor`）**不是 AE2 给的数，也不是从 DE 拿的代码**——AE2 那边控制器节点答 0（第 1 节第 2 条），
这个式子是 DE 自己公开的建模思路，我们照同一个思路从 AE2 的公开常数（密线 32、立方 6 面）自己推、自己写。
javadoc 里必须把这句话说全，否则下一个人会以为这个数能从 AE2 读到。

D 的两条依据都是现量、不是推想：

- **分桶独立**：`PathingCalculation` 构造子对控制器每条边做 `connection.setControllerRoute(controllerNode)`，
  于是贴着控制器的第一层节点在 `GridNode.setControllerRoute`（offset 15-57）里命中
  `owner instanceof ControllerBlockEntity` 那支 ⇒ `highestSimilarAncestor = null`，每只端口自己是一个
  `channelBottlenecks` 桶的根；`tryUseChannel` 往上走只走到这个根为止。
- **API 可行**：AE2CS 的 `BroadcastFrequencyBand.applyReceiver` offset 318-331 就是
  `GridHelper.createConnection(controllerNode, receiverNode)`——把一只外来节点直接挂到控制器节点上，
  这颗调用已经在发布件里跑着。

---

## 6. 复现命令

```bash
J=/f/java/bin/javap.exe
AE2=/f/neoecoSimplify/libs/ae2-19.2.17.jar

# 参考 jar 先解出来（仓库不分发 jar，见 THIRD_PARTY_NOTICES.md 的分发规则）
mkdir -p /c/tmp/ae2 /c/tmp/ae2cs /c/tmp/eaep /c/tmp/de
(cd /c/tmp/ae2   && /f/java/bin/jar xf "$AE2")
(cd /c/tmp/ae2cs && /f/java/bin/jar xf "/f/neoecoSimplify/参考模组/ae2cs-1.21.1-1.3.0+alpah.jar")
(cd /c/tmp/eaep  && /f/java/bin/jar xf "/f/neoecoSimplify/参考模组/extendedae_plus-1.6.3.jar")
(cd /c/tmp/de    && /f/java/bin/jar xf "/f/neoecoSimplify/libs/data_energistics-1.21.1-3.4.0.jar")

# 1) 上限的算术
$J -p -c -cp $AE2 appeng.me.GridNode       | sed -n '/public int getMaxChannels/,/ireturn/p'
$J -p -c -cp $AE2 appeng.me.GridConnection | sed -n '/public int getMaxChannels/,/imul/p'
$J -p -c -cp $AE2 appeng.me.GridConnection | sed -n '/public boolean hasFlag/,/ireturn/p'      # iconst_0; ireturn
$J -p -c -cp $AE2 appeng.me.pathfinding.PathingCalculation \
   | sed -n '/private boolean tryUseChannel/,/^  private void propagateAssignments/p'
$J -p -c -cp $AE2 appeng.api.networking.pathing.ChannelMode | sed -n '/static {}/,$p'          # 因子表
$J -p -c -cp $AE2 appeng.blockentity.networking.ControllerBlockEntity | grep -B2 -A2 GridFlags  # CANNOT_CARRY + DENSE

# 2) 有没有抬上限的口子（0 = 没有；13 = 尺子在走）
grep -rl "setMaxChannels\|setChannelCapacity" --include=*.class /c/tmp/ae2
grep -rl "getUsedChannels"                    --include=*.class /c/tmp/ae2 | wc -l
# 边那个 32 有没有人读（期望：空）
grep -rl "getMaxChannels" --include=*.class /c/tmp/ae2 | while read f; do
    $J -p -c "$f" 2>/dev/null | grep -q "GridConnection.getMaxChannels" && echo "$f"; done

# 3) 三家
grep -rl "setMaxChannels\|CustomChannelProvider" /c/tmp/ae2cs   # AE2CS：命中 mixin/GridNodeMixin
grep -rl "setMaxChannels\|CustomChannelProvider" /c/tmp/eaep    # EAE Plus：空 = 它没抬
$J -p -c /c/tmp/ae2cs/io/github/lounode/ae2cs/mixin/GridNodeMixin.class | sed -n '/getMaxChannels/,$p'
$J -p -c /c/tmp/ae2cs/io/github/lounode/ae2cs/common/block/entity/EnderBroadcasterBlockEntity.class \
   | sed -n '/computeSenderVirtualNodeTarget/,/ireturn/p'
$J -p -c /c/tmp/eaep/com/extendedae_plus/ae/wireless/WirelessSlaveLink.class \
   | sed -n '/public void updateStatus/,/Exception table/p'

# DE：它读 AE2 的数、记自己的账（前两把尺都应该是空）
grep -rl "setMaxChannels\|CustomChannelProvider" /c/tmp/de
grep -rl "GridHelper.createConnection"           /c/tmp/de
D=com/fish_dan_/data_energistics/integration/ae/ae2/grid/ControllerChannelCapacity.class
$J -p -c /c/tmp/de/$D | sed -n '/private static int nativeControllerSupply/,/ireturn/p'   # 6 × (32 × factor)
$J -p -c /c/tmp/de/$D | sed -n '/private static int supplyFromControllerNode/,/ireturn/p'  # node.getMaxChannels() <= 0 -> 32*factor
$J -p    /c/tmp/de/com/fish_dan_/data_energistics/common/blockentity/tower/virtual/VirtualChannelLedger.class
```

## 7. 这份文档里还没证的

- **>32 的夹具已经跑过并且绿了**（第 2 节末的 `wireless_port_probe`：3 只端口、3 个独立远端网共 36 台，
  36/36 吃到频道；拆掉端口之后 0 台）。所以 D 的前提不再是推演，B 的"算式与上游一致"也第一次有了
  一个能对照的形状。**剩下没证的是实现面**：真实 BE 的端口生命周期（`onReady` 造、
  `setRemoved` / `onChunkUnloaded` 逐只 `destroy()`）、区块卸载再进来之后端口会不会重建、
  以及 `WirelessLinks` 的绑定改走端口之后 `at_limit` / `live` / 账单三处读数还自洽不自洽。
- **D 的 `6 面 × 32 × factor` 是照 DE 公开的建模思路、从 AE2 自己的公开常数推的**，不是 AE2 给的数
  （AE2 那边控制器节点答 0），也不是它的代码。真落地时这句必须进 javadoc，
  否则下一个读代码的人会去 AE2 里找这个数，或者更糟——去 DE 里抄代码，那会撞上第 4 节第 8 条。
- AE2CS 的 `32 × 相邻控制器数`、它给接收侧下发的配额，读的是它的字节码，**没有在我们的世界里复现过**。
- DE 的 `VirtualChannelLedger` 只读了接口与容量算式，**没读它把虚拟频道花在什么资源上**（哪本账、
  怎么扣 `physicalChannelUsage`）。所以"DE 做到了更多"这句话的范围是"它的塔服务更多目标"，
  不是"AE2 允许多频道"——这条区别在第 4 节第 1 条里。
- `X2/X3/X4` 三档只量了 `ChannelMode.<clinit>` 的常量，没在这些档下跑过任何频道断言。
- 模块与无线卡目前都没有配方、不进创造栏（既定取向：整代隐藏），所以 A/B/C/D 都是先定架构，再谈交付。
