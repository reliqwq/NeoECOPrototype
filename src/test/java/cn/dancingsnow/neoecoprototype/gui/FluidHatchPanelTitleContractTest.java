package cn.dancingsnow.neoecoprototype.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * Where eco's two hatch panels get the title they draw, read off the class file of the jar we compile
 * against, and the two name keys that title is made of on our side.
 *
 * <p>Behaviour cannot answer this: the suite already opens both panels
 * ({@code fluidHatchesBuildTheirUI}) and gets a non-null UI back, and a title is a string a screen paints,
 * which the gameTest server never looks at. So the claim is pinned at the shape that produces it.</p>
 *
 * <p>The reading that made this file, taken 2026-11-06 across every jar in {@code neoecobeta/} - the input
 * panel's title argument in {@code gui/common/FluidInputHatchUI.createRoot} and the output panel's in
 * {@code blocks/entity/crafting/ECOFluidOutputHatchBlockEntity.createUI}:</p>
 *
 * <pre>
 *   21.2.13  21.2.14  21.2.15-beta   input: ldc "block.neoecoae.input_hatch"    output: ldc "block.neoecoae.output_hatch"
 *   21.2.15                          input: getBlockState().getBlock().getDescriptionId()   output: still ldc
 * </pre>
 *
 * <p>eco issue #155 is the reason for that last row: we asked for the literal to become the block's own
 * name, they did it, and it shipped in the stable release only. Our run floor is {@code [21.2.15-beta,)},
 * so one addon jar now shows <em>our</em> name to a player on 21.2.15 and <em>eco's</em> to a player still on
 * the beta. That is what the 1.4.6 notes have to say precisely, and it is why the first arm below is a test
 * rather than a comment: an override of {@code createUI} used to be how we bought that title, we deleted it
 * on 2026-10-27 because it was a hand copy of upstream's method body, and if this arm ever reddens the
 * answer is to re-file upstream - not to put the copy back.</p>
 */
final class FluidHatchPanelTitleContractTest {

    private static final String INPUT_UI = "cn/dancingsnow/neoecoae/gui/common/FluidInputHatchUI";
    private static final String INPUT_BE =
            "cn/dancingsnow/neoecoae/blocks/entity/crafting/ECOFluidInputHatchBlockEntity";
    private static final String OUTPUT_BE =
            "cn/dancingsnow/neoecoae/blocks/entity/crafting/ECOFluidOutputHatchBlockEntity";
    private static final String BLOCK_NAME = "net/minecraft/world/level/block/Block";
    private static final Path LANG_DIR
            = Path.of("src/main/resources/assets/neoecoprototype/lang");

    /**
     * The F1 input hatch panel is titled by the block standing there, so
     * {@code block.neoecoprototype.simplify_fluid_input_hatch} is what a player reads.
     */
    @Test
    void inputHatchPanelTakesItsTitleFromTheBlock() throws Exception {
        var ui = ecoClass(INPUT_UI);
        var named = callsTo(ui, BLOCK_NAME, "getDescriptionId");
        assertEquals(1, named.size(), "the input hatch panel no longer asks the block for its description id"
                + " (found " + named.size() + " calls in " + INPUT_UI + "), so eco issue #155 has been"
                + " reverted or this test is reading a jar older than 21.2.15 - either way the panel title"
                + " of " + "simplify_fluid_input_hatch is not ours any more. Say so in the release notes"
                + " and re-file upstream; do not answer this by re-adding a createUI override, which is a"
                + " copy of upstream's method body and the thing we removed on 2026-10-27.");
        var literals = ecoOwnTitleKeys(ui);
        assertTrue(literals.isEmpty(), "the input hatch panel passes eco's own title back in as a literal"
                + " again: " + literals + " in " + INPUT_UI + ". With the block-name call also present the"
                + " panel would carry two titles, so read both arms before deciding which one moved.");
    }

