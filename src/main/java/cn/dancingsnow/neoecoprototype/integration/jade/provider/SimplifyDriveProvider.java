package cn.dancingsnow.neoecoprototype.integration.jade.provider;

import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.integration.jade.SimplifyJadePlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * Drive tooltip, structurally identical to eco's own {@code ECODriveProvider}:
 * mounted state, used/total bytes, used/total types. The AE2 Jade module keeps
 * its native device-online line because drives now register their own provider.
 */
public enum SimplifyDriveProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("online")) {
            boolean online = serverData.getBoolean("online");
            tooltip.add(Component.translatable(online
                    ? "jade.neoecoprototype.online"
                    : "jade.neoecoprototype.offline").withStyle(
                    online ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
        if (serverData.contains("mounted")) {
            boolean mounted = serverData.getBoolean("mounted");
            if (mounted) {
                tooltip.add(Component.translatable("jade.neoecoprototype.mounted").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("jade.neoecoprototype.unmounted").withStyle(ChatFormatting.RED));
            }
        }
        if (serverData.contains("usedBytes") && serverData.contains("totalBytes")) {
            long used = serverData.getLong("usedBytes");
            long total = serverData.getLong("totalBytes");
            tooltip.add(Component.translatable("jade.neoecoprototype.bytes_used", used, total));
        }
        if (serverData.contains("storedItemTypes") && serverData.contains("totalItemTypes")) {
            long types = serverData.getLong("storedItemTypes");
            long totalTypes = serverData.getLong("totalItemTypes");
            tooltip.add(Component.translatable("jade.neoecoprototype.types_used", types, totalTypes));
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof SimplifyDriveBlockEntity be) {
            tag.putBoolean("online", be.isOnline());
            tag.putBoolean("mounted", be.isMounted());
            IECOStorageCell cell = be.getCellInventory();
            if (cell != null) {
                tag.putLong("usedBytes", cell.getUsedBytes());
                tag.putLong("totalBytes", cell.getTotalBytes());
                tag.putLong("storedItemTypes", cell.getStoredItemTypes());
                tag.putLong("totalItemTypes", cell.getTotalItemTypes());
            }
        }
    }

    @Override
    public net.minecraft.resources.ResourceLocation getUid() {
        return SimplifyJadePlugin.id("simplify_drive");
    }
}
