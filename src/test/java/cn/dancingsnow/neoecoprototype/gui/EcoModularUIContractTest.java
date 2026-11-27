package cn.dancingsnow.neoecoprototype.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

final class EcoModularUIContractTest {
    private static final String ECO_UI = "cn/dancingsnow/neoecoae/gui/common/ECOModularUI";
    private static final String ECO_SCREEN = "cn/dancingsnow/neoecoae/client/gui/ECOModularContainerScreen";
    private static final String CLIENT_BRIDGE = "cn/dancingsnow/neoecoae/gui/crafting/ClientUIBridge";
    private static final String RETIRED_MIXIN =
            "cn/dancingsnow/neoecoprototype/mixin/ECOClientUIBridgeMixin";
    private static final List<UiFactory> FACTORIES = List.of(
            new UiFactory("cn/dancingsnow/neoecoprototype/blockentity/storage/SimplifyStorageHostBlockEntity",
                    "createUI"),
            new UiFactory("cn/dancingsnow/neoecoprototype/blockentity/dissociation/MeteoriteDissociatorUI",
                    "create"),
            new UiFactory("cn/dancingsnow/neoecoprototype/blockentity/music/MusicRecorderUI", "create"),
            new UiFactory("cn/dancingsnow/neoecoprototype/items/WirelessChannelModuleUI", "create"));

    @Test
    void everyNativeSlotUiUsesEcosCoordinateAwareFactory() throws Exception {
        var screen = read(CLASSPATH.getResourceAsStream(ECO_SCREEN + ".class"), ECO_SCREEN);
        var opening = method(screen, "onScreenOpening");
        boolean checksEcoUi = false;
        for (AbstractInsnNode node : opening.instructions) {
            if (node instanceof TypeInsnNode type && type.getOpcode() == Opcodes.INSTANCEOF
                    && Type.getObjectType(type.desc).getInternalName().equals(ECO_UI)) {
                checksEcoUi = true;
            }
        }
        assertTrue(checksEcoUi, "eco's replacement screen no longer selects ECOModularUI, so this contract's"
                + " premise changed and the four call sites must be re-read");

        for (UiFactory factory : FACTORIES) {
            var ui = read(CLASSPATH.getResourceAsStream(factory.className + ".class"), factory.className);
            var method = method(ui, factory.methodName);
            int ecoFactories = 0;
            int plainFactories = 0;
            for (AbstractInsnNode node : method.instructions) {
                if (node instanceof MethodInsnNode call && call.owner.equals(ECO_UI)
                        && call.name.equals("create")) {
                    ecoFactories++;
                }
                if (node instanceof TypeInsnNode type && type.getOpcode() == Opcodes.NEW
                        && Type.getObjectType(type.desc).getInternalName()
                        .equals("com/lowdragmc/lowdraglib2/gui/ui/ModularUI")) {
                    plainFactories++;
                }
            }
            assertEquals(1, ecoFactories, factory + " must call ECOModularUI.create exactly once");
            assertEquals(0, plainFactories, factory + " still constructs a plain ModularUI and bypasses eco's"
                    + " native-slot hit testing");
        }
    }

    @Test
    void retiredClientBridgeMixinStaysRetiredWhileUpstreamGuardsTheServer() throws Exception {
        var bridge = read(CLASSPATH.getResourceAsStream(CLIENT_BRIDGE + ".class"), CLIENT_BRIDGE);
        var call = method(bridge, "call");
        int distGuard = -1;
        int reflection = -1;
        int index = 0;
        for (AbstractInsnNode node : call.instructions) {
            if (node instanceof MethodInsnNode invocation) {
                if (invocation.owner.equals("net/neoforged/api/distmarker/Dist")
                        && invocation.name.equals("isClient")) {
                    distGuard = index;
                }
                if (invocation.owner.equals("java/lang/Class") && invocation.name.equals("forName")) {
                    reflection = index;
                }
            }
            index++;
        }
        assertTrue(distGuard >= 0 && reflection > distGuard,
                "ClientUIBridge.call no longer checks the physical side before reflection; restore"
                        + " ECOClientUIBridgeMixin before this guard is allowed to pass");

        assertNull(CLASSPATH.getResourceAsStream(RETIRED_MIXIN + ".class"),
                "the retired ECOClientUIBridgeMixin class is still on the classpath");
        try (InputStream input = CLASSPATH.getResourceAsStream("neoecoprototype.mixins.json")) {
            assertNotNull(input, "neoecoprototype.mixins.json is missing from the test classpath");
            var json = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            for (String field : List.of("mixins", "client")) {
                var entries = json.getAsJsonArray(field);
                if (entries == null) continue;
                for (var entry : entries) {
                    assertFalse(entry.getAsString().equals("ECOClientUIBridgeMixin"),
                            "ECOClientUIBridgeMixin is registered again even though upstream now guards the"
                                    + " dedicated server itself");
                }
            }
        }
    }

    private static MethodNode method(ClassNode node, String name) {
        var matches = node.methods.stream().filter(method -> method.name.equals(name)).toList();
        assertEquals(1, matches.size(), node.name + " must declare exactly one " + name + " method");
        return matches.get(0);
    }

    private static ClassNode read(InputStream input, String name) throws Exception {
        assertNotNull(input, name + " is missing from the test classpath");
        var node = new ClassNode();
        new org.objectweb.asm.ClassReader(input).accept(node, Opcodes.ASM9);
        return node;
    }

    private static final ClassLoader CLASSPATH = EcoModularUIContractTest.class.getClassLoader();

    private record UiFactory(String className, String methodName) {
    }
}
