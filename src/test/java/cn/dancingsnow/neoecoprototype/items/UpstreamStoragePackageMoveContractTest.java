package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * The two places our bytecode names one of eco's storage implementation classes, read off upstream's own
 * class files.
 *
 * <p>eco 21.3.0 moved the tree again, from {@code storage} to {@code grid.storage}. The small-bulk override
 * still names the engine class in its return descriptor, so this guard reads the descriptor from both sides.
 *
 * <p>The drive renderer used to call eco's internal {@code ECOInfiniteStorageMember.isMember}. eco 3.0 removed
 * that method; its broader {@code hasTransactionEvidence} deliberately excludes anonymous legacy members. The
 * renderer therefore reads the legacy marker through Minecraft's public data-component API and no longer names
 * an eco implementation class at all.</p>
 */
final class UpstreamStoragePackageMoveContractTest {

    private static final String CELL_ITEM = "cn/dancingsnow/neoecoae/items/ECOStorageCellItem";
    private static final String OUR_SMALL_BULK =
            "cn/dancingsnow/neoecoprototype/items/SimplifySmallBulkStorageCellItem";
    private static final String OUR_DRIVE_RENDERER =
            "cn/dancingsnow/neoecoprototype/client/renderer/blockentity/SimplifyDriveRenderer";

    @Test
    void ourSmallBulkEngineStillOverridesTheMethodEcoDeclares() throws Exception {
        var upstream = ecoClass(CELL_ITEM).methods.stream()
                .filter(method -> method.name.equals("createCellInventory"))
                .map(method -> method.desc)
                .toList();
        assertEquals(1, upstream.size(), "eco's cell item now declares " + upstream.size()
                + " createCellInventory methods (expected exactly one): " + upstream
                + " - read this whole file before picking one");

        var ours = read(UpstreamStoragePackageMoveContractTest.class.getClassLoader()
                .getResourceAsStream(OUR_SMALL_BULK + ".class")).methods.stream()
                .filter(method -> method.name.equals("createCellInventory"))
                .map(method -> method.desc)
                .toList();
        assertEquals(List.of(upstream.get(0)), ours, "our small-bulk cell no longer overrides eco's"
                + " createCellInventory: we declare " + ours + " but eco declares " + upstream
                + ". If the return type moved from storage to grid.storage, our byte-infinite engine is"
                + " silently gone and the cell falls back to eco's standard one - repoint the import in "
                + OUR_SMALL_BULK + " and re-run before shipping.");

        var descriptor = upstream.get(0);
        var returned = descriptor.substring(descriptor.indexOf(')') + 2, descriptor.length() - 1);
        assertNotNull(getClass().getClassLoader().getResourceAsStream(returned + ".class"),
                "the engine class eco's own signature names (" + returned + ") is not on the classpath,"
                        + " so nothing here can be trusted");
    }

    @Test
    void driveRendererKeepsTheLegacyMemberLedWithoutNamingEcoImplementation() throws Exception {
        var renderer = read(UpstreamStoragePackageMoveContractTest.class.getClassLoader()
                .getResourceAsStream(OUR_DRIVE_RENDERER + ".class"));
        for (var method : renderer.methods) {
            for (AbstractInsnNode node : method.instructions) {
                if (node instanceof MethodInsnNode call) {
                    assertFalse(call.owner.contains("/storage/infinite/ECOInfiniteStorageMember"),
                            "the client renderer still names eco's moved infinite-storage implementation: "
                                    + call.owner + "." + call.name);
                }
            }
        }

        MethodNode predicate = renderer.methods.stream()
                .filter(method -> method.name.equals("isLegacyInfiniteMember"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the drive renderer no longer has its legacy-member predicate"));
        boolean seesTag = false;
        boolean readsCustomData = false;
        boolean readsBoolean = false;
        for (AbstractInsnNode node : predicate.instructions) {
            if (node instanceof LdcInsnNode constant && "neoecoae_infinite_member".equals(constant.cst)) {
                seesTag = true;
            }
            if (node instanceof MethodInsnNode call
                    && call.owner.equals("net/minecraft/world/item/ItemStack")
                    && call.name.equals("getOrDefault")) {
                readsCustomData = true;
            }
            if (node instanceof MethodInsnNode call
                    && call.owner.equals("net/minecraft/nbt/CompoundTag")
                    && call.name.equals("getBoolean")) {
                readsBoolean = true;
            }
        }
        assertTrue(seesTag && readsCustomData && readsBoolean,
                "the legacy-member predicate no longer reads the exact marker through Minecraft's public API:"
                        + " tag=" + seesTag + ", customData=" + readsCustomData + ", boolean=" + readsBoolean);

        MethodNode render = renderer.methods.stream()
                .filter(method -> method.name.equals("render"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("the drive renderer no longer has render"));
        int predicateCall = -1;
        int mountedCall = -1;
        int index = 0;
        for (AbstractInsnNode node : render.instructions) {
            if (node instanceof MethodInsnNode call && call.owner.equals(renderer.name)) {
                if (call.name.equals("isLegacyInfiniteMember")) {
                    predicateCall = index;
                } else if (call.name.equals("isMounted")) {
                    mountedCall = index;
                }
            }
            index++;
        }
        assertTrue(predicateCall >= 0 && (mountedCall < 0 || predicateCall < mountedCall),
                "the legacy-member LED must be decided before the mounted/online gate");
    }

    private static ClassNode ecoClass(String internalName) throws Exception {
        var override = System.getProperty("neoecoprototype.test.ecoJar");
        if (override != null && !override.isBlank()) {
            try (var jar = new java.util.jar.JarFile(new java.io.File(override))) {
                var entry = jar.getJarEntry(internalName + ".class");
                assertNotNull(entry, "no " + internalName + " inside " + override);
                try (InputStream input = jar.getInputStream(entry)) {
                    return read(input);
                }
            }
        }
        var input = UpstreamStoragePackageMoveContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class");
        if (input == null) {
            // Distinguishing the two ways this can be empty matters: 21.2.12 took this branch because a
            // class moved package, and the old wording sent the reader to the build file instead.
            assertNotNull(UpstreamStoragePackageMoveContractTest.class.getClassLoader()
                            .getResourceAsStream(CELL_ITEM + ".class"),
                    "no eco class at all on the test classpath - the eco jar is not being resolved, so this"
                            + " test is not reading upstream at all");
            throw new AssertionError("eco no longer ships " + internalName + " at that path, although the rest"
                    + " of its classes resolve. That is a move or a rename, not a broken classpath: find the"
                    + " new location in the jar, repoint the constant in this file AND every reference in"
                    + " src/main, then re-run the suite.");
        }
        return read(input);
    }

    private static ClassNode read(InputStream input) throws Exception {
        assertNotNull(input, "our own class is missing from the test classpath");
        var node = new ClassNode();
        new org.objectweb.asm.ClassReader(input).accept(node, Opcodes.ASM9);
        return node;
    }
}
