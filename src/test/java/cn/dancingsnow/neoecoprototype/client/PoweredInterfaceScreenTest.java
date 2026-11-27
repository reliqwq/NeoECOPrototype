package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.inventory.Slot;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class PoweredInterfaceScreenTest {
    private static final String SCREEN =
            "cn/dancingsnow/neoecoprototype/client/PoweredInterfaceScreen";

    @Test
    void secondRowOffsetUsesTheFirstAbsoluteYAcrossRepeatedInit() throws Exception {
        Slot slot = org.mockito.Mockito.mock(Slot.class);
        Map<Slot, Integer> originalYs = new IdentityHashMap<>();
        Field y = Slot.class.getDeclaredField("y");
        y.setAccessible(true);
        y.setInt(slot, 20);

        assertEquals(56, PoweredInterfaceScreen.secondRowY(slot, originalYs));
        y.setInt(slot, 56);

        assertEquals(56, PoweredInterfaceScreen.secondRowY(slot, originalYs));
        assertEquals(56, PoweredInterfaceScreen.secondRowY(slot, originalYs));
    }

    @Test
    void compiledScreenRoutesEverySecondRowThroughTheAbsoluteOffset() throws Exception {
        var node = new ClassNode();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(SCREEN + ".class")) {
            assertNotNull(input, SCREEN + " is missing from the test classpath");
            new ClassReader(input).accept(node, Opcodes.ASM9);
        }
        var matches = node.methods.stream()
                .filter(method -> method.name.equals("moveSecondRow"))
                .toList();
        assertEquals(1, matches.size(), "the screen must declare exactly one second-row layout method");
        MethodNode move = matches.get(0);
        long absoluteOffsets = 0;
        for (AbstractInsnNode instruction : move.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && call.owner.equals(SCREEN) && call.name.equals("secondRowY")) {
                absoluteOffsets++;
            }
        }
        assertEquals(1, absoluteOffsets,
                "second-row slots must use the cached absolute Y, not add to their current Y");
    }
}
