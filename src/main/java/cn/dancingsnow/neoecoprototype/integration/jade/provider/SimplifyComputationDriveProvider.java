package cn.dancingsnow.neoecoprototype.integration.jade.provider;

import appeng.core.localization.Tooltips;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.integration.jade.SimplifyJadePlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Jade data for the L1 computation storage drive. */
public enum SimplifyComputationDriveProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains("online")) {
            boolean online = data.getBoolean("online");
            tooltip.add(Component.translatable(online
                    ? "jade.neoecoprototype.online"
                    : "jade.neoecoprototype.offline").withStyle(
                    online ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        if (data.contains("formed")) {
            tooltip.add(Component.translatable(data.getBoolean("formed")
                    ? "jade.neoecoprototype.formed"
                    : "jade.neoecoprototype.unformed"));
        }
        if (data.contains("mounted")) {
            boolean mounted = data.getBoolean("mounted");
            tooltip.add(Component.translatable(mounted
                    ? "jade.neoecoprototype.mounted"
                    : "jade.neoecoprototype.unmounted").withStyle(
                    mounted ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        if (data.contains("usedBytes") && data.contains("totalBytes")) {
            tooltip.add(Tooltips.bytesUsed(data.getLong("usedBytes"), data.getLong("totalBytes")));
        }
        if (data.contains("activeCpus")) {
            tooltip.add(Component.translatable(
                    "jade.neoecoprototype.computation_cpus", data.getInt("activeCpus")));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof SimplifyComputationDriveBlockEntity drive)) {
            return;
        }

        tag.putBoolean("online", drive.getMainNode().isOnline()
                && SimplifyGridFacade.isConnected(drive.getMainNode()));
        tag.putBoolean("formed", drive.isFormed());
        tag.putBoolean("mounted", drive.getCellStack() != null && !drive.getCellStack().isEmpty());

        if (drive.getCluster() != null) {
            tag.putLong("usedBytes", drive.getCluster().getOwnUsedStorage());
            tag.putLong("totalBytes", drive.getCluster().getTotalStorage());
            tag.putInt("activeCpus", drive.getCluster().getActiveCPUCount());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return SimplifyJadePlugin.id("simplify_computation_drive");
    }
}
