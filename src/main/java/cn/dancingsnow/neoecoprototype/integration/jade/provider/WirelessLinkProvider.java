package cn.dancingsnow.neoecoprototype.integration.jade.provider;

import cn.dancingsnow.neoecoprototype.integration.jade.SimplifyJadePlugin;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/** Shows the wireless links that currently touch the block under Jade's crosshair. */
public enum WirelessLinkProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final String DATA = "wirelessLinks";
    private static final String PEERS = "peers";
    private static final String TOTAL = "total";
    private static final int MAX_PEERS = 6;

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (!(accessor.getLevel() instanceof ServerLevel server)) {
            return;
        }
        GlobalPos endpoint = GlobalPos.of(server.dimension(), accessor.getPosition());
        List<WirelessLinks.LinkSummary> links = WirelessLinks.linksTouching(server,
                ModRegistration.WIRELESS_LINKS, endpoint);
        CompoundTag data = new CompoundTag();
        data.putInt(TOTAL, links.size());
        ListTag peers = new ListTag();
        for (int i = 0; i < Math.min(links.size(), MAX_PEERS); i++) {
            peers.add(IntTag.valueOf(links.get(i).live() ? 1 : 0));
        }
        data.put(PEERS, peers);
        tag.put(DATA, data);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag root = accessor.getServerData();
        if (!root.contains(DATA, CompoundTag.TAG_COMPOUND)) {
            return;
        }
        ListTag peers = root.getCompound(DATA).getList(PEERS, CompoundTag.TAG_INT);
        // One line per state, not per link: without the far-end position the lines would be identical, so a
        // hub of four links would print the same sentence four times. A waiting link still stands out - it is
        // the other line, in yellow.
        int live = 0;
        for (int i = 0; i < peers.size(); i++) {
            if (peers.getInt(i) != 0) {
                live++;
            }
        }
        int waiting = peers.size() - live;
        if (live > 0) {
            tooltip.add(stateLine("jade.neoecoprototype.wireless_link.live", live, ChatFormatting.GREEN));
        }
        if (waiting > 0) {
            tooltip.add(stateLine("jade.neoecoprototype.wireless_link.waiting", waiting, ChatFormatting.YELLOW));
        }
        int total = root.getCompound(DATA).getInt(TOTAL);
        if (total > peers.size()) {
            tooltip.add(Component.translatable("jade.neoecoprototype.wireless_link.more", total - peers.size())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component stateLine(String key, int count, ChatFormatting color) {
        MutableComponent line = Component.translatable(key);
        if (count > 1) {
            line.append(" ×" + count);
        }
        return line.withStyle(color);
    }

    @Override
    public ResourceLocation getUid() {
        return SimplifyJadePlugin.id("wireless_links");
    }
}
