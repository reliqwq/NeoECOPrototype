package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class OptionalChemicalCellModelsTest {
    private static final String CLIENT =
            "cn/dancingsnow/neoecoprototype/client/NeoECOPrototypeClient";
    private static final String REGISTRATION =
            "cn/dancingsnow/neoecoprototype/registration/ModRegistration";
    private static final List<String> CHEMICAL_CELLS = List.of(
            "OPTIONAL_CHEMICAL_CELL_1K", "OPTIONAL_CHEMICAL_CELL_16K", "OPTIONAL_CHEMICAL_CELL_64K",
            "OPTIONAL_CHEMICAL_CELL_1M", "OPTIONAL_CHEMICAL_CELL_4M");

    @Test
    void absentMekanismIntegrationDoesNotDereferenceAnyChemicalCell() {
        assertDoesNotThrow(() -> NeoECOPrototypeClient.registerChemicalCellModels(
                null, null, null, null, null));
    }

    @Test
    void clientSetupPassesAllFiveCellsThroughOneGuardedRegistration() throws Exception {
        var node = read(CLIENT);
        var setup = method(node, "onClientSetup");
        long guardedCalls = calls(setup, CLIENT, "registerChemicalCellModels");
        assertEquals(1, guardedCalls, "client setup must register the chemical family through its null guard");
        for (String field : CHEMICAL_CELLS) {
            assertEquals(1, fields(setup, field),
                    "client setup must pass " + field + " to the guarded family registration");
        }

        var register = method(node, "registerChemicalCellModels");
        assertEquals(5, calls(register, "java/util/function/Supplier", "get"),
                "all five chemical cell suppliers must be read inside the guarded registration");
        assertEquals(5, calls(register, "cn/dancingsnow/neoecoae/api/ECOCellModels", "register"),
                "all five chemical cell rungs must keep their drive models");
    }

    private static ClassNode read(String name) throws Exception {
        var node = new ClassNode();
        try (InputStream input = OptionalChemicalCellModelsTest.class.getClassLoader()
                .getResourceAsStream(name + ".class")) {
            assertNotNull(input, name + " is missing from the test classpath");
            new ClassReader(input).accept(node, Opcodes.ASM9);
        }
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        var matches = node.methods.stream().filter(method -> method.name.equals(name)).toList();
        assertEquals(1, matches.size(), node.name + " must declare exactly one " + name);
        return matches.get(0);
    }

    private static long calls(MethodNode method, String owner, String name) {
        long count = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call
                    && call.owner.equals(owner) && call.name.equals(name)) {
                count++;
            }
        }
        return count;
    }

    private static long fields(MethodNode method, String name) {
        long count = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode field
                    && field.getOpcode() == Opcodes.GETSTATIC
                    && field.owner.equals(REGISTRATION) && field.name.equals(name)) {
                count++;
            }
        }
        return count;
    }
}
