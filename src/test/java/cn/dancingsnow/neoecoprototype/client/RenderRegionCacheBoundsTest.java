package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

final class RenderRegionCacheBoundsTest {
    private static final String ECO_RENDERER =
            "cn/dancingsnow/neoecoae/client/rendering/FixedBlockEntityRenderers";
    private static final String MIXIN =
            "cn/dancingsnow/neoecoprototype/mixin/FixedBlockEntityRenderersMixin";
    private static final String ACCESSOR =
            "cn/dancingsnow/neoecoprototype/mixin/RenderChunkRegionAccess";
    private static final String BOUNDS =
            "cn/dancingsnow/neoecoprototype/client/RenderRegionCacheBounds";
    private static final ClassLoader CLASSPATH = RenderRegionCacheBoundsTest.class.getClassLoader();

    @Test
    void acceptsTheWholeThreeByThreeCacheEnvelope() {
        assertFalse(RenderRegionCacheBounds.isOutside(-1, -1, -16, -16));
        assertFalse(RenderRegionCacheBounds.isOutside(-1, -1, 0, 0));
        assertFalse(RenderRegionCacheBounds.isOutside(-1, -1, 31, 31));
    }

    @Test
    void rejectsIndicesOutsideTheFlatCacheArray() {
        assertTrue(RenderRegionCacheBounds.isOutside(-1, -1, -80, 0));
        assertTrue(RenderRegionCacheBounds.isOutside(-1, -1, 80, 0));
        assertTrue(RenderRegionCacheBounds.isOutside(-1, -1, 0, -80));
        assertTrue(RenderRegionCacheBounds.isOutside(-1, -1, 0, 80));
        assertTrue(RenderRegionCacheBounds.isOutside(-1, -1, 48, 48));
    }

    @Test
    void preservesRenderChunkRegionIndexAliasingInsteadOfTighteningBehavior() {
        assertFalse(RenderRegionCacheBounds.isOutside(-1, -1, 32, 0),
                "x=32 aliases into cache slot 6 in RenderChunkRegion's flattened index");
        assertFalse(RenderRegionCacheBounds.isOutside(-1, -1, -32, 0),
                "x=-32 aliases into cache slot 2; a per-axis bounds check would change behavior");
    }

    @Test
    void compiledMixinChecksTheRealCacheBeforeTheStandardLookup() throws Exception {
        var upstream = read(ECO_RENDERER);
        var render = method(upstream, "render",
                "(Lnet/neoforged/neoforge/client/event/AddSectionGeometryEvent$SectionRenderingContext;"
                        + "Lnet/minecraft/core/BlockPos;)V");
        long upstreamLookups = calls(render, "net/minecraft/world/level/BlockAndTintGetter",
                "getBlockEntity");
        assertEquals(1, upstreamLookups, "eco's section scan no longer has the single lookup this mixin guards");

        var mixin = read(MIXIN);
        var guard = method(mixin, "neoecoprototype$guardRegionLookup");
        int regionCheck = indexOfType(guard, TypeInsnNode.class,
                node -> node instanceof TypeInsnNode type
                        && type.getOpcode() == Opcodes.INSTANCEOF
                        && Type.getObjectType(type.desc).getInternalName()
                        .equals("net/minecraft/client/renderer/chunk/RenderChunkRegion"));
        int boundsCheck = indexOfCall(guard, BOUNDS, "isOutside");
        int standardLookup = indexOfCall(guard, "net/minecraft/client/renderer/chunk/RenderChunkRegion",
                "getBlockEntity");
        assertTrue(regionCheck >= 0 && boundsCheck > regionCheck && standardLookup > boundsCheck,
                "RenderChunkRegion lookups must pass the cache-index precheck before getBlockEntity");
        assertEquals(0, calls(guard, "net/minecraft/client/renderer/chunk/RenderChunkRegion",
                "getMinBuildHeight"), "Y/build height does not bound the horizontal cache index");

        var accessor = read(ACCESSOR);
        assertEquals(1, accessor.methods.stream()
                .filter(method -> method.name.equals("neoecoprototype$getMinChunkX")).count());
        assertEquals(1, accessor.methods.stream()
                .filter(method -> method.name.equals("neoecoprototype$getMinChunkZ")).count());

        try (InputStream input = CLASSPATH.getResourceAsStream("neoecoprototype.mixins.json")) {
            assertNotNull(input, "neoecoprototype.mixins.json is missing from the test classpath");
            var json = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
            assertTrue(json.getAsJsonArray("client").asList().stream()
                            .anyMatch(entry -> entry.getAsString().equals("RenderChunkRegionAccess")),
                    "the cache-origin accessor is not registered on the physical client");
        }
    }

    private static ClassNode read(String name) throws Exception {
        try (InputStream input = CLASSPATH.getResourceAsStream(name + ".class")) {
            assertNotNull(input, name + " is missing from the test classpath");
            var node = new ClassNode();
            new ClassReader(input).accept(node, Opcodes.ASM9);
            return node;
        }
    }

    private static MethodNode method(ClassNode node, String name) {
        return method(node, name, null);
    }

    private static MethodNode method(ClassNode node, String name, String descriptor) {
        var matches = node.methods.stream()
                .filter(method -> method.name.equals(name)
                        && (descriptor == null || method.desc.equals(descriptor)))
                .toList();
        assertEquals(1, matches.size(), node.name + " must declare exactly one " + name + " method");
        return matches.get(0);
    }

    private static long calls(MethodNode method, String owner, String name) {
        long count = 0;
        for (AbstractInsnNode node : method.instructions) {
            if (node instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name)) {
                count++;
            }
        }
        return count;
    }

    private static int indexOfCall(MethodNode method, String owner, String name) {
        int index = 0;
        for (AbstractInsnNode node : method.instructions) {
            if (node instanceof MethodInsnNode call && call.owner.equals(owner) && call.name.equals(name)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static int indexOfType(MethodNode method, Class<? extends AbstractInsnNode> type,
                                   java.util.function.Predicate<AbstractInsnNode> predicate) {
        int index = 0;
        for (AbstractInsnNode node : method.instructions) {
            if (type.isInstance(node) && predicate.test(node)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
