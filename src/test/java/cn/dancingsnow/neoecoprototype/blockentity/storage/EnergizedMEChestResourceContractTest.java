package cn.dancingsnow.neoecoprototype.blockentity.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

final class EnergizedMEChestResourceContractTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/neoecoprototype");
    private static final Path DATA = Path.of("src/main/resources/data");
    private static final Path AE2_JAR = Path.of("libs/ae2-19.2.17.jar");
    private static final Path ECO_JAR = Path.of("neoecobeta/neoecoae-21.3.0.jar");
    private static final String ID = "neoecoprototype:energized_me_chest";
    private static final Set<String> STATES = Set.of("off", "absent", "empty", "partial", "types_full", "full");
    private static final Map<String, String> FINAL_ART_SHA256 = Map.ofEntries(
            Map.entry("textures/block/ec1_me_chest/back.png",
                    "361c486f15a6b85a9ac53e313598657e4b201bcecfda89fd5ae5ca885d4512b8"),
            Map.entry("textures/block/ec1_me_chest/bottom.png",
                    "ecb2cee5ead9c1303a6ecd65adce2110974ac488b3eedcd30f370af7ebc6ac42"),
            Map.entry("textures/block/ec1_me_chest/details.png",
                    "d52398e66e0b7eb10476f4e2f14dc8c082ce6cfae6ded62302efe40a9f156b22"),
            Map.entry("textures/block/ec1_me_chest/front_eco_drive.png",
                    "ad86c14e64c96bacdb098c3fdbbb924d7367599a6e9a7dbd75c7868232476742"),
            Map.entry("textures/block/ec1_me_chest/front_eco_drive_on.png",
                    "8354698ad15c4fb084bc0f24eac03cc541693a71258cfd2d0decaa06d4661ff2"),
            Map.entry("textures/block/ec1_me_chest/side.png",
                    "b71662efbe7b88a693ff8efd5e1c20eabe12b4bd1aad758e2b454b4b53f53a1f"),
            Map.entry("textures/block/ec1_me_chest/top.png",
                    "8e3ea4563f9424914262177785bb19612e38bf2b0be080f419818ce9ff30d4c1"),
            Map.entry("textures/gui/energized_me_chest.png",
                    "7c101a6d55cd0ca5e59ce4c6c922d04b6491d8065de7592643dc73e5459a36b6"));

    @Test
    void finalHandoffArtMatchesPinnedBytes() throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        HexFormat hex = HexFormat.of();
        for (var entry : FINAL_ART_SHA256.entrySet()) {
            Path asset = ASSETS.resolve(entry.getKey());
            byte[] bytes = Files.readAllBytes(asset);
            assertEquals(entry.getValue(), hex.formatHex(sha256.digest(bytes)), asset.toString());
        }
    }

    @Test
    void chestDisplayNamesMatchTheirTiers() throws Exception {
        JsonObject chinese = json(ASSETS.resolve("lang/zh_cn.json"));
        assertEquals("盈能 ME箱子", chinese.get("block.neoecoprototype.energized_me_chest").getAsString());
        assertEquals("ECO-L1 ME箱子", chinese.get("block.neoecoprototype.ec1_me_chest").getAsString());
        JsonObject english = json(ASSETS.resolve("lang/en_us.json"));
        assertEquals("Energized ME Chest", english.get("block.neoecoprototype.energized_me_chest").getAsString());
        assertEquals("ECO-L1 ME Chest", english.get("block.neoecoprototype.ec1_me_chest").getAsString());
    }

    @Test
    void sideGuiUsesAe2MEChestStyleAndMatrixIcon() throws Exception {
        JsonObject style;
        try (InputStream input = resource("/assets/ae2/screens/me_chest.json")) {
            style = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
        }
        assertEquals("guis/me_chest.png", style.getAsJsonObject("background").get("texture").getAsString());
        assertEquals("common/common.json", style.getAsJsonArray("includes").get(0).getAsString());
        assertEquals("common/player_inventory.json", style.getAsJsonArray("includes").get(1).getAsString());
        assertEquals(1, style.getAsJsonObject("slots").size());
        assertTrue(style.getAsJsonObject("slots").has("STORAGE_CELL"));
        assertEquals(80, style.getAsJsonObject("slots").getAsJsonObject("STORAGE_CELL").get("left").getAsInt());
        assertEquals(37, style.getAsJsonObject("slots").getAsJsonObject("STORAGE_CELL").get("top").getAsInt());
        assertTrue(style.getAsJsonObject("widgets").has("openPriority"));

        try (InputStream input = resource("/assets/ae2/textures/guis/me_chest.png")) {
            BufferedImage image = ImageIO.read(input);
            assertNotNull(image, "the AE2 ME chest GUI texture did not decode");
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
        }

        JsonObject localStyle = json(ASSETS.resolve("screens/energized_me_chest.json"));
        assertEquals("neoecoprototype:textures/gui/energized_me_chest.png",
                localStyle.getAsJsonObject("background").get("texture").getAsString());
        var srcRect = localStyle.getAsJsonObject("background").getAsJsonArray("srcRect");
        assertEquals(List.of(19, 0, 182, 168), List.of(srcRect.get(0).getAsInt(),
                srcRect.get(1).getAsInt(), srcRect.get(2).getAsInt(), srcRect.get(3).getAsInt()));
        assertFalse(localStyle.has("includes"), "the locally loaded style must be self-contained");

        JsonObject palette;
        try (InputStream input = resource("/assets/ae2/screens/common/palette.json")) {
            palette = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
        assertEquals(palette.getAsJsonObject("palette"), localStyle.getAsJsonObject("palette"));

        JsonObject playerInventory;
        try (InputStream input = resource("/assets/ae2/screens/common/player_inventory.json")) {
            playerInventory = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
        JsonObject localSlots = localStyle.getAsJsonObject("slots");
        JsonObject inventorySlot = localSlots.getAsJsonObject("PLAYER_INVENTORY");
        assertEquals(9, inventorySlot.get("left").getAsInt(),
                "the inventory must keep its source x=28 position in the 19-pixel-cropped panel");
        assertEquals(84, inventorySlot.get("bottom").getAsInt());
        assertEquals(playerInventory.getAsJsonObject("slots").getAsJsonObject("PLAYER_INVENTORY")
                        .get("grid").getAsString(),
                inventorySlot.get("grid").getAsString());
        JsonObject hotbarSlot = localSlots.getAsJsonObject("PLAYER_HOTBAR");
        assertEquals(9, hotbarSlot.get("left").getAsInt(),
                "the hotbar must keep the same source position as the inventory");
        assertEquals(26, hotbarSlot.get("bottom").getAsInt());
        assertEquals(playerInventory.getAsJsonObject("slots").getAsJsonObject("PLAYER_HOTBAR")
                        .get("grid").getAsString(),
                hotbarSlot.get("grid").getAsString());
        JsonObject inventoryTitle = localStyle.getAsJsonObject("text").getAsJsonObject("player_inventory_title");
        assertEquals(playerInventory.getAsJsonObject("text").getAsJsonObject("player_inventory_title")
                        .get("text"),
                inventoryTitle.get("text"));
        assertEquals(7, inventoryTitle.getAsJsonObject("position").get("left").getAsInt(),
                "the inventory label must start at source x=26 in the 19-pixel-cropped panel");
        assertEquals(95, inventoryTitle.getAsJsonObject("position").get("bottom").getAsInt());
        assertEquals("block.neoecoprototype.energized_me_chest",
                localStyle.getAsJsonObject("text").getAsJsonObject("dialog_title")
                        .getAsJsonObject("text").get("translate").getAsString());
        assertEquals("LEFT", localStyle.getAsJsonObject("text").getAsJsonObject("dialog_title")
                .get("align").getAsString(), "the chest title must align with the inventory title");

        assertFalse(localStyle.getAsJsonObject("widgets").has("verticalToolbar"),
                "the hand-drawn buttons replace AE2's vertical toolbar");
        JsonObject storageCell = localSlots.getAsJsonObject("STORAGE_CELL");
        assertEquals(80, storageCell.get("left").getAsInt(),
                "the matrix slot must start at source x=99 in the 19-pixel-cropped panel");
        assertEquals(37, storageCell.get("top").getAsInt(),
                "the matrix slot must start at source y=37");
        JsonObject priority = localStyle.getAsJsonObject("widgets").getAsJsonObject("openPriority");
        assertEquals(158, priority.get("left").getAsInt(),
                "the priority button must keep source x=177 in the 19-pixel-cropped panel");
        assertEquals(-5, priority.get("top").getAsInt());
        assertEquals(20, priority.get("width").getAsInt());
        assertEquals(20, priority.get("height").getAsInt());

        String constants = constants("cn/dancingsnow/neoecoprototype/client/EnergizedMEChestScreen.class");
        assertTrue(constants.contains("/assets/neoecoprototype/screens/energized_me_chest.json"));
        assertTrue(constants.contains("appeng/client/gui/style/ScreenStyle"));
        assertFalse(constants.contains("appeng/client/gui/style/StyleManager"));
        assertTrue(constants.contains("addOpenPriorityButton"),
                "the JSON styles the priority button; the constructor must create it exactly once");
        String screenSource = Files.readString(Path.of(
                "src/main/java/cn/dancingsnow/neoecoprototype/client/EnergizedMEChestScreen.java"));
        int priorityButtonCreation = screenSource.indexOf("widgets.addOpenPriorityButton();");
        assertEquals(priorityButtonCreation, screenSource.lastIndexOf("widgets.addOpenPriorityButton();"),
                "the priority button must be created exactly once");
        assertTrue(screenSource.contains("dialogTitle.setAlign(TextAlignment.LEFT)"),
                "the title must be left-aligned in code, not only in a reloadable style file");
        assertTrue(screenSource.contains("titlePosition.setLeft(14)"),
                "the title must keep its explicit left position");
        assertTrue(constants.contains("energized_me_chest.png"));
        assertTrue(constants.contains("ToolbarButton"));
        assertFalse(constants.contains("renderFakeItem"));
        assertFalse(constants.contains("SIMPLIFY_ITEM_CELL_1K"));
        assertTrue(constants.contains("shouldAddToolbar"));
        assertTrue(constants.contains("addRenderableWidget"));
        assertTrue(constants.contains("sendCellWorkbenchRequest"));
        assertTrue(constants.contains("gui.neoecoprototype.energized_me_chest.configure"));
        assertFalse(constants.contains("energized_me_chest_toolbar_slot.png"));
        assertFalse(constants.contains("energized_me_chest_toolbar_buttons.png"));
        assertFalse(constants.contains("energized_me_chest_cell_slot.png"));
        assertFalse(constants.contains("energized_me_chest_guide_button.png"));
        assertTrue(constants.contains("energized_me_chest.md"));
        assertTrue(constants.contains("getHelpTopic"));
        assertTrue(constants.contains("GuidesCommon"));
        assertTrue(constants.contains("PageAnchor"));
        String toolbarButton = constants(
                "cn/dancingsnow/neoecoprototype/client/EnergizedMEChestScreen$ToolbarButton.class");
        assertTrue(toolbarButton.contains("setHeight"));
        assertTrue(toolbarButton.contains("renderWidget"));
        assertTrue(toolbarButton.contains("blit"));
        assertFalse(constants.contains("/screens/me_chest.json"));

        String menu = constants("cn/dancingsnow/neoecoprototype/menu/EnergizedMEChestMenu.class");
        assertTrue(menu.contains("open_cell_workbench"));
        assertTrue(menu.contains("openCellConfiguration"));
        String blockEntity = constants(
                "cn/dancingsnow/neoecoprototype/blockentity/storage/EnergizedMEChestBlockEntity.class");
        assertTrue(blockEntity.contains("IECOConfigurableCellHost"));
        assertTrue(blockEntity.contains("IECOCellWorkbenchDrive"));
        assertTrue(blockEntity.contains("reloadCellConfiguration"));
        assertTrue(blockEntity.contains("returnToCellWorkbenchHost"));
        assertTrue(blockEntity.contains("writeToStream"));
        assertTrue(blockEntity.contains("readFromStream"));
        assertTrue(blockEntity.contains("net/minecraft/core/registries/BuiltInRegistries"));
    }

    @Test
    void clientDisplayUpdatesNeverReenterServerCellFactories() throws Exception {
        String owner = "cn/dancingsnow/neoecoprototype/blockentity/storage/EnergizedMEChestBlockEntity";
        ClassNode chest = ourClass(owner);
        MethodNode updateHandler = method(chest, "updateHandler");
        int clientGuard = instructionOfCall(updateHandler, "isClientSide");
        assertTrue(clientGuard >= 0, "updateHandler must reject client-side cell construction");
        for (String factory : List.of("appeng/api/storage/StorageCells", "cn/dancingsnow/neoecoae/api/storage/ECOStorageCells")) {
            int factoryCall = instructionOfCall(updateHandler, factory, "getCellInventory");
            assertTrue(factoryCall < 0 || clientGuard < factoryCall,
                    factory + " is reached before the client-side guard");
        }

        MethodNode updateDisplay = method(chest, "updateDisplay");
        assertTrue(instructionOfCall(updateDisplay, "isClientSide") >= 0,
                "client display updates must leave synchronization to the server");
        for (String callback : List.of("getDisplayState", "getCurrentCell", "getCellStatus", "updateHandler")) {
            assertFalse(instructionOfCall(updateDisplay, callback) >= 0,
                    "updateDisplay re-enters " + callback + " and can recurse when an Omni inventory is client-null");
        }
        assertTrue(readsField(updateDisplay, owner, "currentCell"),
                "display updates must read the already-resolved cell rather than lazily resolving it again");
        assertTrue(instructionOfCall(updateDisplay, "getStatus") >= 0,
                "display updates no longer read the resolved cell status");

        MethodNode getDisplayState = method(chest, "getDisplayState");
        assertTrue(instructionOfCall(getDisplayState, "isClientSide") >= 0,
                "getDisplayState must distinguish the physical client");
        assertTrue(instructionOfCall(getDisplayState, "getBlockState") >= 0,
                "the client display state must come from synchronized block state");
        assertTrue(readsField(getDisplayState,
                        "cn/dancingsnow/neoecoprototype/block/storage/EnergizedMEChestBlock", "CELL_STATE"),
                "the client display state no longer reads CELL_STATE");
    }

    @Test
    void clientCellRenderingReadsTheSyncedCellEveryFrame() throws Exception {
        String chestOwner = "cn/dancingsnow/neoecoprototype/blockentity/storage/EnergizedMEChestBlockEntity";
        String rendererOwner = "cn/dancingsnow/neoecoprototype/client/renderer/blockentity/EnergizedMEChestRenderer";
        ClassNode chest = ourClass(chestOwner);
        MethodNode readFromStream = method(chest, "readFromStream");
        assertTrue(instructionOfCall(readFromStream, "appeng/util/inv/AppEngInternalInventory", "setItemDirect") >= 0,
                "a synchronized cell change must update the client inventory read by the renderer");
        assertEquals(-1, instructionOfCall(readFromStream,
                        "net/minecraft/client/renderer/LevelRenderer", "setBlocksDirty"),
                "the chest must not depend on rebuilding a fixed-renderer section mesh");
        assertTrue(instructionOfCall(method(chest, "onChangeInventory"), chestOwner, "markForUpdate") >= 0,
                "changing the installed cell must still synchronize the block entity");

        ClassNode renderer = ourClass(rendererOwner);
        assertFalse(renderer.interfaces.contains(
                        "cn/dancingsnow/neoecoae/client/rendering/IFixedBlockEntityRenderer"),
                "the chest matrix must not be baked into section geometry");
        MethodNode render = method(renderer, "render");
        assertTrue(instructionOfCall(render, rendererOwner, "renderCell") >= 0,
                "the dynamic block-entity renderer must draw the current cell every frame");
        MethodNode renderCell = method(renderer, "renderCell");
        assertTrue(instructionOfCall(renderCell, chestOwner, "getCellStack") >= 0,
                "dynamic cell rendering must read the synchronized cell stack");
        assertTrue(instructionOfCall(renderCell, rendererOwner, "modelFor") >= 0,
                "dynamic cell rendering must choose the matrix or fallback model");
        assertTrue(instructionOfCall(renderCell, rendererOwner, "tessellateModel") >= 0,
                "dynamic cell rendering must tessellate the selected model");
    }

    @Test
    void everyFacingAndCellStateUsesEcoDriveCavityGeometry() throws Exception {
        JsonObject blockstates = json(ASSETS.resolve("blockstates/energized_me_chest.json"));
        var parts = blockstates.getAsJsonArray("multipart");
        assertEquals(24, parts.size(), "each facing/state needs exactly one drive model");
        Map<String, Integer> coveredStates = new java.util.HashMap<>();
        Map<String, Integer> rotations = Map.of("north", 0, "east", 90, "south", 180, "west", 270);
        for (var part : parts) {
            JsonObject apply = part.getAsJsonObject().getAsJsonObject("apply");
            JsonObject when = part.getAsJsonObject().getAsJsonObject("when");
            String facing = when.get("facing").getAsString();
            String state = when.get("cell_state").getAsString();
            String model = apply.get("model").getAsString();
            assertEquals("neoecoprototype:block/"
                            + (Set.of("off", "absent").contains(state) ? "energized_me_chest_off" : "energized_me_chest_base"),
                    model);
            assertEquals(rotations.get(facing).intValue(), apply.has("y") ? apply.get("y").getAsInt() : 0, model);
            coveredStates.merge(facing + "," + state, 1, Integer::sum);
        }
        assertEquals(24, coveredStates.size());
        for (int count : coveredStates.values()) {
            assertEquals(1, count, "a state must select exactly one model");
        }

        JsonObject base = json(ASSETS.resolve("models/block/energized_me_chest_base.json"));
        assertEquals("block/block", base.get("parent").getAsString());
        assertEquals("cutout", base.get("render_type").getAsString());
        JsonObject baseTextures = base.getAsJsonObject("textures");
        assertEquals(Set.of("2", "3", "4", "top", "bottom", "side", "back", "details", "particle"),
                baseTextures.keySet());
        assertEquals("neoecoprototype:block/energized_me_chest/front_eco_drive_on",
                baseTextures.get("2").getAsString());
        assertEquals("neoecoprototype:block/storage_recolor/drive/drive_inside_top_bottom",
                baseTextures.get("3").getAsString());
        assertEquals("neoecoprototype:block/storage_recolor/drive/drive_inside",
                baseTextures.get("4").getAsString());
        for (String slot : Set.of("top", "bottom", "side", "back", "details")) {
            assertEquals("neoecoprototype:block/energized_me_chest/" + slot,
                    baseTextures.get(slot).getAsString());
        }
        assertFalse(baseTextures.has("light"), "the chest must not add an energized status-light overlay");

        JsonObject upstreamDrive;
        try (InputStream input = resource("/assets/neoecoae/models/block/eco_drive_empty.json")) {
            upstreamDrive = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
        var upstreamElements = upstreamDrive.getAsJsonArray("elements");
        var chestElements = base.getAsJsonArray("elements");
        assertEquals(upstreamElements.size(), chestElements.size(),
                "the chest must use eco's shell without an extra energized status-light face");
        for (int index = 0; index < upstreamElements.size(); index++) {
            JsonObject upstreamElement = upstreamElements.get(index).getAsJsonObject();
            JsonObject chestElement = chestElements.get(index).getAsJsonObject();
            assertEquals(upstreamElement.get("name"), chestElement.get("name"));
            assertEquals(upstreamElement.get("from"), chestElement.get("from"),
                    "element " + index + " must keep eco's bounds");
            assertEquals(upstreamElement.get("to"), chestElement.get("to"),
                    "element " + index + " must keep eco's bounds");
            var upstreamFaces = upstreamElement.getAsJsonObject("faces");
            var chestFaces = chestElement.getAsJsonObject("faces");
            assertEquals(upstreamFaces.keySet(), chestFaces.keySet(),
                    "element " + index + " must keep eco's faces");
            for (var upstreamFaceEntry : upstreamFaces.entrySet()) {
                JsonObject upstreamFace = upstreamFaceEntry.getValue().getAsJsonObject();
                JsonObject chestFace = chestFaces.get(upstreamFaceEntry.getKey()).getAsJsonObject();
                if ("#2".equals(upstreamFace.get("texture").getAsString())) {
                    assertEquals(upstreamFace, chestFace,
                            "front element " + index + " must keep eco's UVs, texture slot, cullface and shade");
                    continue;
                }
                String externalSlot = externalSlot(chestElement, upstreamFaceEntry.getKey(), chestFace);
                if (externalSlot != null) {
                    assertEquals(externalSlot, chestFace.get("texture").getAsString(),
                            "external face " + index + "/" + upstreamFaceEntry.getKey() + " uses the wrong texture");
                    assertEquals(externalUv(chestElement, upstreamFaceEntry.getKey()), uv(chestFace),
                            "external face " + index + "/" + upstreamFaceEntry.getKey() + " is misaligned");
                }
            }
        }

        JsonObject off = json(ASSETS.resolve("models/block/energized_me_chest_off.json"));
        assertEquals("neoecoprototype:block/energized_me_chest_base", off.get("parent").getAsString());
        assertEquals(Set.of("2"), off.getAsJsonObject("textures").keySet());
        assertEquals("neoecoprototype:block/energized_me_chest/front_eco_drive",
                off.getAsJsonObject("textures").get("2").getAsString());
        String block = Files.readString(Path.of(
                "src/main/java/cn/dancingsnow/neoecoprototype/block/storage/EnergizedMEChestBlock.java"));
        assertTrue(block.contains(".noOcclusion()"), "the open drive cavity must not be culled as a full cube");
        JsonObject itemModel = json(ASSETS.resolve("models/item/energized_me_chest.json"));
        assertEquals("neoecoprototype:block/energized_me_chest_off", itemModel.get("parent").getAsString());

        assertFalse(Files.exists(ASSETS.resolve("models/block/cell/storage_cell_ae2_plain.json")),
                "AE2 cells must use their official item-cell models");
    }

    @Test
    void returnedExteriorAndGuiTexturesKeepTheirHandoffSizes() throws Exception {
        for (String texture : Set.of("front_eco_drive", "top", "side", "back", "bottom", "details")) {
            assertImageSize(ASSETS.resolve("textures/block/energized_me_chest/" + texture + ".png"),
                    16, 16, texture);
        }
        assertImageSize(ASSETS.resolve("textures/gui/energized_me_chest.png"), 256, 256, "GUI");
        try (InputStream input = Files.newInputStream(ASSETS.resolve("textures/gui/energized_me_chest.png"))) {
            BufferedImage gui = ImageIO.read(input);
            assertNotNull(gui, "GUI did not decode");
            for (int[] pixel : List.of(new int[]{3, 4}, new int[]{20, 23}, new int[]{208, 0},
                    new int[]{3, 26}, new int[]{24, 45}, new int[]{208, 32},
                    new int[]{98, 36}, new int[]{115, 53})) {
                assertEquals(255, gui.getRGB(pixel[0], pixel[1]) >>> 24,
                        "missing button frame at " + pixel[0] + "," + pixel[1]);
            }
        }
        String screen = Files.readString(Path.of(
                "src/main/java/cn/dancingsnow/neoecoprototype/client/EnergizedMEChestScreen.java"));
        assertTrue(screen.contains("guideButton.setPosition(leftPos - 19, topPos)"),
                "the guide button must align with the supplied GUI toolbar");
        assertTrue(screen.contains("cellWorkbenchButton.setPosition(leftPos - 19, topPos + 25)"),
                "the workbench button must align with the supplied GUI toolbar");
        assertTrue(screen.contains("new ToolbarButton(Icon.HELP, 0, 25, 0, 4, 18,"),
                "the question button must use the supplied normal and hover rows");
        assertTrue(screen.contains("new ToolbarButton(Icon.COG, 25, 27, 32, 1, 22,"),
                "the gear hover must replace source x=3..24, y=26..45 in place");
        assertTrue(screen.contains("private static final int TEXTURE_WIDTH = 25;"),
                "the toolbar blit must include the supplied right edge at x=24");
        assertTrue(screen.contains("graphics.blit(GUI_TEXTURE, getX(), getY(), 0, slotY, TEXTURE_WIDTH, slotHeight, 256, 256)"),
                "toolbar slots must come directly from the supplied GUI image");
        assertTrue(screen.contains("graphics.blit(GUI_TEXTURE, getX() + 3, getY() + hoverOffsetY, 208, hoverY,"),
                "toolbar hover frames must use the supplied right-hand frames");
        assertTrue(screen.contains("hoverWidth, ICON_HEIGHT, 256, 256)"),
                "the gear hover must fill the full supplied normal-icon width");
        assertFalse(screen.contains("renderSlot("),
                "the matrix icon is already embedded in the supplied GUI image");
        assertTrue(screen.contains("cellWorkbenchButton.setVisibility(true)"),
                "the workbench button must remain visible without a configurable cell");
        assertTrue(screen.contains("cellWorkbenchButton.active = ECOStorageCellSettings.canConfigure(cellSlot.getItem())"),
                "the visible workbench button must be disabled without a configurable cell");
        try (InputStream input = Files.newInputStream(
                ASSETS.resolve("textures/block/energized_me_chest/top.png"))) {
            BufferedImage top = ImageIO.read(input);
            assertNotNull(top, "top texture did not decode");
            for (int x = 0; x < top.getWidth(); x++) {
                for (int y = 0; y < top.getHeight(); y++) {
                    assertEquals(255, top.getRGB(x, y) >>> 24,
                            "the top center must be opaque at " + x + "," + y);
                }
            }
        }
    }

    @Test
    void poweredFrontKeepsEcosSixFrameAnimation() throws Exception {
        Path animation = ASSETS.resolve("textures/block/energized_me_chest/front_eco_drive_on.png");
        BufferedImage strip;
        try (InputStream input = Files.newInputStream(animation)) {
            strip = ImageIO.read(input);
        }
        assertNotNull(strip, "the animated front did not decode");
        assertEquals(16, strip.getWidth());
        assertEquals(96, strip.getHeight());
        int changedFrames = 0;
        for (int frame = 0; frame < 6; frame++) {
            int top = frame * 16;
            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    int argb = strip.getRGB(x, top + y);
                    int red = (argb >> 16) & 0xFF;
                    int green = (argb >> 8) & 0xFF;
                    int blue = argb & 0xFF;
                    assertFalse(red > green + 20 && blue > green + 20,
                            "powered front still contains purple pixels at frame " + frame + ", " + x + "," + y);
                }
            }
            int changedPixels = 0;
            if (frame > 0) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        if (strip.getRGB(x, top + y) != strip.getRGB(x, top - 16 + y)) {
                            changedPixels++;
                        }
                    }
                }
            }
            if (changedPixels > 0) {
                changedFrames++;
            }
        }
        assertEquals(5, changedFrames, "the powered front does not animate between all six frames");
        JsonObject metadata = json(ASSETS.resolve(
                "textures/block/energized_me_chest/front_eco_drive_on.png.mcmeta"));
        JsonObject animationMetadata = metadata.getAsJsonObject("animation");
        assertTrue(animationMetadata.get("interpolate").getAsBoolean());
        assertEquals(10, animationMetadata.get("frametime").getAsInt());
    }

    @Test
    void chestRegistersItsDynamicCellRendererAndGuidePage() throws Exception {
        String renderer = constants(
                "cn/dancingsnow/neoecoprototype/client/renderer/blockentity/EnergizedMEChestRenderer.class");
        assertFalse(renderer.contains("IFixedBlockEntityRenderer"));
        assertFalse(renderer.contains("renderFixed"));
        assertTrue(renderer.contains("renderCell"));
        assertTrue(renderer.contains("tessellateModel"));
        assertTrue(renderer.contains("ECOCellModels"));
        assertTrue(renderer.contains("getModelLocation"));
        assertTrue(renderer.contains("storage_cell_l1_item"));
        assertFalse(renderer.contains("StorageCellModels"));
        assertFalse(renderer.contains("getDefaultModel"));
        assertFalse(renderer.contains("storage_cell_ae2_plain"));
        assertTrue(renderer.contains("CellLedRenderer"));
        assertTrue(renderer.contains("getDisplayState"));
        assertTrue(renderer.contains("getStateColor"));
        String client = constants("cn/dancingsnow/neoecoprototype/client/NeoECOPrototypeClient.class");
        assertTrue(client.contains("ENERGIZED_ME_CHEST_BE"));
        assertTrue(client.contains("EnergizedMEChestRenderer"));
        String rendererSource = Files.readString(Path.of(
                "src/main/java/cn/dancingsnow/neoecoprototype/client/renderer/blockentity/EnergizedMEChestRenderer.java"));
        int dynamicRender = rendererSource.indexOf("public void render(");
        assertTrue(dynamicRender >= 0, "dynamic renderer method is missing");
        assertFalse(rendererSource.contains("public void renderFixed("),
                "the installed cell must not be baked into fixed section geometry");
        assertTrue(rendererSource.substring(dynamicRender).contains("tessellateModel"),
                "the dynamic block-entity renderer must draw the current cell every frame");
        String clientSource = Files.readString(Path.of(
                "src/main/java/cn/dancingsnow/neoecoprototype/client/NeoECOPrototypeClient.java"));
        assertTrue(clientSource.contains("event.registerBlockEntityRenderer(ModRegistration.ENERGIZED_ME_CHEST_BE.get()"),
                "the chest must register its dynamic cell renderer");
        assertFalse(clientSource.contains("FixedBlockEntityRenderers.register(ModRegistration.ENERGIZED_ME_CHEST_BE.get()"),
                "the chest must not register a fixed cell renderer");
        for (String language : Set.of("", "_zh_cn")) {
            Path page = ASSETS.resolve("ae2guide/" + language + "/neoecoprototype_intro/energized_me_chest.md");
            assertTrue(Files.isRegularFile(page), page.toString());
            String text = Files.readString(page);
            assertTrue(text.contains("neoecoprototype:energized_me_chest"));
            assertTrue(text.contains("parent: neoecoprototype_intro/index.md"));
        }
    }

    @Test
    void recipeLootAndToolTagsNameTheChest() throws Exception {
        JsonObject recipe = json(DATA.resolve("neoecoprototype/recipe/energized_me_chest.json"));
        assertEquals(ID, recipe.getAsJsonObject("result").get("id").getAsString());
        JsonObject loot = json(DATA.resolve("neoecoprototype/loot_table/blocks/energized_me_chest.json"));
        assertEquals(ID, loot.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
        for (String tag : Set.of("mineable/pickaxe", "needs_stone_tool")) {
            JsonObject values = json(DATA.resolve("minecraft/tags/block/" + tag + ".json"));
            assertTrue(values.getAsJsonArray("values").contains(new com.google.gson.JsonPrimitive(ID)), tag);
        }
    }

    @Test
    void guiClassesDoNotReachForAe2ChestGuiClasses() throws Exception {
        for (String name : Set.of(
                "cn/dancingsnow/neoecoprototype/menu/EnergizedMEChestMenu",
                "cn/dancingsnow/neoecoprototype/client/EnergizedMEChestScreen")) {
            String constants = constants(name + ".class");
            assertFalse(constants.contains("appeng/menu/implementations/MEChestMenu"), name);
            assertFalse(constants.contains("appeng/client/gui/implementations/MEChestScreen"), name);
        }
    }

    @Test
    void ec1ChestHasGreenDerivedArtAndItsOwnRecipe() throws Exception {
        String ec1Id = "neoecoprototype:ec1_me_chest";
        JsonObject recipe = json(DATA.resolve("neoecoprototype/recipe/ec1_me_chest.json"));
        assertEquals(ec1Id, recipe.getAsJsonObject("result").get("id").getAsString());
        var pattern = recipe.getAsJsonArray("pattern");
        assertEquals(3, pattern.size());
        for (int index = 0; index < 3; index++) {
            assertEquals(List.of("ADA", "DBD", "CDC").get(index), pattern.get(index).getAsString());
        }
        assertEquals("ae2:drive", recipe.getAsJsonObject("key").getAsJsonObject("A").get("item").getAsString());
        assertEquals("ae2:logic_processor", recipe.getAsJsonObject("key").getAsJsonObject("B").get("item").getAsString());
        assertEquals("ae2:chest", recipe.getAsJsonObject("key").getAsJsonObject("C").get("item").getAsString());
        assertEquals("neoecoae:storage_casing",
                recipe.getAsJsonObject("key").getAsJsonObject("D").get("item").getAsString());

        JsonObject energizedRecipe = json(DATA.resolve("neoecoprototype/recipe/energized_me_chest.json"));
        assertEquals(List.of("ADA", "ACA", "ADA"), energizedRecipe.getAsJsonArray("pattern").asList().stream()
                .map(value -> value.getAsString()).toList());
        assertEquals("neoecoprototype:ec1_me_chest",
                energizedRecipe.getAsJsonObject("key").getAsJsonObject("C").get("item").getAsString());
        assertEquals("neoecoae:energized_superconductive_block",
                energizedRecipe.getAsJsonObject("key").getAsJsonObject("A").get("item").getAsString());
        assertEquals("neoecoae:superconducting_processor",
                energizedRecipe.getAsJsonObject("key").getAsJsonObject("D").get("item").getAsString());

        JsonObject loot = json(DATA.resolve("neoecoprototype/loot_table/blocks/ec1_me_chest.json"));
        assertEquals(ec1Id, loot.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString());
        for (String tag : Set.of("mineable/pickaxe", "needs_stone_tool")) {
            JsonObject values = json(DATA.resolve("minecraft/tags/block/" + tag + ".json"));
            assertTrue(values.getAsJsonArray("values").contains(new com.google.gson.JsonPrimitive(ec1Id)), tag);
        }

        JsonObject blockstates = json(ASSETS.resolve("blockstates/ec1_me_chest.json"));
        assertEquals(24, blockstates.getAsJsonArray("multipart").size());
        for (var part : blockstates.getAsJsonArray("multipart")) {
            String model = part.getAsJsonObject().getAsJsonObject("apply").get("model").getAsString();
            assertTrue(model.equals("neoecoprototype:block/ec1_me_chest_base")
                            || model.equals("neoecoprototype:block/ec1_me_chest_off"),
                    "EC1 blockstate selected " + model);
        }
        JsonObject ec1Base = json(ASSETS.resolve("models/block/ec1_me_chest_base.json"));
        assertEquals("neoecoprototype:block/energized_me_chest_base", ec1Base.get("parent").getAsString());
        JsonObject ec1Textures = ec1Base.getAsJsonObject("textures");
        for (String slot : List.of("top", "bottom", "side", "back", "details", "particle")) {
            assertEquals("neoecoprototype:block/ec1_me_chest/" + (slot.equals("particle") ? "side" : slot),
                    ec1Textures.get(slot).getAsString(), slot);
        }
        assertEquals("neoecoprototype:block/ec1_me_chest_base",
                json(ASSETS.resolve("models/block/ec1_me_chest_off.json")).get("parent").getAsString());
        assertEquals("neoecoprototype:block/ec1_me_chest_off",
                json(ASSETS.resolve("models/item/ec1_me_chest.json")).get("parent").getAsString());
        int recoloredPixels = assertGreenDerivative("front_eco_drive_on.png", 16, 96);
        for (String texture : List.of("front_eco_drive", "top", "bottom", "side", "back", "details")) {
            recoloredPixels += assertGreenDerivative(texture + ".png", 16, 16);
        }
        assertTrue(recoloredPixels > 0, "EC1 art contains no green recoloring");

        String registration = constants("cn/dancingsnow/neoecoprototype/registration/ModRegistration.class");
        assertTrue(registration.contains("EC1_ME_CHEST_BLOCK"));
        assertTrue(registration.contains("EC1_ME_CHEST_ITEM"));
        String blockEntity = constants(
                "cn/dancingsnow/neoecoprototype/blockentity/storage/EnergizedMEChestBlockEntity.class");
        assertTrue(blockEntity.contains("IECOStorageCellItem"));
        assertTrue(blockEntity.contains("ECO_INFINITE_ITEM_CELL"));
        assertTrue(blockEntity.contains("acceptsCell"));
    }

    private static int assertGreenDerivative(String name, int width, int height) throws Exception {
        BufferedImage source;
        BufferedImage green;
        try (InputStream input = Files.newInputStream(
                ASSETS.resolve("textures/block/energized_me_chest/" + name))) {
            source = ImageIO.read(input);
        }
        try (InputStream input = Files.newInputStream(
                ASSETS.resolve("textures/block/ec1_me_chest/" + name))) {
            green = ImageIO.read(input);
        }
        assertNotNull(source, name + " source did not decode");
        assertNotNull(green, name + " green derivative did not decode");
        assertEquals(width, source.getWidth());
        assertEquals(height, source.getHeight());
        assertEquals(source.getWidth(), green.getWidth());
        assertEquals(source.getHeight(), green.getHeight());
        int recolored = 0;
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int red = (argb >> 16) & 0xFF;
                int greenChannel = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                if (blue > greenChannel + 20 && blue > red + 20) {
                    assertEquals(source.getRGB(x, y) >>> 24, green.getRGB(x, y) >>> 24,
                            "EC1 recoloring changed alpha at " + x + "," + y);
                    if (green.getRGB(x, y) != argb) {
                        int greenRed = (green.getRGB(x, y) >> 16) & 0xFF;
                        int greenGreen = (green.getRGB(x, y) >> 8) & 0xFF;
                        int greenBlue = green.getRGB(x, y) & 0xFF;
                        assertFalse(greenBlue > greenGreen + 20 && greenBlue > greenRed + 20,
                                "EC1 recolored pixel is still blue-dominant at " + x + "," + y);
                        recolored++;
                    }
                } else {
                    assertEquals(argb, green.getRGB(x, y),
                            "EC1 recoloring changed a non-blue pixel at " + x + "," + y);
                }
            }
        }
        return recolored;
    }

    private static InputStream resource(String name) throws Exception {
        InputStream input = EnergizedMEChestResourceContractTest.class.getClassLoader().getResourceAsStream(name);
        if (input != null) {
            return input;
        }
        if (name.startsWith("/assets/ae2/")) {
            try (ZipFile zip = new ZipFile(AE2_JAR.toFile())) {
                var entry = zip.getEntry(name.substring(1));
                assertNotNull(entry, name + " is missing from " + AE2_JAR);
                return new ByteArrayInputStream(zip.getInputStream(entry).readAllBytes());
            }
        }
        if (name.startsWith("/assets/neoecoae/")) {
            try (ZipFile zip = new ZipFile(ECO_JAR.toFile())) {
                var entry = zip.getEntry(name.substring(1));
                assertNotNull(entry, name + " is missing from " + ECO_JAR);
                return new ByteArrayInputStream(zip.getInputStream(entry).readAllBytes());
            }
        }
        assertNotNull(input, name + " is missing from the test classpath");
        return input;
    }

    private static ClassNode ourClass(String name) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream input = resource(name + ".class")) {
            new ClassReader(input).accept(node, 0);
        }
        return node;
    }

    private static MethodNode method(ClassNode node, String name) {
        MethodNode method = node.methods.stream()
                .filter(candidate -> candidate.name.equals(name))
                .findFirst()
                .orElse(null);
        assertNotNull(method, name + " is missing from " + node.name);
        return method;
    }

    private static int instructionOfCall(MethodNode method, String name) {
        return instructionOfCall(method, null, name);
    }

    private static int instructionOfCall(MethodNode method, String owner, String name) {
        int index = 0;
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof MethodInsnNode call && call.name.equals(name)
                    && (owner == null || call.owner.equals(owner))) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private static boolean readsField(MethodNode method, String owner, String name) {
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction instanceof FieldInsnNode field && field.owner.equals(owner) && field.name.equals(name)
                    && (field.getOpcode() == Opcodes.GETSTATIC || field.getOpcode() == Opcodes.GETFIELD)) {
                return true;
            }
        }
        return false;
    }

    private static String constants(String name) throws Exception {
        try (InputStream input = resource(name)) {
            return new String(input.readAllBytes(), StandardCharsets.ISO_8859_1);
        }
    }

    private static void assertImageSize(Path path, int width, int height, String label) throws Exception {
        BufferedImage image;
        try (InputStream input = Files.newInputStream(path)) {
            image = ImageIO.read(input);
        }
        assertNotNull(image, label + " texture did not decode");
        assertEquals(width, image.getWidth(), label);
        assertEquals(height, image.getHeight(), label);
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static String externalSlot(JsonObject element, String direction, JsonObject face) {
        String cullface = face.has("cullface") ? face.get("cullface").getAsString() : "";
        if (Set.of("east", "west").contains(direction) && direction.equals(cullface)) {
            return "#side";
        }
        if ("south".equals(direction) && "south".equals(cullface)) {
            return "#back";
        }
        boolean frontPillar = "front_pillar_a".equals(element.get("name").getAsString());
        if ("up".equals(direction) && (frontPillar || "up".equals(cullface))) {
            return "#top";
        }
        if ("down".equals(direction) && (frontPillar || "down".equals(cullface))) {
            return "#bottom";
        }
        return null;
    }

    private static List<Integer> externalUv(JsonObject element, String direction) {
        var from = element.getAsJsonArray("from");
        var to = element.getAsJsonArray("to");
        return switch (direction) {
            case "east", "west" -> List.of(from.get(2).getAsInt(), from.get(1).getAsInt(),
                    to.get(2).getAsInt(), to.get(1).getAsInt());
            case "south" -> List.of(from.get(0).getAsInt(), from.get(1).getAsInt(),
                    to.get(0).getAsInt(), to.get(1).getAsInt());
            case "up", "down" -> List.of(from.get(0).getAsInt(), from.get(2).getAsInt(),
                    to.get(0).getAsInt(), to.get(2).getAsInt());
            default -> throw new IllegalArgumentException(direction);
        };
    }

    private static List<Integer> uv(JsonObject face) {
        var uv = face.getAsJsonArray("uv");
        return List.of(uv.get(0).getAsInt(), uv.get(1).getAsInt(), uv.get(2).getAsInt(), uv.get(3).getAsInt());
    }

}
