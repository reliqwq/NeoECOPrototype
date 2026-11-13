package cn.dancingsnow.neoecoprototype.blockentity.wireless;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** A fixed in-world AE2 node that can serve as the local endpoint of a wireless link. */
public final class WirelessChannelModuleBlockEntity extends AENetworkedBlockEntity
        implements IInWorldGridNodeHost {

    public WirelessChannelModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected appeng.api.networking.IManagedGridNode createMainNode() {
        return super.createMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
    }

    @Override
    public IGridNode getGridNode(Direction side) {
        return getMainNode().getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction side) {
        return AECableType.DENSE_SMART;
    }
}
