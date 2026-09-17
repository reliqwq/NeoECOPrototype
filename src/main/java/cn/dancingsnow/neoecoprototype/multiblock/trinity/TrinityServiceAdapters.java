package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/** Read-only adapter for one Trinity-owned module; it never impersonates an eco CPU or subsystem. */
public final class TrinityServiceAdapters {
    private TrinityServiceAdapters() {
    }

    /**
     * Adapts a single Trinity module block entity. The module itself is the component, so the wing
     * reports online as soon as its module exists and its grid node is powered.
     */
    public static TrinityService module(TrinityService.Module module, @Nullable BlockEntity part) {
        List<BlockEntity> source = part == null ? List.of() : List.of(part);
        return new ReadOnlyService(module, source,
                candidate -> candidate == part,
                candidate -> candidate instanceof NEBlockEntity<?, ?> ne && ne.getMainNode().isPowered());
    }

    private record ReadOnlyService(TrinityService.Module module, List<BlockEntity> source,
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
