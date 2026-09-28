package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingSystem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * F1 crafting host. Publishes which of the two interface blocks the finished machine holds so the
 * blockstate file can give each case its own formed model. The calculator writes it; placement never
 * does.
 */
public class SimplifyCraftingSystemBlock extends ECOCraftingSystem {
    /**
     * True when the interface cell holds {@code simplify_crafting_network_interface} -- the 通讯接口, the
     * one that opens eco's interface UI. Same rule as C1 and L1: the id with "network" in it is the panel
     * the player uses, and the plain one is the endpoint the one-click builder places.
     */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    /**
     * The easter egg: once per host, the machine rolls a one-in-sixteen chance to grow a face, and the
     * result is saved into the host so re-forming never re-rolls it. Only meaningful with
     * {@code formed=true}, which is where the blockstate hands over to the {@code *_face} models.
     */
    public static final BooleanProperty HAS_MIND = BooleanProperty.create("has_mind");

    public SimplifyCraftingSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COMMUNICATION_INTERFACE, false)
                .setValue(HAS_MIND, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE, HAS_MIND);
    }
}
