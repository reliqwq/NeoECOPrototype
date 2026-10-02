package cn.dancingsnow.neoecoprototype.block;

import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An ore of someone else's crystal: it drops eco's 天外寒冰 and chills whoever breaks it.
 */
public class SimplifyCryotheumOreBlock extends Block {
    public SimplifyCryotheumOreBlock(Properties properties) {
        super(properties);
    }

    /**
     * Powder snow does not damage on contact either - it fills a counter, and vanilla turns that into
     * freeze damage on its own once the player sits in it long enough.
     *
     * <p>Only the breaker is touched, and only while they are not already chilling, so a chain-mining
     * mod that routes its breaks through {@code ServerPlayerGameMode} pays one integer compare per ore
     * instead of an entity scan per ore per tick.
     */
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player != null) {
            int ticks = NeoECOPrototypeServerConfig.CRYOTHEUM_ORE_FREEZE_TICKS.get();
            if (ticks > 0 && player.getTicksFrozen() <= 0) {
                player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze(), ticks));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
