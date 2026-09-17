package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostActionUI;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.block.trinity.SimplifyTrinityControllerBlock;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyTrinityDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.SimplifyTrinityCluster;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityEnergySnapshot;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityCraftingExecutor;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityResourceCheck;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityWorkSnapshot;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator.StructureValidation;
import com.google.common.collect.ImmutableSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import cn.dancingsnow.neoecoae.gui.theme.NEStyleSheets;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Trinity controller state. Full storage/computation/crafting services come later. */
public class SimplifyTrinityControllerBlockEntity
        extends NEBlockEntity<SimplifyTrinityCluster, SimplifyTrinityControllerBlockEntity>
        implements ISyncPersistRPCBlockEntity, MultiBlockBuildController.Host, ICraftingRequester {
    private static final ResourceLocation INVALID_TARGET =
            ResourceLocation.fromNamespaceAndPath("neoecoprototype", "__invalid_target__");
    private static final String DEFAULT_TASK_TARGET = "minecraft:iron_ingot";
    private static final String DEFAULT_TASK_QUANTITY = "1";
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Persisted
    @DescSynced
    private int selectedBuildLength = 1;
    @DescSynced
    private boolean buildInProgress;
    private final MultiBlockBuildController buildController = new MultiBlockBuildController(this);

    @DescSynced
    private String syncedStatus = "Trinity: Not validated yet.";

    @DescSynced
    private String taskTargetText = "minecraft:iron_ingot";

    @DescSynced
    private String taskQuantityText = "1";

    private StructureValidation structureValidation = StructureValidation.invalid("Not validated yet.");
    private final cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime taskRuntime =
            new cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime();
    /**
     * Turns a preflighted request into a real AE2 crafting job. Crafting itself belongs to the
     * network's CPU; Trinity owns the request, the link, and the output check.
     */
    private final TrinityCraftingExecutor craftingExecutor = new TrinityCraftingExecutor(this);
    private int statusRefreshTick;
    private int serverTickCount;

    /** Server ticks this controller has received; a stuck zero means the block entity ticker is dead. */
    public int getServerTickCount() {
        return serverTickCount;
    }

    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    public TrinityCraftingExecutor getCraftingExecutor() {
        return craftingExecutor;
    }

    /**
     * Submits the configured target to AE2's crafting service.
     *
     * <p>Trinity's own readiness check is advisory, so it is not used as the gate here: AE2's
     * planner is the authority and reports a precise failure (missing ingredient, no CPU, ...) if
     * the request cannot run. That failure is more useful than a generic "blocked".</p>
     */
    public boolean startCraftingTask() {
        if (craftingExecutor.isBusy()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel) || getCluster() == null) {
            return false;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !"valid".equals(taskInputStatus())) {
            return false;
        }
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(createTaskRequest().targetItem());
        if (item == net.minecraft.world.item.Items.AIR) {
            return false;
        }
        var craftingModule = getCluster().getCraftingModule();
        if (craftingModule == null || !craftingModule.hasAdjacentCraftingMachine()) {
            craftingExecutor.failBeforeStart(
                    "No adjacent AE2 crafting machine can accept the Trinity pattern; place a Molecular Assembler beside the crafting module.");
            taskRuntime.syncExecution(craftingExecutor.phase(), craftingExecutor.detail());
            syncedStatus = formatStatusForPanel();
            setChanged();
            markForUpdate();
            return false;
        }
        taskRuntime.clear();
        craftingExecutor.reset();
        boolean started = craftingExecutor.start(serverLevel, grid, IActionSource.ofMachine(this),
                AEItemKey.of(item), getTaskQuantity());
        taskRuntime.syncExecution(craftingExecutor.phase(), craftingExecutor.detail());
        syncedStatus = formatStatusForPanel();
        setChanged();
        markForUpdate();
        return started;
    }

    /** Cancels a live job, or clears the report of a finished one. */
    public void cancelCraftingTask() {
        if (craftingExecutor.isBusy()) {
            craftingExecutor.cancel();
        } else {
            craftingExecutor.reset();
            taskRuntime.clear();
            refreshTaskPreflight();
        }
        taskRuntime.syncExecution(craftingExecutor.phase(), craftingExecutor.detail());
        syncedStatus = formatStatusForPanel();
        setChanged();
        markForUpdate();
    }

    // ==================== AE2 crafting requester ====================

    @Override
    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return craftingExecutor.getRequestedJobs();
    }

    @Override
    public long insertCraftedItems(ICraftingLink link, AEKey what, long amount, Actionable mode) {
        if (!craftingExecutor.acceptsCraftedItems(link) || amount <= 0) {
            return 0L;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            return 0L;
        }
        return grid.getStorageService().getInventory().insert(what, amount, mode,
                IActionSource.ofMachine(this));
    }

    @Override
    public void jobStateChange(ICraftingLink link) {
        craftingExecutor.onJobStateChange(link);
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        craftingExecutor.save(data);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        craftingExecutor.load(data);
        taskRuntime.syncExecution(craftingExecutor.phase(), craftingExecutor.detail());
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
        BlockPos min = worldPosition.offset(-1, -1, -1);
        BlockPos max = worldPosition.offset(1, 1, 1);
        SimplifyTrinityClusterCalculator calculator = new SimplifyTrinityClusterCalculator(this);
        StructureValidation validation = calculator.validateStructure(serverLevel, min, max);
        setStructureValidation(validation);
        if (getCluster() != null) {
            taskRuntime.refresh(getCluster().checkTaskReadiness(createTaskRequest()));
        } else {
            taskRuntime.clear();
        }
        syncedStatus = formatStatusForPanel();
        setChanged();
        markForUpdate();
    }

    public void setTaskTargetText(String text) {
        String next = text == null ? "" : text.trim();
        if (!next.equals(taskTargetText) && !craftingExecutor.isBusy()) {
            clearFinishedTaskReport();
        }
        taskTargetText = next;
        refreshTaskPreflight();
    }

    public void setTaskQuantityText(String text) {
        String next = text == null ? "" : text.trim();
        if (!next.equals(taskQuantityText) && !craftingExecutor.isBusy()) {
            clearFinishedTaskReport();
        }
        taskQuantityText = next;
        refreshTaskPreflight();
    }

    private void clearFinishedTaskReport() {
        if (craftingExecutor.phase() != TrinityCraftingExecutor.Phase.IDLE) {
            craftingExecutor.reset();
            taskRuntime.clear();
        }
    }

    public void refreshTaskPreflight() {
        validateNow();
        if (getCluster() != null) {
            taskRuntime.refresh(getCluster().checkTaskReadiness(createTaskRequest()));
        } else {
            taskRuntime.clear();
        }
    }

    public cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime getTaskRuntime() {
        return taskRuntime;
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

    private String taskInputStatus() {
        if (taskTargetText.isBlank()) {
            return "无效: 目标物品为空";
        }
        try {
            ResourceLocation target = ResourceLocation.parse(taskTargetText);
            if (net.minecraft.core.registries.BuiltInRegistries.ITEM.get(target)
                    == net.minecraft.world.item.Items.AIR
                    && !target.equals(ResourceLocation.withDefaultNamespace("air"))) {
                return "无效: 物品未注册";
            }
        } catch (Exception ignored) {
            return "无效: 物品 ID 格式错误";
        }
        if (getTaskQuantity() <= 0) {
            return "无效: 数量必须大于 0";
        }
        return "valid";
    }

    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (level instanceof ServerLevel) {
            validateNow();
        }
        UIElement root = new UIElement().layout(layout -> layout
                .width(320)
                .height(300)
                .flexDirection(FlexDirection.COLUMN)
                .gapAll(2))
                .addClass("panel_bg");
        root.addChild(new TextElement()
                .setText(Component.literal("Trinity 合成终端"))
                .layout(layout -> layout.widthPercent(100).height(18)));
        root.addChild(labelledRow("目标物品", () -> taskTargetText, this::setTaskTargetText));
        root.addChild(labelledRow("数量", () -> taskQuantityText, this::setTaskQuantityText));
        UIElement taskRow = new UIElement()
                .layout(layout -> layout.widthPercent(100).height(20)
                        .flexDirection(FlexDirection.ROW).gapAll(2));
        Button startButton = new Button();
        startButton.setText("开始任务");
        startButton.setOnServerClick(event -> startCraftingTask());
        startButton.layout(layout -> layout.widthPercent(50).height(20));
        taskRow.addChild(startButton);
        Button cancelButton = new Button();
        cancelButton.setText("取消 / 清除");
        cancelButton.setOnServerClick(event -> cancelCraftingTask());
        cancelButton.layout(layout -> layout.widthPercent(50).height(20));
        taskRow.addChild(cancelButton);
        root.addChild(taskRow);
        for (int line = 0; line < 14; line++) {
            final int lineIndex = line;
            Label statusLine = new Label();
            statusLine.setText(formatStatusComponent(statusLine(lineIndex)));
            statusLine.bind(DataBindingBuilder.componentS2C(
                    () -> formatStatusComponent(statusLine(lineIndex))).build());
            statusLine.layout(layout -> layout.widthPercent(100).height(14));
            root.addChild(statusLine);
        }
        // eco's action bar renders as a vertical side button bar (HostSideButtonBar), which is its
        // normal shape next to a panel; it holds the multiblock builder, priority and guide entries.
        StorageHostActionUI.Elements actionUI = StorageHostActionUI.create(new StorageHostActionUI.Config(
                holder.player,
                () -> selectedBuildLength,
                this::isMirrorBuild,
                mirror -> buildController.setMirrorBuild(holder.player, mirror),
                () -> buildController.decreaseBuildLength(holder.player),
                () -> buildController.increaseBuildLength(holder.player),
                () -> requestBuild(holder.player),
                this::isFormed,
                () -> buildInProgress,
                buildController::createLocalPreviewPlan,
                () -> 0,
                priority -> {
                },
                delta -> {
                },
                () -> false,
                () -> 0L,
                () -> {
                }));
        actionUI.addTo(root);
        return new ModularUI(UI.of(root,
                java.util.List.of(StylesheetManager.INSTANCE.getStylesheetSafe(NEStyleSheets.ECO))), holder.player);
    }

    /** One compact "label + input" row, so both task fields are labelled the same way. */
    private UIElement labelledRow(String label,
                                  java.util.function.Supplier<String> getter,
                                  java.util.function.Consumer<String> setter) {
        UIElement row = new UIElement()
                .layout(layout -> layout.widthPercent(100).height(18)
                        .flexDirection(FlexDirection.ROW).gapAll(2));
        row.addChild(new Label().setText(Component.literal(label))
                .layout(layout -> layout.widthPercent(32).height(18)));
        TextField field = new TextField();
        field.setText(getter.get());
        field.bind(DataBindingBuilder.string(getter, setter).build());
        field.layout(layout -> layout.widthPercent(68).height(18));
        row.addChild(field);
        return row;
    }

    private String statusLine(int index) {
        String[] lines = syncedStatus.split("\\n", -1);
        return index < lines.length ? lines[index] : "";
    }

    private static Component formatStatusComponent(String line) {
        Component text = Component.literal(line);
        if (line.contains("失败") || line.contains("无效") || line.contains("未连接")
                || line.contains("缺少") || line.contains("不足") || line.contains("不可执行")) {
            return text.copy().withStyle(ChatFormatting.RED);
        }
        // 任务设置有效只是字段校验通过；整机是否可以执行由“就绪”行说明，所以这里不用绿色，
        // 否则会出现绿色“有效”紧挨着红色“失败”的误读。
        if (line.startsWith("就绪: 可以执行") || line.contains("已完成")) {
            return text.copy().withStyle(ChatFormatting.GREEN);
        }
        return text;
    }

    /** Runtime state names in player-facing Chinese. */
    private static String stateName(cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime.State state) {
        return switch (state) {
            case IDLE -> "空闲";
            case READY -> "就绪";
            case BLOCKED -> "受阻";
            case RUNNING -> "合成中";
            case WAITING_OUTPUT -> "等待回收";
            case COMPLETED -> "已完成";
            case FAILED -> "失败";
        };
    }

    private static String phaseName(TrinityCraftingExecutor.Phase phase) {
        return switch (phase) {
            case IDLE -> "空闲";
            case PLANNING -> "规划中";
            case RUNNING -> "合成中";
            case WAITING_OUTPUT -> "等待回收";
            case COMPLETED -> "已完成";
            case FAILED -> "失败";
        };
    }

    /**
     * Turns the executor's diagnostic English into a player-facing sentence. The raw text stays
     * untouched in the log, so nothing is lost for debugging.
     */
    private static String reasonText(String reason) {
        if (reason == null || reason.isEmpty()) {
            return "无";
        }
        if (reason.contains("No AE2 crafting CPU")) {
            return "网络中没有 AE2 合成 CPU";
        }
        if (reason.contains("No adjacent AE2 crafting machine")) {
            return "样板模块旁没有分子装配室";
        }
        if (reason.contains("only a simulation")) {
            int missing = reason.indexOf("missing ");
            return missing < 0 ? "材料不足，计划未提交"
                    : "缺少材料 " + reason.substring(missing + "missing ".length());
        }
        if (reason.startsWith("Completed: ")) {
            return "已完成 " + reason.substring("Completed: ".length())
                    .replace(" reached the network.", " 已回到网络");
        }
        if (reason.startsWith("Planning ")) {
            return "正在规划 " + reason.substring("Planning ".length());
        }
        if (reason.startsWith("Crafting ")) {
            return "正在合成 " + reason.substring("Crafting ".length());
        }
        if (reason.contains("waiting for the output")) {
            return "合成结束，等待产物回到网络";
        }
        if (reason.contains("ME network disappeared")) {
            return "执行中 ME 网络断开";
        }
        if (reason.contains("quantity must be greater than zero")) {
            return "数量必须大于 0";
        }
        if (reason.contains("NO_SUITABLE_CPU_FOUND")) {
            return "AE2 CPU 正忙或容量不足";
        }
        if (reason.contains("was cancelled")) {
            return "任务已被取消";
        }
        if (reason.contains("world reload")) {
            return "世界重载中断，未自动恢复";
        }
        if (reason.contains("timed out")) {
            return "执行超时";
        }
        return reason;
    }

    /** A concrete next step for the current failure, instead of just an error string. */
    private static String suggestionText(String reason) {
        if (reason == null || reason.isEmpty()) {
            return "点击开始任务即可提交";
        }
        if (reason.contains("No adjacent AE2 crafting machine")) {
            return "在样板模块旁放置 AE2 分子装配室";
        }
        if (reason.contains("No AE2 crafting CPU")) {
            return "在网络中放置 AE2 合成存储器";
        }
        if (reason.contains("only a simulation")) {
            return "补足材料，或加入对应样板";
        }
        if (reason.contains("NO_SUITABLE_CPU_FOUND")) {
            return "等待当前任务结束再开始";
        }
        if (reason.contains("timed out")) {
            return "检查分子装配室供电与样板";
        }
        if (reason.contains("world reload")) {
            return "重新点击开始任务";
        }
        return "可重新设置目标后重试";
    }

    private String formatWorkStatus(TrinityWorkSnapshot snapshot,
                                    cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime runtime) {
        boolean executing = runtime.isExecuting();
        String state = executing
                || runtime.state() == cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime.State.BLOCKED
                ? stateName(runtime.state()) : snapshot.state();
        String reason = runtime.reasons().isEmpty() ? "" : runtime.reasons().get(0);
        StringBuilder out = new StringBuilder("状态: ").append(state);
        TrinityCraftingExecutor.Phase phase = craftingExecutor.phase();
        // COMPLETED and FAILED are terminal: they need a reason (and a next step), not a progress
        // readout, so they are deliberately not treated as "in flight" here.
        boolean inFlight = phase == TrinityCraftingExecutor.Phase.PLANNING
                || phase == TrinityCraftingExecutor.Phase.RUNNING
                || phase == TrinityCraftingExecutor.Phase.WAITING_OUTPUT;
        boolean failed = phase == TrinityCraftingExecutor.Phase.FAILED
                || runtime.state() == cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRuntime.State.BLOCKED;
        if (inFlight) {
            out.append(" | 进度: ")
                    .append(craftingExecutor.produced())
                    .append('/').append(craftingExecutor.requested());
        } else if (phase == TrinityCraftingExecutor.Phase.COMPLETED) {
            out.append(" | 产出: ")
                    .append(craftingExecutor.produced())
                    .append('/').append(craftingExecutor.requested());
        }
        if (!reason.isEmpty()) {
            out.append('\n').append(inFlight ? "进度: " : phase == TrinityCraftingExecutor.Phase.COMPLETED
                    ? "结果: " : "原因: ").append(reasonText(reason));
            if (failed) {
                out.append('\n').append("建议: ").append(suggestionText(reason));
            }
        }
        return out.toString();
    }

    /** Usable text width inside the 320px panel, leaving room for padding and scrollbar. */
    private static final int STATUS_PIXEL_WIDTH = 296;

    /** Minecraft's default font advances 9px for CJK glyphs and 6px for ASCII. */
    private static int glyphWidth(char c) {
        if (c == ' ') {
            return 4;
        }
        return c < 0x2E80 ? 6 : 9;
    }

    private static int textWidth(String text) {
        int width = 0;
        for (int i = 0; i < text.length(); i++) {
            width += glyphWidth(text.charAt(i));
        }
        return width;
    }

    /**
     * Wraps by rendered width, not by character count: a 66-character limit silently overflowed the
     * 320px panel because CJK glyphs are 9px wide and ASCII glyphs only 6px.
     */
    private static List<String> wrapStatusLines(String text) {
        List<String> lines = new java.util.ArrayList<>();
        for (String raw : text.split("\n", -1)) {
            StringBuilder current = new StringBuilder();
            int width = 0;
            int lastSpace = -1;
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if (width + glyphWidth(c) > STATUS_PIXEL_WIDTH && current.length() > 0) {
                    if (lastSpace > 0) {
                        String carry = current.substring(lastSpace + 1);
                        lines.add(current.substring(0, lastSpace));
                        current.setLength(0);
                        current.append(carry);
                        width = textWidth(carry);
                    } else {
                        lines.add(current.toString());
                        current.setLength(0);
                        width = 0;
                    }
                    lastSpace = -1;
                    if (c == ' ') {
                        continue;
                    }
                }
                if (c == ' ') {
                    lastSpace = current.length();
                }
                current.append(c);
                width += glyphWidth(c);
            }
            lines.add(current.toString().stripTrailing());
        }
        return lines;
    }

    private int getAe2CpuCount() {
        IGrid grid = getMainNode().getGrid();
        return grid == null ? 0 : grid.getCraftingService().getCpus().size();
    }

    private String formatStatusForPanel() {
        if (!structureValidation.valid()) {
            return String.join("\n", wrapStatusLines("Structure: invalid\n"
                    + String.join("\n", structureValidation.issues())));
        }
        String structure = getCluster() == null
                ? "结构: 未形成"
                : "结构: 正常 | 电力: " + (getCluster().getEnergySnapshot().available() ? "正常" : "不足");
        String services = getCluster() == null
                ? "网络服务: 未连接"
                : "网络 CPU: AE2/eco " + getAe2CpuCount()
                + " 台 | 分子装配室 " + (getCluster().getCraftingModule() != null
                && getCluster().getCraftingModule().hasAdjacentCraftingMachine() ? "已连接" : "未连接");
        var request = createTaskRequest();
        String inputStatus = taskInputStatus();
        // "任务设置" is the field-level validation (id format + quantity); it is deliberately not
        // called a material check, which is what the separate 材料 line reports.
        String target = "任务设置: " + (inputStatus.equals("valid") ? "有效" : inputStatus)
                + " | 目标: " + taskTargetText + " × " + getTaskQuantity();
        TrinityResourceCheck resourceCheck = getCluster() == null
                ? null : getCluster().getResourceCheck(request);
        String resource;
        if (resourceCheck == null) {
            resource = "库存提示: 未检查";
        } else if (resourceCheck.requirements().isEmpty()) {
            resource = "库存提示: " + (resourceCheck.available() ? "目标当前可见" : "目标当前不足")
                    + "（AE2 最终规划）";
        } else {
            String detail = resourceCheck.requirements().stream()
                    .map(requirement -> "  " + requirement.describe())
                    .collect(java.util.stream.Collectors.joining("\n"));
            resource = "库存提示: " + (resourceCheck.available() ? "目标当前可见" : "目标当前不足")
                    + "（AE2 最终规划）\n" + detail;
        }
        String pattern;
        String patternOutputs;
        if (getCluster() == null) {
            pattern = "样板: 未连接";
            patternOutputs = "可产出: 未知";
        } else {
            var patternCheck = getCluster().getPatternCheck(request);
            pattern = "样板: 预检候选 " + patternCheck.matchingPatterns()
                    + "/" + patternCheck.loadedPatterns();
            patternOutputs = "可产出: " + (patternCheck.loadedOutputs().isEmpty()
                    ? "无" : String.join(", ", patternCheck.loadedOutputs()));
        }
        String plan = getCluster() == null
                ? "计划提示: 未连接"
                : "计划提示: AE2 提交时最终规划"
                + " | 当前 CPU " + getAe2CpuCount() + " 台";
        String readiness = getCluster() == null
                ? "提交条件: 未知"
                : (getCluster().checkTaskReadiness(request).executable()
                ? "提交条件: 输入格式有效，最终结果由 AE2 判断"
                : "提交条件: 输入格式无效");
        String work = getCluster() == null
                ? "状态: 未连接"
                : formatWorkStatus(TrinityWorkSnapshot.capture(getCluster(), inputStatus.equals("valid")), taskRuntime);
        String materials = pattern.equals("样板: 未连接")
                ? pattern
                : pattern + " | " + resource;
        StringBuilder text = new StringBuilder()
                .append(structure).append('\n')
                .append(services).append('\n')
                .append(target).append('\n')
                .append(work).append('\n')
                .append(materials).append('\n')
                .append(patternOutputs).append('\n')
                .append(plan).append('\n')
                .append(readiness);
        return String.join("\n", wrapStatusLines(text.toString()));
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
        getMainNode().addService(ICraftingRequester.class, this);
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.storageControllerIdlePower());
    }

    // ============================ auto-build ============================

    /** Server ticker used by the incremental non-creative builder and the crafting executor. */
    public static void tick(Level level, BlockPos pos, BlockState state,
                            SimplifyTrinityControllerBlockEntity controller) {
        controller.buildController.tick(level);
        controller.tickCraftingExecutor();
    }

    /**
     * Advances a live crafting job. The panel text is only rebuilt when the phase changes or every
     * second, because building it re-runs the whole preflight scan.
     */
    private void tickCraftingExecutor() {
        if (!(level instanceof ServerLevel)) {
            return;
        }
        serverTickCount++;
        boolean busy = craftingExecutor.isBusy();
        TrinityCraftingExecutor.Phase before = craftingExecutor.phase();
        if (busy) {
            craftingExecutor.tick(getMainNode().getGrid(), IActionSource.ofMachine(this));
        }
        TrinityCraftingExecutor.Phase after = craftingExecutor.phase();
        taskRuntime.syncExecution(after, craftingExecutor.detail());
        if (before != after || (busy && ++statusRefreshTick % 20 == 0)) {
            syncedStatus = formatStatusForPanel();
            markForUpdate();
        }
    }

    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyTrinityDefinition.L1;
    }

    @Override
    public int getMinBuildLength() {
        return getBuildDefinition().getExpandMin();
    }

    @Override
    public int getMaxBuildLength() {
        return getBuildDefinition().getExpandMax();
    }

    @Override
    public int getSelectedBuildLength() {
        return selectedBuildLength;
    }

    @Override
    public void setSelectedBuildLength(int length) {
        selectedBuildLength = Math.clamp(length, getMinBuildLength(), getMaxBuildLength());
    }

    /**
     * Trinity's layout is NOT mirror-symmetric (storage west, computation east, crafting north),
     * so a mirrored build would put the modules where {@code SimplifyTrinityClusterCalculator}
     * rejects them -- the player would spend 23 casings on a shell that never forms. The toggle is
     * therefore deliberately inert, and the panel's mirror button does nothing.
     */
    @Override
    public boolean isMirrorBuild() {
        return false;
    }

    @Override
    public void setMirrorBuild(boolean mirrorBuild) {
        // Intentionally ignored; see isMirrorBuild().
    }

    @Override
    public boolean isBuildInProgress() {
        return buildInProgress;
    }

    @Override
    public void setBuildInProgress(boolean buildInProgress) {
        this.buildInProgress = buildInProgress;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return level != null
                && level.getBlockState(worldPosition).getBlock() instanceof SimplifyTrinityControllerBlock
                && player.level() == level
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Level getBuildLevel() {
        return level;
    }

    @Override
    public BlockPos getBuildPosition() {
        return worldPosition;
    }

    @Override
    public BlockState getBuildState() {
        return getBlockState();
    }

    @Override
    public void rebuildAfterBuild() {
        rebuildMultiblock();
    }

    @Override
    public void buildStateChanged() {
        setChanged();
        markForUpdate();
    }

    private void requestBuild(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!canPlayerInteract(player)) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.too_far");
            return;
        }
        if (!(level instanceof ServerLevel)) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.server_only");
            return;
        }
        if (isFormed()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.already_formed");
            return;
        }
        if (isBuildInProgress()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.build_in_progress");
            return;
        }
        MultiBlockPlacementPlan plan = buildController.createLocalPreviewPlan();
        if (plan == null) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.no_plan");
            return;
        }
        if (!plan.getConflictPositions().isEmpty()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.conflicts",
                    plan.getConflictPositions().size());
            return;
        }
        if (!serverPlayer.isCreative()
                && !cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService
                        .hasRequiredItems(serverPlayer, plan.getRequiredItems())) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.missing_items");
            return;
        }
        buildController.autoBuild(serverPlayer);
    }

    private static void sendBuildMessage(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }
}
