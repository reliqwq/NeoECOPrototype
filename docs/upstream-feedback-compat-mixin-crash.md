# 问题反馈：未安装兼容模组时 neoecoae 无条件注册 compat Mixin 导致启动硬崩溃

## 结论

从 `07889ed9`（整理 Mixin 结构）开始，`META-INF/neoforge.mods.toml` 无条件注册了 4 个兼容模组专用 Mixin 配置，且各配置没有按 `ModList` 过滤的 plugin 守卫。在**未安装** Advanced AE / ExtendedAE / AE2 Omni Cells / A Useless Mod 的环境里，`neoecoae.compat.advanced_ae.mixins.json` 中的 `crafting.AdvancedAeCraftingServiceMixin` 附加失败，把 AE2 的 mod 构造直接炸掉，游戏无法启动。该问题在 `111a6fd6`（添加cell接口）仍然存在。

作者环境不易复现，推测是因为上游开发运行时默认装载了这些兼容模组（build.gradle 将 omni-cells 等作为 runtime 依赖）。

## 环境

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.233 |
| neoecoae | 21.2.0-preview14（从 commit `111a6fd6802ff44dc96e076feed3a854bb962582` 自行构建） |
| AE2 | 19.2.17 |
| Advanced AE / ExtendedAE / AE2 Omni Cells / A Useless Mod | **均未安装** |

## 复现步骤

1. 安装 neoecoae（含 `07889ed9` 之后的构建）+ AE2，不安装 Advanced AE；
2. 启动游戏；
3. mod 加载阶段崩溃，崩溃报告指向 `appeng.core.AppEngClient` 构造失败。

## 崩溃链

```
Failed to create mod instance. ModID: ae2, class appeng.core.AppEngClient
└─ MixinTransformerError: An unexpected critical error was encountered
   └─ MixinPreProcessorException: Attach error for
      neoecoae.compat.advanced_ae.mixins.json:crafting.AdvancedAeCraftingServiceMixin
      during activity: [Transform -> Method
      neoecoae$insertIntoAdvancedAeCpuForJob(...)J -> INVOKEVIRTUAL ->
      net/pedroksl/advanced_ae/common/entities/AdvCraftingBlockEntity::getCluster]
      └─ RuntimeException: java.lang.ClassNotFoundException:
         net.pedroksl.advanced_ae.common.entities.AdvCraftingBlockEntity
         (MixinPreProcessorStandard.transformMemberReference)
```

崩溃时机：AE2 `InitGridServices.init` 触发 `CraftingService` 类加载 → Mixin 附加该 mixin → 失败 → AE2 构造中断 → FATAL。

完整崩溃报告：`crash-2026-09-12_21.14.12-fml.txt`（随附）。

## 根因分析

两个因素叠加：

1. **无条件注册**：`neoforge.mods.toml` 直接列出 4 个 compat 配置
   （`neoecoae.compat.advanced_ae/extendedae/ae2omnicells/useless_mod.mixins.json`），
   配置本身没有 `"plugin"` 入口按 `ModList` 判断是否加载。
2. **`@Pseudo` 不覆盖方法体引用**：`AdvancedAeCraftingServiceMixin` 标了
   `@Pseudo` + `@Mixin(CraftingService.class)`。目标类是 AE2 自己的类（永远存在），
   所以 Mixin 一定会执行附加；但 `@Pseudo` 只豁免"目标类缺失"，不豁免
   **mixin 方法体字节码里对缺失类的直接引用**。该方法体内
   `INVOKEVIRTUAL net/pedroksl/advanced_ae/.../AdvCraftingBlockEntity::getCluster`
   在预处理器解析成员引用时抛 `ClassNotFoundException` → 附加失败。

注：同配置里的 accessor mixin（目标类直接是 Advanced AE 的类）只会打
`@Mixin target ... was not found` 的 WARN 并跳过，不致命——所以只有方法体引用
缺失类的 `AdvancedAeCraftingServiceMixin` 这类"目标是常驻类、引用是可选类"的
mixin 会硬崩。

## 修复建议

给 4 个 compat 配置各加一个 `MixinConfigPlugin`，在 `shouldLoadMixin` 中按
`ModList.get().isLoaded(...)` 过滤（或整个配置在 plugin 层拒绝加载），例如：

```json
{
  "plugin": "cn.dancingsnow.neoecoae.mixins.compat.AdvancedAeMixinPlugin",
  ...
}
```

```java
public class AdvancedAeMixinPlugin implements IMixinConfigPlugin {
    private static final String MODID = "advanced_ae";
    // shouldLoadMixin: return ModList.get().isLoaded(MODID);
}
```

也可以考虑把 `AdvancedAeCraftingServiceMixin` 方法体内的 Advanced AE 引用改为
反射/中介类（与 `ECOProcessingPatternDispatcher` 里对 thunderbolt 的反射处理
同思路），双重保险。

## 引入提交

- `07889ed9` 整理 Mixin 结构 —— 引入 4 个 compat mixin 配置并无条件注册
- `111a6fd6` 添加cell接口 —— 问题仍存在（本次构建所用提交）

## 受影响配置

- `neoecoae.compat.advanced_ae.mixins.json`（致命：AdvancedAeCraftingServiceMixin）
- `neoecoae.compat.extendedae.mixins.json`
- `neoecoae.compat.ae2omnicells.mixins.json`
- `neoecoae.compat.useless_mod.mixins.json`
