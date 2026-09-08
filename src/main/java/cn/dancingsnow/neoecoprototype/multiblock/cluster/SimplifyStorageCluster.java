package cn.dancingsnow.neoecoprototype.multiblock.cluster;

import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECluster;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import net.minecraft.core.Direction;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageCasingBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageInterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Cluster of the L1 storage subsystem. The layout / semantics mirror eco's own
 * {@code NEStorageCluster}, but bound to this addon's block entities and to the
 * eco generic base {@link NECluster} (grid + multiblock runtime inherited).
 */
public class SimplifyStorageCluster extends NECluster<SimplifyStorageCluster> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyStorageCluster.class);

    private SimplifyStorageHostBlockEntity controller = null;
    private final List<SimplifyDriveBlockEntity> drives = new ArrayList<>();
    private final List<SimplifyEnergyCellBlockEntity> energyCells = new ArrayList<>();
    private SimplifyStorageInterfaceBlockEntity theInterface = null;
    private final List<SimplifyStorageCasingBlockEntity> casings = new ArrayList<>();

    public SimplifyStorageCluster(BlockPos boundMin, BlockPos boundMax) {
        super(boundMin, boundMax);
    }

    public SimplifyStorageHostBlockEntity getController() {
        return controller;
    }

    public List<SimplifyDriveBlockEntity> getDrives() {
        return drives;
    }

    public List<SimplifyEnergyCellBlockEntity> getEnergyCells() {
        return energyCells;
    }

    public SimplifyStorageInterfaceBlockEntity getTheInterface() {
        return theInterface;
    }

    @Override
    public void addBlockEntity(NEBlockEntity<SimplifyStorageCluster, ?> blockEntity) {
        super.addBlockEntity(blockEntity);
        if (blockEntity instanceof SimplifyDriveBlockEntity driveBlockEntity) {
            drives.add(driveBlockEntity);
        } else if (blockEntity instanceof SimplifyEnergyCellBlockEntity energyCellBlockEntity) {
            energyCells.add(energyCellBlockEntity);
        } else if (blockEntity instanceof SimplifyStorageInterfaceBlockEntity interfaceBlockEntity) {
            theInterface = interfaceBlockEntity;
        } else if (blockEntity instanceof SimplifyStorageHostBlockEntity systemBlockEntity) {
            controller = systemBlockEntity;
        } else if (blockEntity instanceof SimplifyStorageCasingBlockEntity casingBlockEntity) {
            casings.add(casingBlockEntity);
        }
    }

    @Override
    protected BlockPos getCasingHideOrigin() {
        return controller == null ? null : controller.getBlockPos();
    }

    @Override
    public boolean shouldCasingRenderInClassic(NEBlockEntity<SimplifyStorageCluster, ?> blockEntity) {
        if (!(blockEntity instanceof ECOMachineCasingBlockEntity)
                || controller == null
                || !shouldCasingHide(blockEntity)) {
            return false;
        }

        BlockPos offset = blockEntity.getBlockPos().subtract(controller.getBlockPos());
        Direction facing = controller.getBlockState().getValue(SimplifyStorageControllerBlock.FACING);
        int localX = switch (facing) {
            case NORTH -> offset.getX();
            case EAST -> offset.getZ();
            case SOUTH -> -offset.getX();
            case WEST -> -offset.getZ();
            default -> throw new IllegalStateException("Storage controller must face horizontally");
        };
        int localZ = switch (facing) {
            case NORTH -> offset.getZ();
            case EAST -> -offset.getX();
            case SOUTH -> -offset.getZ();
            case WEST -> offset.getX();
            default -> throw new IllegalStateException("Storage controller must face horizontally");
        };

        // Match neweco8: the formed controller occupies local x/z [0, 1], y [-1, 1].
        return !(localX >= 0 && localX <= 1
                && localZ >= 0 && localZ <= 1
                && offset.getY() >= -1 && offset.getY() <= 1);
    }

    @Override
    public void destroy() {
        LOGGER.info("SimplifyStorageCluster DESTROYED at {} (min={}, max={})",
                controller != null ? controller.getBlockPos() : BlockPos.ZERO,
                getBoundsMin(), getBoundsMax());
        super.destroy();
        this.controller = null;
        this.theInterface = null;
        this.drives.clear();
        this.energyCells.clear();
        this.casings.clear();
    }
}
