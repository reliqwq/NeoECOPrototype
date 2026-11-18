package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * What the fluid bucket is built on, read off our own class files.
 *
 * <p>Both rules exist because the failure he saw - a bucket rendered as the purple-black missing sprite -
 * cannot reach the game-test server. {@code IClientFluidTypeExtensions.of(type)} is a plain static map
 * lookup (verified in NeoForge 21.1.251: {@code FLUID_TYPE_EXTENSIONS.getOrDefault(type, DEFAULT)}), and
 * {@code RegisterClientExtensionsEvent} is only ever posted on the client, so on a dedicated server that
 * map is empty and every fluid type answers {@code DEFAULT}, whose {@code getStillTexture()} is
 * {@code aconst_null}. The bucket model bakes that null into its atlas lookup, which is exactly the line
 * the client logs: {@code minecraft:textures/atlas/blocks.png:null}.
 *
 * <p>So the two halves of the wiring are checked where they are actually written - the bytecode of our own
 * client class - and each rule covers every fluid we register, not only the ones that have broken.</p>
 */
final class HandRegisteredFluidTextureContractTest {

    private static final String CLIENT_CLASS = "cn/dancingsnow/neoecoprototype/client/NeoECOPrototypeClient";
    private static final String REGISTRATION_CLASS
            = "cn/dancingsnow/neoecoprototype/registration/ModRegistration";
    /** What javac writes for a {@code Supplier<FluidType>} field - the descriptor alone is untyped. */
    private static final String FLUID_TYPE_SUPPLIER
            = "Ljava/util/function/Supplier<Lnet/neoforged/neoforge/fluids/FluidType;>;";
    private static final String NAMESPACE = "neoecoprototype";
    private static final Path TEXTURES = Path.of("src/main/resources/assets/" + NAMESPACE + "/textures");
    private static final Path BLOCK_ATLAS
            = Path.of("src/main/resources/assets/minecraft/atlases/blocks.json");

    /**
     * A hand-registered {@code FluidType} gets its sprites from nowhere else: eco's own fluids take them
     * from Registrate, ours have to be handed over in {@code onRegisterClientExtensions}. A third fluid
     * added without that call is a silent purple-black bucket, which is what this rule turns into a red.
     */
    @Test
    void everyHandRegisteredFluidTypeIsGivenAClientExtension() throws Exception {
        var fluidFields = new TreeSet<String>();
        for (FieldNode field : ourClass(REGISTRATION_CLASS).fields) {
            if (FLUID_TYPE_SUPPLIER.equals(field.signature)) {
                fluidFields.add(field.name);
            }
        }
        assertFalse(fluidFields.isEmpty(), "no field in ModRegistration carries the signature "
                + FLUID_TYPE_SUPPLIER + " any more, so this rule matched nothing and is not reading our"
                        + " fluids at all. Find out what the fluid suppliers are declared as before"
                        + " trusting a green here.");

        var handedOver = new TreeSet<String>();
        for (MethodNode method : ourClass(CLIENT_CLASS).methods) {
            boolean registersAnExtension = false;
            for (AbstractInsnNode node : method.instructions) {
                if (node instanceof MethodInsnNode call && call.name.equals("registerFluidType")) {
                    registersAnExtension = true;
                    break;
                }
            }
            if (!registersAnExtension) {
                continue;
            }
            for (AbstractInsnNode node : method.instructions) {
                if (node instanceof FieldInsnNode field && field.getOpcode() == Opcodes.GETSTATIC
                        && field.owner.equals(REGISTRATION_CLASS)) {
                    handedOver.add(field.name);
                }
            }
        }

        var missing = new TreeSet<String>(fluidFields);
        missing.removeAll(handedOver);
        assertTrue(missing.isEmpty(), "these hand-registered fluid types are never handed to"
                + " registerFluidType in " + CLIENT_CLASS + ": " + missing
                + ". Their bucket model bakes a null still texture, which shows in game as the missing"
                + " sprite and in the client log as one WARN line - nothing on the server side notices.");
    }

    /**
     * The names the client hands over have to both resolve and be stitch-listed, and this reads them out of
     * {@code <clinit>} rather than repeating them, so a renamed constant cannot drop out. A name may point at
     * another namespace (the powder snow coolant is drawn with vanilla's sprite), which changes only where the
     * png is looked for - the rule stays "it resolves and somebody lists it".
     */
    @Test
    void everyFluidSpriteTheClientNamesShipsAndIsStitchedListed() throws Exception {
        var staticInit = ourClass(CLIENT_CLASS).methods.stream()
                .filter(method -> method.name.equals("<clinit>"))
                .findFirst()
                .orElse(null);
        assertNotNull(staticInit, "NeoECOPrototypeClient no longer has a static initialiser, so the sprite"
                + " names this rule reads are gone - re-read how the fluid extensions get their textures.");

        var sprites = new TreeSet<String>();
        for (AbstractInsnNode node : staticInit.instructions) {
            if (node instanceof LdcInsnNode constant && constant.cst instanceof String text) {
                if (text.startsWith("block/")) {
                    sprites.add(NAMESPACE + ":" + text);
                } else if (text.startsWith("minecraft:block/")) {
                    sprites.add(text);
                }
            }
        }
        assertEquals(4, sprites.size(), "the fluid extensions name four sprites today - the dissolution pair,"
                + " the ice cream block face and vanilla's powder snow face. Found " + sprites
                + ": a sprite the client names has gone unread, so this rule checked less than it thinks.");

        if (!Files.isRegularFile(BLOCK_ATLAS)) {
            fail(BLOCK_ATLAS + " is not there, so no fluid sprite can be stitched into the block atlas.");
        }
        String stitched = Files.readString(BLOCK_ATLAS);
        for (String sprite : sprites) {
            String namespace = sprite.substring(0, sprite.indexOf(':'));
            String path = sprite.substring(sprite.indexOf(':') + 1);
            if (NAMESPACE.equals(namespace)) {
                Path png = TEXTURES.resolve(path + ".png");
                if (!Files.isRegularFile(png)) {
                    fail(png + " does not exist. The client hands " + sprite
                            + " to the atlas lookup, and a name with no file behind it is the purple-black bucket.");
                }
                if (Files.size(png) == 0) {
                    fail(png + " is empty, which reaches the screen the same way as no file at all.");
                }
            } else {
                var url = HandRegisteredFluidTextureContractTest.class.getResource(
                        "/assets/" + namespace + "/textures/" + path + ".png");
                assertNotNull(url, sprite + " is handed to the atlas lookup by the client but no jar on the"
                        + " classpath carries it - the reference is not a copy, so it has to be there at runtime.");
            }
            if (!stitched.contains(sprite)) {
                fail(sprite + " is handed to the block atlas by the client but listed by no blocks.json source"
                        + " of ours, so nothing guarantees the stitch ever sees it.");
            }
        }
    }

    /** Our own compiled classes, from whatever the test JVM was pointed at. */
    private static ClassNode ourClass(String internalName) throws Exception {
        try (InputStream input = HandRegisteredFluidTextureContractTest.class.getClassLoader()
                .getResourceAsStream(internalName + ".class")) {
            assertNotNull(input, "no " + internalName + " on the test classpath, so this file read nothing."
                    + " Compile main before running test.");
            var node = new ClassNode();
            new org.objectweb.asm.ClassReader(input).accept(node, 0);
            return node;
        }
    }
}
