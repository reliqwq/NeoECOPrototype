package cn.dancingsnow.neoecoprototype.gui;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks the local GUI currently being assembled without changing Eco's global translations. */
public final class LocalGuiTitleContext {
    private static final Logger LOGGER = LoggerFactory.getLogger("neoecoprototype");
    /** Warn once per resolved local title, so a broken mixin shows up in the log instead of silently. */
    private static final Set<String> REPORTED_MISSING_REDIRECT = ConcurrentHashMap.newKeySet();

    private static final ThreadLocal<String> TITLE_KEY = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> REDIRECT_FIRED = new ThreadLocal<>();

    private LocalGuiTitleContext() {
    }

    public static void begin(ECOMachineInterfaceBlockEntity<?> blockEntity) {
        TITLE_KEY.set(localInterfaceTitle(blockEntity));
        REDIRECT_FIRED.set(Boolean.FALSE);
    }

    public static void begin(ECOCraftingPatternBusBlockEntity blockEntity) {
        TITLE_KEY.set(blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get())
                ? "block.neoecoprototype.simplify_crafting_pattern_bus" : null);
        REDIRECT_FIRED.set(Boolean.FALSE);
    }

    public static String replace(String key) {
        String localKey = TITLE_KEY.get();
        if (localKey == null) {
            return key;
        }
        return switch (key) {
            case "gui.neoecoae.storage_interface.title",
                    "gui.neoecoae.crafting_interface.title",
                    "block.neoecoae.computation_interface",
                    "block.neoecoae.crafting_pattern_bus" -> {
                REDIRECT_FIRED.set(Boolean.TRUE);
                yield localKey;
            }
            default -> key;
        };
    }

    /**
     * Reports a title redirect that never ran. The eco UI mixins redirect a compiler generated
     * lambda name, so any lambda added upstream renumbers it and silently costs us the localised
     * title; that is exactly what eco 21.2.0-beta5 did to StorageInterfaceUI#create.
     */
    public static void end() {
        try {
            String localKey = TITLE_KEY.get();
            if (localKey != null && !Boolean.TRUE.equals(REDIRECT_FIRED.get())
                    && REPORTED_MISSING_REDIRECT.add(localKey)) {
                LOGGER.warn("Local GUI title {} was resolved but no eco UI redirect fired - the @Redirect "
                        + "target in ECO{{Storage,Crafting,Computation}}InterfaceUIMixin no longer matches "
                        + "this eco build, so those GUIs keep eco's default title.", localKey);
            }
        } finally {
            TITLE_KEY.remove();
            REDIRECT_FIRED.remove();
        }
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
