package cn.dancingsnow.neoecoprototype.blockentity.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

/**
 * The two ways this mod reaches for eco's bulk storage, read off our own compiled classes.
 *
 * <p>Both belong here rather than in the game-test suite because the thing being measured is a
 * <i>reference</i>, not a behaviour: what happens if a future eco stops registering
 * {@code cn.dancingsnow.neoecoae.storage.bulk.ECOBulkStorageCell} or the {@code neoecoae:mega_*} cell types
 * is decided by which classes name them and whether that name is reached on a path that runs. A live eco
 * always has both, so no runtime assertion in this repository can ever see the difference.</p>
 */
final class EcoBulkDependencyGatingTest {

    private static final Path MAIN_CLASSES = Path.of("build/classes/java/main/cn/dancingsnow/neoecoprototype");
    private static final String BULK_CELL_INTERNAL
            = "cn/dancingsnow/neoecoae/storage/bulk/ECOBulkStorageCell";
    private static final String CELL_TYPES = "cn/dancingsnow/neoecoprototype/api/EcoCellTypes";

    /**
     * The bulk cell class may be named in exactly one place, and that place has to ask first. Naming it on
     * an executed path is a {@code NoClassDefFoundError} the first time a player sets a priority on a
     * foreign eco bulk cell; {@code Class.forName(name, false, loader)} is what keeps the name unresolved
     * until the class is known to exist.
     */
    @Test
    void theBulkCellClassIsNamedInOnePlaceAndThatPlaceChecksFirst() throws Exception {
        var naming = new TreeSet<String>();
        var guarded = new TreeSet<String>();
        for (ClassNode node : ourClasses()) {
            boolean namesIt = false;
            for (var method : node.methods) {
                int firstReference = -1;
                int gateBefore = -1;
                int index = 0;
                for (AbstractInsnNode insn : method.instructions) {
                    if (isReferenceToBulkCell(insn)) {
                        namesIt = true;
                        if (firstReference < 0) {
                            firstReference = index;
                        }
                    }
                    // The guard has to be a boolean read of this class's own field that the JVM reaches
                    // before the reference: short-circuit && is what keeps the branch, and with it the
                    // name, unresolved. A probe that merely exists somewhere else in the class is not it.
                    if (insn instanceof FieldInsnNode field
                            && field.getOpcode() == org.objectweb.asm.Opcodes.GETSTATIC
                            && field.owner.equals(node.name) && "Z".equals(field.desc)
                            && (firstReference < 0 || index < firstReference)) {
                        gateBefore = index;
                    }
                    index++;
                }
                if (firstReference >= 0 && gateBefore >= 0) {
                    guarded.add(node.name + "#" + method.name);
                }
            }
            if (namesIt) {
                naming.add(node.name);
            }
        }
        // The ruler proves it can see the reference before it is allowed to report that there is only one.
        assertFalse(naming.isEmpty(), "no compiled class of ours reaches for " + BULK_CELL_INTERNAL
                + " at all, so this rule cannot be counting references. Either the drive's bulk branch is"
                + " gone or the scan is not reading the tree it thinks it is - re-compile main and re-read"
                + " this file.");
        assertEquals(List.of("cn/dancingsnow/neoecoprototype/blockentity/storage/SimplifyDriveBlockEntity"),
                new ArrayList<>(naming), "these classes reach for eco's bulk cell class; the plan is one"
                        + " gated home for it, and every extra name is another NoClassDefFoundError waiting"
                        + " for an eco release");
        assertFalse(guarded.isEmpty(), "the class that reaches for " + BULK_CELL_INTERNAL
                + " does not read a boolean of its own before the reference, so the name is live on a path"
                + " that runs and a deleted eco class becomes a crash instead of a warning: names " + naming
                + ", guarded methods " + guarded);
    }

