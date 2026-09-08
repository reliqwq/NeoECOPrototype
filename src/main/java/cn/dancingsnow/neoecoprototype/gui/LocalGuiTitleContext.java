package cn.dancingsnow.neoecoprototype.gui;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;

/** Tracks the local GUI currently being assembled without changing Eco's global translations. */
public final class LocalGuiTitleContext {
    private static final ThreadLocal<String> TITLE_KEY = new ThreadLocal<>();

    private LocalGuiTitleContext() {
    }

    public static void begin(ECOMachineInterfaceBlockEntity<?> blockEntity) {
        TITLE_KEY.set(localInterfaceTitle(blockEntity));
    }

    public static void begin(ECOCraftingPatternBusBlockEntity blockEntity) {
        TITLE_KEY.set(blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get())
                ? "block.neoecoprototype.simplify_crafting_pattern_bus" : null);
    }

    public static String replace(String key) {
        String localKey = TITLE_KEY.get();
        return localKey == null ? key : switch (key) {
            case "gui.neoecoae.storage_interface.title",
                    "gui.neoecoae.crafting_interface.title",
                    "block.neoecoae.computation_interface",
                    "block.neoecoae.crafting_pattern_bus" -> localKey;
            default -> key;
        };
    }

    public static void end() {
        TITLE_KEY.remove();
    }

    private static String localInterfaceTitle(ECOMachineInterfaceBlockEntity<?> blockEntity) {
        if (blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get())) {
            return "block.neoecoprototype.simplify_storage_interface";
        }
        if (blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get())
                || blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get())) {
            return "block.neoecoprototype.simplify_crafting_interface";
        }
        if (blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get())) {
            return "block.neoecoprototype.simplify_computation_interface";
        }
        return null;
    }
}
