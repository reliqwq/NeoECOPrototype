package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.SimplifyTrinityCluster;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator.StructureValidation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import cn.dancingsnow.neoecoae.gui.theme.NEStyleSheets;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Trinity controller state. Full storage/computation/crafting services come later. */
public class SimplifyTrinityControllerBlockEntity
        extends NEBlockEntity<SimplifyTrinityCluster, SimplifyTrinityControllerBlockEntity>
        implements ISyncPersistRPCBlockEntity {
    private static final ResourceLocation INVALID_TARGET =
            ResourceLocation.fromNamespaceAndPath("neoecoprototype", "__invalid_target__");
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @DescSynced
    private String syncedStatus = "Trinity: Not validated yet.";

    @DescSynced
    private String taskTargetText = "minecraft:iron_ingot";

    @DescSynced
    private String taskQuantityText = "1";

    private StructureValidation structureValidation = StructureValidation.invalid("Not validated yet.");

    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    public void setStructureValidation(StructureValidation validation) {
        this.structureValidation = validation;
    }

    public StructureValidation getStructureValidation() {
        return structureValidation;
    }

    /** Re-run the shell validator around the fixed controller anchor. */
    public void validateNow() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos min = worldPosition.offset(-3, -1, -3);
        BlockPos max = worldPosition.offset(3, 1, 3);
        SimplifyTrinityClusterCalculator calculator = new SimplifyTrinityClusterCalculator(this);
        StructureValidation validation = calculator.validateStructure(serverLevel, min, max);
        setStructureValidation(validation);
        syncedStatus = formatStatusForPanel();
        setChanged();
        markForUpdate();
    }

    public void setTaskTargetText(String text) {
        taskTargetText = text == null ? "" : text.trim();
        refreshTaskPreflight();
    }

    public void setTaskQuantityText(String text) {
        taskQuantityText = text == null ? "" : text.trim();
        refreshTaskPreflight();
    }

    public void refreshTaskPreflight() {
        validateNow();
    }

    public String getTaskTargetText() {
        return taskTargetText;
    }

    public int getTaskQuantity() {
        try {
            return Integer.parseInt(taskQuantityText);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRequest createTaskRequest() {
        ResourceLocation target;
        try {
            target = ResourceLocation.parse(taskTargetText);
        } catch (Exception ignored) {
            target = INVALID_TARGET;
        }
        return new cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRequest(
                cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRequest.Kind.GENERIC,
                target, getTaskQuantity(), true, true, true);
    }

    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (level instanceof ServerLevel) {
            validateNow();
        }
        UIElement root = new UIElement().layout(layout -> layout
                .width(320)
                .height(360)
                .flexDirection(FlexDirection.COLUMN)
                .gapAll(6))
                .addClass("panel_bg");
        root.addChild(new TextElement()
                .setText(Component.literal("Trinity Overview"))
                .layout(layout -> layout.widthPercent(100).height(20)));
        TextField targetField = new TextField();
        targetField.setText(taskTargetText);
        targetField.setTextResponder(text -> taskTargetText = text == null ? "" : text.trim());
        targetField.bind(DataBindingBuilder.string(() -> taskTargetText, this::setTaskTargetText).build());
        targetField.layout(layout -> layout.widthPercent(100).height(18));
        root.addChild(targetField);
        TextField quantityField = new TextField();
        quantityField.setNumbersOnlyInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
        quantityField.setText(taskQuantityText);
        quantityField.setTextResponder(text -> taskQuantityText = text == null ? "" : text.trim());
        quantityField.bind(DataBindingBuilder.string(() -> taskQuantityText, this::setTaskQuantityText).build());
        quantityField.layout(layout -> layout.widthPercent(100).height(18));
        root.addChild(quantityField);
        for (int line = 0; line < 14; line++) {
            final int lineIndex = line;
            Label statusLine = new Label();
            statusLine.setText(Component.literal(statusLine(lineIndex)));
            statusLine.bind(DataBindingBuilder.componentS2C(
                    () -> Component.literal(statusLine(lineIndex))).build());
            statusLine.layout(layout -> layout.widthPercent(100).height(16));
            root.addChild(statusLine);
        }
        return new ModularUI(UI.of(root,
                java.util.List.of(StylesheetManager.INSTANCE.getStylesheetSafe(NEStyleSheets.ECO))), holder.player);
    }

    private String statusLine(int index) {
        String[] lines = syncedStatus.split("\\n", -1);
        return index < lines.length ? lines[index] : "";
    }

    private String formatStatusForPanel() {
        if (!structureValidation.valid()) {
            return "Structure: invalid\n" + String.join("\n", structureValidation.issues());
        }
        String services = getCluster() == null
                ? "cluster-not-formed"
                : "storage=" + getCluster().getStorageService().status()
                + "\ncomputation=" + getCluster().getComputationService().status()
                + "\ncrafting=" + getCluster().getCraftingService().status();
        String energy = getCluster() == null
                ? "cluster-not-formed"
                : "nodes=" + getCluster().getEnergySnapshot().nodeCount()
                + ", online=" + getCluster().getEnergySnapshot().onlineNodes()
                + ", powered=" + getCluster().getEnergySnapshot().poweredNodes()
                + ", available=" + getCluster().getEnergySnapshot().available();
        var request = createTaskRequest();
        String target = taskTargetText + " x" + getTaskQuantity();
        String resource = getCluster() == null
                ? "cluster-not-formed"
                : (getCluster().isStorageResourceConnected()
                ? "connected; SIMULATE only"
                : "storage-host-grid-not-connected");
        String pattern = getCluster() == null
                ? "cluster-not-formed"
                : getCluster().getPatternCheck(request).summary();
        String plan = getCluster() == null
                ? "cluster-not-formed"
                : getCluster().getPlanCheck(request).summary();
        String readiness;
        if (getCluster() == null) {
            readiness = "cluster-not-formed";
        } else {
            var readinessResult = getCluster().checkTaskReadiness(request);
            readiness = readinessResult.executable()
                    ? "ready"
                    : "blocked: " + String.join("; ", readinessResult.reasons());
        }
        String counts = getCluster() == null
                ? "storage components=" + structureValidation.storageParts()
                + "\ncomputation components=" + structureValidation.computationParts()
                + "\ncrafting components=" + structureValidation.craftingParts()
                : "storage components=" + getCluster().getStorageParts().size()
                + "\ncomputation components=" + getCluster().getComputationParts().size()
                + "\ncrafting components=" + getCluster().getCraftingParts().size();
        return "Structure: valid\n"
                + counts
                + "\n\nServices:\n" + services
                + "\n\nEnergy: " + energy
                + "\nTask target: " + target
                + "\nResource check: " + resource
                + "\nPattern check: " + pattern
                + "\nPlan check: " + plan
                + "\nReadiness: " + readiness;
    }

    public Component getStatusMessage() {
        if (structureValidation.valid()) {
            String services = getCluster() == null
                    ? "services=cluster-not-formed"
                    : "services storage=" + getCluster().getStorageService().status()
                    + ", computation=" + getCluster().getComputationService().status()
                    + ", crafting=" + getCluster().getCraftingService().status();
            String energy = getCluster() == null
                    ? "energy=cluster-not-formed"
                    : "energy=nodes " + getCluster().getEnergySnapshot().nodeCount()
                    + ", online " + getCluster().getEnergySnapshot().onlineNodes()
                    + ", powered " + getCluster().getEnergySnapshot().poweredNodes()
                    + ", available " + getCluster().getEnergySnapshot().available();
            String readiness;
            if (getCluster() == null) {
                readiness = "readiness=cluster-not-formed";
            } else {
                var result = getCluster().checkTaskReadiness();
                readiness = result.executable()
                        ? "readiness=ready"
                        : "readiness=blocked (" + String.join("; ", result.reasons()) + ")";
            }
            int storageCount = getCluster() == null ? structureValidation.storageParts() : getCluster().getStorageParts().size();
            int computationCount = getCluster() == null ? structureValidation.computationParts() : getCluster().getComputationParts().size();
            int craftingCount = getCluster() == null ? structureValidation.craftingParts() : getCluster().getCraftingParts().size();
            return Component.literal("Trinity: valid; storage=" + storageCount
                    + ", computation=" + computationCount + ", crafting=" + craftingCount
                    + "; " + services + "; " + energy + "; " + readiness);
        }
        return Component.literal("Trinity: " + String.join("; ", structureValidation.issues()));
    }
    public SimplifyTrinityControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SimplifyTrinityClusterCalculator::new);
    }

    @Override
    public void onReady() {
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.storageControllerIdlePower());
    }
}
