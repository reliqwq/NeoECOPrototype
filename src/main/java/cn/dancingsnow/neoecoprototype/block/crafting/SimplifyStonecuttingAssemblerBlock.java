package cn.dancingsnow.neoecoprototype.block.crafting;

import appeng.block.crafting.MolecularAssemblerBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Green molecular assembler that opens its own menu, which leaves out the encoded-pattern slot. */
public class SimplifyStonecuttingAssemblerBlock extends MolecularAssemblerBlock {
    public SimplifyStonecuttingAssemblerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                MenuOpener.open(ProcessorAssemblerMenu.type(), player, MenuLocators.forBlockEntity(blockEntity));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
