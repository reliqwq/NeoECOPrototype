package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorUI;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class MeteoriteDissociatorFacePreview {
    private static final float ICON_SIZE = 14.0F;
    private static final float ITEM_SIZE = 16.0F;

    private MeteoriteDissociatorFacePreview() {
    }

    public static void draw(GUIContext context, Direction direction,
                            MeteoriteDissociatorUI.FacePreviewState state,
                            float x, float y) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !(minecraft.player.containerMenu instanceof ModularUIContainerMenu menu)
                || !(menu.uiHolder instanceof BlockUIMenuType.BlockUIHolder holder)
                || minecraft.level == null) {
            return;
        }
        BlockPos position = holder.pos.relative(direction);
        BlockState blockState = minecraft.level.getBlockState(position);
        if (!state.matches(position, blockState)) {
            ItemStack icon = blockState.isAir()
                    ? ItemStack.EMPTY
                    : new ItemStack(blockState.getBlock().asItem());
            state.update(position, blockState, icon);
        }
        if (state.icon().isEmpty()) {
            return;
        }
        context.graphics.flush();
        PoseStack pose = context.graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(ICON_SIZE / ITEM_SIZE, ICON_SIZE / ITEM_SIZE, 1.0F);
        context.graphics.renderItem(state.icon(), 0, 0);
        pose.popPose();
        context.graphics.flush();
    }
}
