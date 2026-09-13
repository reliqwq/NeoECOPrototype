package cn.dancingsnow.neoecoprototype.integration.kubejs;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.BuilderTypeRegistry;
import net.minecraft.core.registries.Registries;

/**
 * KubeJS 插件入口。仅在 KubeJS 在场时由 KubeJS 自身通过
 * {@code kubejs.plugins.txt} 加载；本插件本体不引用任何其他模组类。
 *
 * <p>注册 {@code neoecoprototype:infinite_storage_matrix} 物品 builder 类型，
 * 脚本即可创建自定义无限存储矩阵：
 *
 * <pre>{@code
 * StartupEvents.registry('item', event => {
 *   event.create('infinite_dirt', 'neoecoprototype:infinite_storage_matrix')
 *        .itemType('minecraft:dirt')
 * })
 * }</pre>
 */
public class KJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBuilderTypes(BuilderTypeRegistry registry) {
        registry.of(
                Registries.ITEM,
                r -> r.add("infinite_storage_matrix", InfiniteMatrixBuilder.class, InfiniteMatrixBuilder::new)
        );
    }
}
