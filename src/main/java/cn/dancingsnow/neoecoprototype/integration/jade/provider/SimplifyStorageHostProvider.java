package cn.dancingsnow.neoecoprototype.integration.jade.provider;

import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
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

/** Host controller tooltip: formed state + mounted drive count. */
public enum SimplifyStorageHostProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("formed")) {
            boolean formed = serverData.getBoolean("formed");
            tooltip.add(Component.translatable(formed ? "jade.neoecoprototype.formed"
                    : "jade.neoecoprototype.unformed")
                    .withStyle(formed ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        if (serverData.contains("driveCount")) {
            int drives = serverData.getInt("driveCount");
            int withCell = serverData.getInt("driveWithCell");
            tooltip.add(Component.translatable("jade.neoecoprototype.drives", withCell, drives));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof SimplifyStorageHostBlockEntity host) {
            var cluster = host.getCluster();
            tag.putBoolean("formed", cluster != null);
            int drives = cluster == null ? 0 : cluster.getDrives().size();
            int withCell = 0;
            if (cluster != null) {
                for (var drive : cluster.getDrives()) {
                    if (drive.hasCell()) {
                        withCell++;
                    }
                }
            }
            tag.putInt("driveCount", drives);
            tag.putInt("driveWithCell", withCell);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return SimplifyJadePlugin.id("simplify_storage_controller");
    }
}
