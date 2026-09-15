package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageVentBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationDriveBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationThreadingCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingWorkerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingVentBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidInputHatchBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidOutputHatchBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.function.Predicate;

/** Factory and immutable read-only adapters for Trinity's three independent wings. */
public final class TrinityServiceAdapters {
    private TrinityServiceAdapters() {
    }

    public static TrinityService storage(List<BlockEntity> parts) {
        return new ReadOnlyService(TrinityService.Module.STORAGE, parts,
                part -> part instanceof SimplifyDriveBlockEntity
                        || part instanceof SimplifyEnergyCellBlockEntity
                        || part instanceof SimplifyStorageVentBlockEntity,
                part -> part instanceof SimplifyDriveBlockEntity drive
                        ? drive.isOnline() && drive.getMainNode().isPowered()
                        : part instanceof NEBlockEntity<?, ?> ne && ne.getMainNode().isPowered());
    }

    public static TrinityService computation(List<BlockEntity> parts) {
        return new ReadOnlyService(TrinityService.Module.COMPUTATION, parts,
                part -> part instanceof ECOComputationDriveBlockEntity
                        || part instanceof ECOComputationThreadingCoreBlockEntity
                        || part instanceof ECOComputationParallelCoreBlockEntity
                        || part instanceof ECOComputationCoolingControllerBlockEntity,
                part -> part instanceof NEBlockEntity<?, ?> ne && ne.getMainNode().isPowered());
    }

    public static TrinityService crafting(List<BlockEntity> parts) {
        return new ReadOnlyService(TrinityService.Module.CRAFTING, parts,
                part -> part instanceof ECOCraftingParallelCoreBlockEntity
                        || part instanceof ECOCraftingPatternBusBlockEntity
                        || part instanceof ECOCraftingWorkerBlockEntity
                        || part instanceof ECOCraftingVentBlockEntity
                        || part instanceof ECOFluidInputHatchBlockEntity
                        || part instanceof ECOFluidOutputHatchBlockEntity
                        || part instanceof ECOMachineInterfaceBlockEntity,
                part -> part instanceof NEBlockEntity<?, ?> ne && ne.getMainNode().isPowered());
    }

    private record ReadOnlyService(Module module, List<BlockEntity> source,
                                   Predicate<BlockEntity> component,
                                   Predicate<BlockEntity> onlineCheck)
            implements TrinityService {
        private ReadOnlyService {
            source = List.copyOf(source);
        }
        @Override
        public int componentCount() {
            return (int) source.stream().filter(component).count();
        }

        @Override
        public boolean online() {
            return componentCount() > 0 && source.stream().filter(component).allMatch(onlineCheck);
        }
    }
}
