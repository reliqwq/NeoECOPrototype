package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** AE2 pattern provider behavior under the addon's green visual variant. */
public class SimplifyPatternProviderBlockEntity extends PatternProviderBlockEntity {
    public SimplifyPatternProviderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(getMainNode(), this,
                NeoECOPrototypeServerConfig.GREEN_PATTERN_PROVIDER_SLOTS.get());
    }
}
