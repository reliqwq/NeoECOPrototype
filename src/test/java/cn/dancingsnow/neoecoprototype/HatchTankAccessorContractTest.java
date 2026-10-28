package cn.dancingsnow.neoecoprototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Our hatch code must reach eco's fluid tank through an accessor, never through the field.
 *
 * <p>This is the shape that shipped the only player-visible break we have ever had from an upstream type
 * change: eco 21.2.11 re-declared {@code ECOFluidInputHatchBlockEntity.tank} as its own
 * {@code util.LongFluidTank}, and a {@code getfield} is matched on name <em>and</em> descriptor, so the
 * released 1.4.2 - whose source still said {@code blockEntity.tank} - threw {@code NoSuchFieldError} on
 * 21.2.11 without any of us editing a line. Two of the three reads here were on that field. The ruler in
 * this test found the same two instructions in the published 1.4.2 jar, which is what makes the green run
 * below worth something rather than just an empty scan.
 *
 * <p>eco 21.2.12 answered our issue #143 by adding {@code getFluidTankHandler()} to both hatch block
 * entities (0 declarations in 21.2.10 and 21.2.11, 2 in 21.2.12), and its body is
 * {@code getfield tank; areturn} - the same object the field gave us, so switching over changes no
 * behaviour at runtime. It only takes {@code LongFluidTank} out of <em>our</em> constant pool. If eco
 * re-types the field again, the accessor's own descriptor breaks on their side first and they fix it
 * inside their jar; we read a {@code FluidTank} either way.
 *
 * <p>Deliberately not covered: {@code ECOCraftingPatternBusBlockEntity.itemHandler}, which we also read as
 * a field and which has no accessor. It is {@code public final IItemHandlerModifiable} - a NeoForge type
 * upstream has no reason to re-declare, so the hazard is a different order of magnitude and leaving it
 * out keeps this rule strict enough to mean something.
 */
final class HatchTankAccessorContractTest {

    private static final String INPUT_HATCH_BE =
            "cn/dancingsnow/neoecoae/blocks/entity/crafting/ECOFluidInputHatchBlockEntity";
    private static final String OUTPUT_HATCH_BE =
            "cn/dancingsnow/neoecoae/blocks/entity/crafting/ECOFluidOutputHatchBlockEntity";
    private static final String ACCESSOR_DESC =
            "()Lnet/neoforged/neoforge/fluids/capability/templates/FluidTank;";

    private static final String OUR_MAIN = "cn/dancingsnow/neoecoprototype/NeoECOPrototype";
    private static final String OUR_OUTPUT_BLOCK =
            "cn/dancingsnow/neoecoprototype/block/crafting/SimplifyFluidOutputHatchBlock";
    private static final Set<String> OUR_CLASSES = Set.of(OUR_MAIN, OUR_OUTPUT_BLOCK);

    @Test
    void ecoStillDeclaresTheTankAccessorOnBothHatches() throws Exception {
        for (String hatch : List.of(INPUT_HATCH_BE, OUTPUT_HATCH_BE)) {
            var declared = ecoClass(hatch).methods.stream()
                    .filter(method -> method.name.equals("getFluidTankHandler"))
                    .map(method -> method.desc)
                    .toList();
            assertEquals(List.of(ACCESSOR_DESC), declared, "expected exactly one"
                    + " getFluidTankHandler() returning a NeoForge FluidTank on " + hatch + ", found "
                    + declared + ". If the return type moved again, our capability lambdas and the output"
                    + " hatch UI no longer compile against it - and the field underneath is the only"
                    + " fallback, which is the read that broke 1.4.2.");
        }
    }

    @Test
    void ourBytecodeReadsNoEcoHatchField() throws Exception {
        for (String ourClass : OUR_CLASSES) {
            var reads = new ArrayList<String>();
            for (var method : read(ourClass).methods) {
                for (AbstractInsnNode node : method.instructions) {
                    if (node instanceof FieldInsnNode field
                            && (field.getOpcode() == Opcodes.GETFIELD || field.getOpcode() == Opcodes.PUTFIELD)
                            && (field.owner.equals(INPUT_HATCH_BE) || field.owner.equals(OUTPUT_HATCH_BE))) {
                        reads.add(field.name + ":" + field.desc);
                    }
                }
            }
            assertTrue(reads.isEmpty(), ourClass + " reads eco's hatch fields directly again: " + reads
                    + ". A field reference is matched on the exact descriptor, so the next time eco"
                    + " re-types tank - as 21.2.11 did, from FluidTank to util.LongFluidTank - this jar"
                    + " throws NoSuchFieldError at runtime while its source still compiles. Use"
                    + " getFluidTankHandler() instead.");
        }
    }

    @Test
    void eachHatchSiteCallsTheAccessorThatServesIt() throws Exception {
        // Which class must call it on which block entity, written out rather than inferred: our main class
        // posts both faces, the output hatch block builds one UI. A control that replaced the input face's
        // read with `new FluidTank(16000)` compiled, answered non-null, and desynced every pipe from the
        // machine - so "some hatch, somewhere" would have been too loose to be worth running.
        var expected = Map.of(OUR_MAIN, Set.of(INPUT_HATCH_BE, OUTPUT_HATCH_BE),
                OUR_OUTPUT_BLOCK, Set.of(OUTPUT_HATCH_BE));
        for (var site : expected.entrySet()) {
            var owners = new ArrayList<String>();
            for (var method : read(site.getKey()).methods) {
                for (AbstractInsnNode node : method.instructions) {
                    if (node instanceof MethodInsnNode call
                            && call.name.equals("getFluidTankHandler") && call.desc.equals(ACCESSOR_DESC)) {
                        owners.add(call.owner);
                    }
                }
            }
            assertEquals(site.getValue(), Set.copyOf(owners), site.getKey()
                    + " calls getFluidTankHandler on " + owners + " but must call it on exactly "
                    + site.getValue() + ". If one of these stopped calling it, that face or UI is served by"
                    + " something else now - and if that something is a tank built here, pipes fill an"
                    + " object the machine never reads.");
        }
    }

    private static ClassNode read(String internalName) throws Exception {
        var input = HatchTankAccessorContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class");
        assertNotNull(input, internalName + " is not on the test classpath, so this file is not reading"
                + " our own produced bytecode");
        return load(input);
    }

    private static ClassNode ecoClass(String internalName) throws Exception {
        var override = System.getProperty("neoecoprototype.test.ecoJar");
        if (override != null && !override.isBlank()) {
            try (var jar = new java.util.jar.JarFile(new java.io.File(override))) {
                var entry = jar.getJarEntry(internalName + ".class");
                assertNotNull(entry, "no " + internalName + " inside " + override);
                try (InputStream input = jar.getInputStream(entry)) {
                    return load(input);
                }
            }
        }
        var input = HatchTankAccessorContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class");
        assertNotNull(input, "no " + internalName + " on the test classpath - the eco jar is not being"
                + " resolved, so this test is not reading upstream at all");
        return load(input);
    }

    private static ClassNode load(InputStream input) throws Exception {
        var node = new ClassNode();
        new org.objectweb.asm.ClassReader(input).accept(node, Opcodes.ASM9);
        return node;
    }
}
