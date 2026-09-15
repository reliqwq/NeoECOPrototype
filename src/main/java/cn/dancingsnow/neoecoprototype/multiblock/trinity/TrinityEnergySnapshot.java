package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/** Immutable, read-only energy view of the Trinity controller and three wings. */
public record TrinityEnergySnapshot(int nodeCount, int onlineNodes, int poweredNodes,
                                    boolean controllerPowered, boolean available) {
    public static TrinityEnergySnapshot capture(BlockEntity controller,
                                                List<BlockEntity> storage,
                                                List<BlockEntity> computation,
                                                List<BlockEntity> crafting) {
        int[] counts = new int[3];
        count(controller, counts);
        storage.forEach(part -> count(part, counts));
        computation.forEach(part -> count(part, counts));
        crafting.forEach(part -> count(part, counts));
        boolean controllerPowered = controller instanceof NEBlockEntity<?, ?> ne && ne.getMainNode().isPowered();
        return new TrinityEnergySnapshot(counts[0], counts[1], counts[2], controllerPowered,
                controllerPowered && counts[0] > 0 && counts[2] == counts[0]);
    }

    private static void count(BlockEntity entity, int[] counts) {
        if (entity instanceof NEBlockEntity<?, ?> ne) {
            counts[0]++;
            if (ne.getMainNode().isOnline()) {
                counts[1]++;
            }
            if (ne.getMainNode().isPowered()) {
                counts[2]++;
            }
        }
    }
}