    /** An instanceof, a checkcast, a call on the instance, or a class literal - all four resolve the class. */
    private static boolean isReferenceToBulkCell(AbstractInsnNode insn) {
        if (insn instanceof TypeInsnNode type) {
            return type.desc.equals(BULK_CELL_INTERNAL);
        }
        if (insn instanceof MethodInsnNode call) {
            return call.owner.equals(BULK_CELL_INTERNAL);
        }
        if (insn instanceof LdcInsnNode constant && constant.cst instanceof org.objectweb.asm.Type type) {
            return type.getInternalName().equals(BULK_CELL_INTERNAL);
        }
        return false;
    }

    /**
     * Every bulk cell-type getter has to go through {@code EcoCellTypes.bulkOrPlain}. The rule is per call
     * site on purpose: a getter that reads the registry directly and throws is invisible until a player
     * opens a storage host panel, which is exactly how the old shape took down the cell view of a whole
     * base long after a clean startup.
     */
    @Test
    void everyBulkTypeGetterGoesThroughTheFallbackResolver() throws Exception {
        var sites = List.of(
                new String[]{"cn/dancingsnow/neoecoprototype/items/SimplifyStorageCellItem",
                        "getMegaItemCellType"},
                new String[]{"cn/dancingsnow/neoecoprototype/items/SimplifySmallBulkFluidStorageCellItem",
                        "getMegaFluidCellType"},
                new String[]{"cn/dancingsnow/neoecoprototype/integration/mekanism"
                        + "/SimplifySmallBulkChemicalStorageCellItem", "getCellType"});
        var classes = ourClasses();
        var notFallbacked = new ArrayList<String>();
        for (String[] site : sites) {
            var node = classes.stream().filter(candidate -> candidate.name.equals(site[0])).findFirst()
                    .orElse(null);
            assertNotNull(node, site[0] + ": the class is not in the compiled output, so this rule read"
                    + " nothing about it");
            var method = node.methods.stream().filter(candidate -> candidate.name.equals(site[1]))
                    .findFirst().orElse(null);
            assertNotNull(method, site[0] + " no longer declares " + site[1] + ", so the bulk family"
                    + " resolves its type somewhere this file does not know about");
            boolean asksTheResolver = false;
            boolean throwsItself = false;
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call && call.owner.equals(CELL_TYPES)
                        && call.name.equals("bulkOrPlain")) {
                    asksTheResolver = true;
                }
                if (insn instanceof LdcInsnNode constant && constant.cst instanceof String text
                        && text.startsWith("Missing eco cell type:")) {
                    throwsItself = true;
                }
            }
            if (!asksTheResolver || throwsItself) {
                notFallbacked.add(site[0] + "." + site[1] + " (asksTheResolver=" + asksTheResolver
                        + ", throwsItsOwn=" + throwsItself + ")");
            }
        }
        assertTrue(notFallbacked.isEmpty(), "these getters do not read eco's bulk type through the"
                + " fallback resolver: " + notFallbacked + ". Reaching for the id and throwing means an eco"
                + " release that drops mega_item / mega_fluid / mega_chemical fails the first time a panel"
                + " or a renderer calls getCellType(), not at startup - see EcoCellTypes for the reading.");
    }

    private static List<ClassNode> ourClasses() throws Exception {
        if (!Files.isDirectory(MAIN_CLASSES)) {
            fail(MAIN_CLASSES + " is not there, so this file has no compiled output to read."
                    + " Run compileJava before test.");
        }
        var nodes = new ArrayList<ClassNode>();
        try (var paths = Files.walk(MAIN_CLASSES)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".class")).toList()) {
                try (InputStream input = Files.newInputStream(path)) {
                    var node = new ClassNode();
                    new org.objectweb.asm.ClassReader(input).accept(node, 0);
                    nodes.add(node);
                }
            }
        }
        assertFalse(nodes.isEmpty(), "no .class files under " + MAIN_CLASSES);
        return nodes;
    }
}