    /**
     * The same method reads the tank through the accessor eco gave us in issue #143, which is what makes the
     * panel show the machine's own content rather than a copy. Pinned next to the title arm because both
     * live in the same five upstream instructions and a refactor there moves them together.
     */
    @Test
    void inputHatchPanelReadsTheTankThroughThePublishedAccessor() throws Exception {
        var ui = ecoClass(INPUT_UI);
        assertEquals(1, callsTo(ui, INPUT_BE, "getFluidTankHandler").size(),
                "the input hatch panel no longer asks the block entity for its tank handler, so the tank it"
                        + " draws comes from somewhere else now - issue #143 promised addons this accessor");
        var fields = new ArrayList<String>();
        ui.methods.forEach(method -> {
            for (AbstractInsnNode node : method.instructions) {
                if (node instanceof FieldInsnNode field && field.owner.equals(INPUT_BE)) {
                    fields.add(field.name);
                }
            }
        });
        assertTrue(fields.isEmpty(), "the input hatch panel reads eco's fields directly (" + fields
                + ") while it also has the accessor: the accessor is the promise, so this is drift to report");
    }

    /**
     * The output half, which we never got: {@code ECOFluidOutputHatchBlockEntity.createUI} still loads eco's
     * own key as a string, so our output hatch's panel is titled "ECO Fluid Output Hatch" and the two
     * hatches of one machine disagree. This arm is <em>meant</em> to redden when upstream fixes it -
     * see the message.
     */
    @Test
    void outputHatchPanelIsStillTitledByEco() throws Exception {
        var be = ecoClass(OUTPUT_BE);
        var literals = ecoOwnTitleKeys(be);
        assertTrue(literals.contains("block.neoecoae.output_hatch"),
                "eco's output hatch no longer hardcodes its panel title (" + literals + ") - that is the one"
                        + " line 上游请求稿/NEO_ECO_AE_PR_FLUID_OUTPUT_HATCH_TITLE.md asks for, and it landed."
                        + " Retire that draft, delete the caveat sentence from the release notes' 变更 5 and"
                        + " from SimplifyFluidOutputHatchBlock's javadoc, and turn this arm into 'both panels"
                        + " take their title from the block'. Do not just invert the assertion here: the"
                        + " player-facing sentence is the other half of this claim.");
        assertTrue(callsTo(be, BLOCK_NAME, "getDescriptionId").isEmpty(),
                "the output hatch now asks the block for its name while it still passes the literal too;"
                        + " read " + OUTPUT_BE + " before trusting either arm");
    }

    /**
     * Both keys the panels are now drawn from, on our side: the title arm above is only worth anything if
     * the key it resolves to actually carries a sentence. A blank row there renders the panel header as its
     * own dotted name.
     */
    @Test
    void bothHatchNamesWeAreTitledByCarryTextInBothLanguages() throws Exception {
        for (String key : List.of("block.neoecoprototype.simplify_fluid_input_hatch",
                "block.neoecoprototype.simplify_fluid_output_hatch")) {
            for (String lang : List.of("en_us", "zh_cn")) {
                var language = JsonParser.parseString(Files.readString(LANG_DIR.resolve(lang + ".json")))
                        .getAsJsonObject();
                assertTrue(language.has(key) && !language.get(key).getAsString().isBlank(),
                        key + " has no usable value in " + lang + ", and on eco 21.2.15 that key is the"
                                + " panel title, not just the item name");
            }
        }
    }

    private static List<String> callsTo(ClassNode node, String owner, String name) {
        var found = new ArrayList<String>();
        node.methods.forEach(method -> {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode call
                        && call.owner.equals(owner) && call.name.equals(name)) {
                    found.add(method.name + method.desc);
                }
            }
        });
        return found;
    }

    private static List<String> ecoOwnTitleKeys(ClassNode node) {
        var found = new ArrayList<String>();
        node.methods.forEach(method -> {
            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof LdcInsnNode constant && constant.cst instanceof String text
                        && text.startsWith("block.neoecoae.")) {
                    found.add(text);
                }
            }
        });
        return found;
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
        var input = FluidHatchPanelTitleContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class");
        assertNotNull(input, "no " + internalName + " on the test classpath - the eco jar is not being"
                + " resolved, so nothing this file says is a reading of upstream");
        return read(input);
    }

    private static ClassNode read(InputStream input) throws Exception {
        var node = new ClassNode();
        new org.objectweb.asm.ClassReader(input).accept(node, Opcodes.ASM9);
        return node;
    }
}
