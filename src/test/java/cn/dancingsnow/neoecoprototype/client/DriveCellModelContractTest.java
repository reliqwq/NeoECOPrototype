package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * The drive slot's cell model has to be baked before eco asks for it, and the only thing that bakes it is the
 * {@code RegisterAdditional} list in this same class. Two literals therefore have to stay in step: the names
 * {@code onClientSetup} hands to {@code ECOCellModels.register}, and {@code DRIVE_CELL_MODELS}. Ten of them
 * were handed over without ever being registered - the singularity matrix among them, which is the purple-black
 * cube a player reported on 2026-11-15 - and the item itself looks correct, because its hand model is a separate
 * generated one, so nothing but this comparison speaks.
 *
 * <p>Why a static check rather than a game test: the game-test server never loads a renderer, so a missing
 * baked model cannot be observed there at any price. The scan is asserted to be non-vacuous (it has to find
 * both literals and a real number of names), because a regex that silently matches nothing would also be green.
 */
final class DriveCellModelContractTest {
    private static final Path CLIENT_SOURCE =
            Path.of("src/main/java/cn/dancingsnow/neoecoprototype/client/NeoECOPrototypeClient.java");
    private static final Path ASSETS = Path.of("src/main/resources/assets/neoecoprototype");
    private static final Pattern USED = Pattern.compile("cellModel\\(\"([a-z0-9_]+)\"\\)");
    private static final Pattern QUOTED = Pattern.compile("\"([a-z0-9_]+)\"");

    /** The test task runs from the project directory, the same way MusicRecorderResourcesTest reads assets. */
    private static String clientSource() throws Exception {
        assertTrue(Files.isRegularFile(CLIENT_SOURCE), "no " + CLIENT_SOURCE + " from the working directory "
                + Path.of(".").toAbsolutePath() + "; this guard reads the source, so it must not be moved");
        return Files.readString(CLIENT_SOURCE, StandardCharsets.UTF_8);
    }

    /** Every name {@code cellModel("...")} is asked for anywhere in the class. */
    private static Set<String> handedToEco(String source) {
        var names = new LinkedHashSet<String>();
        Matcher matched = USED.matcher(source);
        while (matched.find()) {
            names.add(matched.group(1));
        }
        return names;
    }

    /** Every name inside the {@code DRIVE_CELL_MODELS} literal. */
    private static Set<String> registeredAsAdditional(String source) {
        int start = source.indexOf("DRIVE_CELL_MODELS = java.util.List.of(");
        assertTrue(start >= 0, "the DRIVE_CELL_MODELS literal moved or changed shape; this guard would"
                + " otherwise pass while checking nothing");
        int end = source.indexOf(");", start);
        assertTrue(end > start, "the DRIVE_CELL_MODELS literal has no closing bracket");
        var names = new LinkedHashSet<String>();
        Matcher matched = QUOTED.matcher(source.substring(start, end));
        while (matched.find()) {
            names.add(matched.group(1));
        }
        return names;
    }

    @Test
    void everyCellModelHandedToEcoIsAlsoRegisteredAsAnAdditionalModel() throws Exception {
        var source = clientSource();
        var handed = handedToEco(source);
        var registered = registeredAsAdditional(source);
        assertTrue(handed.size() >= 30, "the scan found only " + handed.size()
                + " cellModel(...) names, so the regex no longer matches how this class is written");
        assertTrue(registered.size() >= handed.size(), "the registration list (" + registered.size()
                + ") is shorter than the set handed to eco (" + handed.size() + ")");
        var missing = new ArrayList<>(handed);
        missing.removeAll(registered);
        assertTrue(missing.isEmpty(), "these drive cell models are handed to eco but never registered as"
                + " additional models, so a drive draws the missing-texture cube for them: " + missing
                + " - add each one to DRIVE_CELL_MODELS (the 2026-11-15 singularity-matrix report was exactly"
                + " this, and the item's own model looked fine)");
    }

    @Test
    void everyRegisteredDriveModelResolvesItsModelAndTextures() throws Exception {
        var source = clientSource();
        var registered = registeredAsAdditional(source);
        assertTrue(registered.size() >= 30, "the scan found only " + registered.size()
                + " names in DRIVE_CELL_MODELS, so the regex no longer matches how this class is written");
        var problems = new ArrayList<String>();
        for (var name : registered) {
            resolve("neoecoprototype:block/cell/storage_cell_" + name, new LinkedHashSet<>(), problems);
        }
        assertTrue(problems.isEmpty(), "unresolved model references in the drive cell set: " + problems);
    }

    /** Follows parents inside our own namespace and checks every texture reference on the classpath. */
    private static void resolve(String reference, Set<String> seen, List<String> problems) {
        if (!seen.add(reference)) {
            return;
        }
        String[] split = reference.split(":", 2);
        String namespace = split.length == 2 ? split[0] : "neoecoprototype";
        String path = split.length == 2 ? split[1] : split[0];
        if (!"neoecoprototype".equals(namespace)) {
            return; // upstream's own model, and upstream ships its own textures
        }
        var model = asset("models/" + path + ".json");
        assertTrue(Files.isRegularFile(model), "no model file for " + reference + " at " + model);
        JsonObject data = JsonParser.parseString(read(model)).getAsJsonObject();
        JsonElement parent = data.get("parent");
        if (parent != null && parent.isJsonPrimitive()) {
            resolve(parent.getAsString(), seen, problems);
        }
        JsonElement textures = data.get("textures");
        if (textures != null && textures.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : textures.getAsJsonObject().entrySet()) {
                if (!entry.getValue().isJsonPrimitive()) {
                    continue;
                }
                String texture = entry.getValue().getAsString();
                if (texture.startsWith("#")) {
                    continue;
                }
                int colon = texture.indexOf(':');
                String textureNamespace = colon < 0 ? "neoecoprototype" : texture.substring(0, colon);
                String texturePath = colon < 0 ? texture : texture.substring(colon + 1);
                if (!"neoecoprototype".equals(textureNamespace)) {
                    continue; // upstream ships its own texture, and CI forbids us from naming it in assets
                }
                if (!Files.isRegularFile(asset("textures/" + texturePath + ".png"))) {
                    problems.add(reference + " -> " + entry.getKey() + " = " + texture + " (no png)");
                }
            }
        }
    }

    /** {@code insideAssets} is relative to our own asset namespace, e.g. {@code models/block/cell/x.json}. */
    private static Path asset(String insideAssets) {
        return ASSETS.resolve(insideAssets.replace("/", java.io.File.separator));
    }

    private static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            throw new IllegalStateException(failure);
        }
    }

    @Test
    void theScanSeesTheSingularityTileItWasWrittenFor() throws Exception {
        var source = clientSource();
        assertTrue(handedToEco(source).contains("l1_singularity"),
                "the singularity cell no longer asks for cellModel(\"l1_singularity\") - re-read the"
                        + " registration list before believing this file's story");
        assertTrue(registeredAsAdditional(source).contains("l1_singularity"),
                "l1_singularity dropped out of DRIVE_CELL_MODELS again");
    }

    @Test
    void theClientSourceIsReadableForTheseChecks() throws Exception {
        assertNotNull(clientSource());
        assertTrue(clientSource().contains("ECOCellModels.register"));
    }
}
