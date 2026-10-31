package cn.dancingsnow.neoecoprototype.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A player who has this addon on the client and joins a server without it never receives the SERVER config:
 * NeoForge only fills the loaded handle from the server's {@code ConfigSync} task, so {@code get()} throws
 * {@code IllegalStateException: Cannot get config value before config is loaded} from whatever runs next. The
 * 1.4.3 report put the throw inside {@code LevelTickEvent.Post} and {@code ClientChunkCache
 * .replaceWithPacketData}, which killed the client on joining.
 *
 * <p>A plain JVM is exactly that state - nothing here registers the spec - so the crash condition reproduces
 * as a unit test instead of needing a second-hand server.</p>
 */
class ServerConfigBeforeLoadedTest {

    /** Files whose every read sits behind a {@code ServerLevel} parameter or a client-side early return. */
    private static final Set<String> SERVER_ONLY_BY_CONSTRUCTION = Set.of(
            "config/NeoECOPrototypeServerConfig.java",          // the definitions themselves
            "event/WirelessLinks.java",                          // every method takes ServerLevel
            "items/SimplifyStructureExpansionCardItem.java",     // handOverTicket(ServerLevel, ...)
            "blockentity/crafting/SimplifyCraftingSystemBlockEntity.java", // applyMindMood returns early on client
            "worldgen/ConfigGatedOreFeature.java",               // feature runs during generation
            "worldgen/CryotheumMeteoriteStructure.java",         // structure enable check, server side
            "gametest/NeoECOPrototypeGameTests.java");          // the suite asserts the loaded values

    @Test
    void rawReadBeforeLoadThrowsTheUpstreamMessage() {
        assertFalse(NeoECOPrototypeServerConfig.SPEC.isLoaded(),
                "expected an unloaded spec in a plain JVM - that is the joining-a-foreign-server state");
        IllegalStateException failure = assertThrows(IllegalStateException.class,
                NeoECOPrototypeServerConfig.WIRELESS_LINKS::get);
        assertTrue(failure.getMessage().contains("before config is loaded"),
                () -> "expected the upstream precondition message, got: " + failure.getMessage());
    }

    @Test
    void currentAnswersTheShippedDefaultInsteadOfThrowing() {
        assertEquals(NeoECOPrototypeServerConfig.WIRELESS_LINKS.getDefault(),
                NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.WIRELESS_LINKS),
                "a boolean key must answer its own default while unloaded");
        assertEquals(NeoECOPrototypeServerConfig.CRYOTHEUM_ORE_FREEZE_TICKS.getDefault(),
                NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.CRYOTHEUM_ORE_FREEZE_TICKS),
                "an int key must answer its own default while unloaded");
        assertEquals(NeoECOPrototypeServerConfig.L1_CPU_TOTAL_BYTES.getDefault(),
                NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.L1_CPU_TOTAL_BYTES),
                "a long key must answer its own default while unloaded");
    }

    /**
     * The textual half of the fix: a client-reachable file must not read a raw value. Scanning the source
     * rather than the bytecode because the property is "which file is this line in", and the read is legal
     * the moment the enclosing method takes a ServerLevel.
     */
    @Test
    void noClientReachableFileReadsARawServerConfigValue() throws IOException {
        Path sourceRoot = mainSourceRoot();
        Pattern raw = Pattern.compile(
                "NeoECOPrototypeServerConfig\\s*\\n?\\s*\\.\\s*([A-Z0-9_]+)\\s*\\.\\s*get\\s*\\(\\s*\\)");
        List<String> offenders = new ArrayList<>();
        List<String> scanned = new ArrayList<>();
        try (var paths = Files.walk(sourceRoot)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                String relative = sourceRoot.relativize(path).toString()
                        .replace(java.io.File.separatorChar, '/');
                scanned.add(relative);
                boolean serverOnly = SERVER_ONLY_BY_CONSTRUCTION.stream().anyMatch(relative::endsWith);
                if (serverOnly) {
                    continue;
                }
                Matcher matcher = raw.matcher(Files.readString(path));
                while (matcher.find()) {
                    offenders.add(relative + " -> " + matcher.group(1));
                }
            }
        }
        assertTrue(scanned.size() > 100,
                () -> "the scan found only " + scanned.size() + " java files - wrong source root, so this "
                        + "guard proves nothing: " + sourceRoot);
        assertEquals(List.of(), offenders,
                () -> "these client-reachable files still read a raw SERVER value and will throw for a player "
                        + "who joins a server without this addon, expected NeoECOPrototypeServerConfig.current(...): "
                        + offenders);
    }

    private static Path mainSourceRoot() {
        Path cursor = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int depth = 0; depth < 4 && cursor != null; depth++, cursor = cursor.getParent()) {
            Path candidate = cursor.resolve("src").resolve("main").resolve("java");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("no src/main/java under " + System.getProperty("user.dir")
                + " - the guard cannot scan, so it must fail rather than pass quietly");
    }
}
