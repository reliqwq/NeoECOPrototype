package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * The two places our bytecode names one of eco's storage implementation classes, read off upstream's own
 * class files.
 *
 * <p>eco 21.2.11 kept them under {@code impl/storage}; 21.2.12 moved the whole tree to {@code storage}
 * (compare: 45 renames, 23 removed, 23 added), so this file is now reading the post-move names. That move
 * is the one upstream change that does not have to crash us: {@code SimplifySmallBulkStorageCellItem}
 * overrides {@code createCellInventory(ItemStack, ISaveProvider)} whose <em>return type</em> is one of the
 * moved classes, so when eco re-declares that method with the new return type, our method simply stops
 * being an override. The class still loads, the cell still works, and the byte-infinite engine we wrote is
 * quietly replaced by eco's standard one - a silent behaviour change, not a crash, and exactly what this
 * file is here to make loud before we ship rather than after a player reports a small-bulk cell that
 * stores less.
 *
 * <p>The second one is the drive renderer's static call into {@code ECOInfiniteStorageMember}; that path
 * only runs on the client, so the game-test server can never see it break either.
 *
 * <p>Both names in this file reddened on the real event, not just on a control: run against 21.2.12 while
 * the constants still said {@code impl/storage}, the renderer test failed - and its message blamed the
 * classpath, which is why {@link #ecoClass} now says "moved" out loud.
 */
final class UpstreamStoragePackageMoveContractTest {

    private static final String CELL_ITEM = "cn/dancingsnow/neoecoae/items/ECOStorageCellItem";
    private static final String OUR_SMALL_BULK =
            "cn/dancingsnow/neoecoprototype/items/SimplifySmallBulkStorageCellItem";
    private static final String INFINITE_MEMBER =
            "cn/dancingsnow/neoecoae/storage/infinite/ECOInfiniteStorageMember";
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
                + ". If the return type moved from impl/storage to storage, our byte-infinite engine is"
                + " silently gone and the cell falls back to eco's standard one - repoint the import in "
                + OUR_SMALL_BULK + " and re-run before shipping.");

        var descriptor = upstream.get(0);
        var returned = descriptor.substring(descriptor.indexOf(')') + 2, descriptor.length() - 1);
        assertNotNull(getClass().getClassLoader().getResourceAsStream(returned + ".class"),
                "the engine class eco's own signature names (" + returned + ") is not on the classpath,"
                        + " so nothing here can be trusted");
    }

    @Test
    void driveRendererStillCallsTheMemberCheckEcoDeclares() throws Exception {
        var declared = ecoClass(INFINITE_MEMBER).methods.stream()
                .filter(method -> method.name.equals("isMember"))
                .map(method -> method.desc)
                .toList();
        assertTrue(declared.contains("(Lnet/minecraft/world/item/ItemStack;)Z"),
                "eco no longer declares isMember(ItemStack)Z on " + INFINITE_MEMBER + ": " + declared);

        var renderer = read(UpstreamStoragePackageMoveContractTest.class.getClassLoader()
                .getResourceAsStream(OUR_DRIVE_RENDERER + ".class"));
        var calls = new ArrayList<String>();
        renderer.methods.forEach(method -> {
            for (AbstractInsnNode node : method.instructions) {
                if (node instanceof MethodInsnNode call && call.owner.equals(INFINITE_MEMBER)) {
                    calls.add(call.name + call.desc);
                }
            }
        });
        assertTrue(calls.contains("isMember(Lnet/minecraft/world/item/ItemStack;)Z"),
                "our drive renderer no longer calls isMember on " + INFINITE_MEMBER + " (it calls " + calls
                        + "), so the singularity-member drive frame is decided by something else now");
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
