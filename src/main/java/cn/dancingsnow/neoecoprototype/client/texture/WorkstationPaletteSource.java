package cn.dancingsnow.neoecoprototype.client.texture;

import cn.dancingsnow.neoecoae.client.texture.WorkstationSpriteSource;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;

/**
 * Asset JSON stays in our namespace; eco 21.2.19 (#165) owns all coloring and animation.
 * Palette keys are full addon ids, as supported by eco's public source codec. The six undyed
 * sheets only need aliases because our asset JSON cannot name eco resources directly.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, value = Dist.CLIENT)
public record WorkstationPaletteSource(WorkstationSpriteSource delegate) implements SpriteSource {
    public static final MapCodec<WorkstationPaletteSource> CODEC = WorkstationSpriteSource.CODEC
        .xmap(WorkstationPaletteSource::new, WorkstationPaletteSource::delegate);
    private static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);
    private static final String ROOT = "block/working_station/";
    private static final java.util.List<String> STATIC_SHEETS = java.util.List.of(
        "controller_formed/controller_formed",
        "controller_formed/controller_formed_smart",
        "controller_formed/controller_formed_light_normal",
        "controller_formed/controller_formed_screen",
        "casing",
        "cooling_controller/screen_on");

    @SubscribeEvent
    public static void register(RegisterSpriteSourceTypesEvent event) {
        event.register(NeoECOPrototype.id("workstation_palette"), TYPE);
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }

    @Override
    public void run(ResourceManager manager, Output output) {
        for (String sheet : STATIC_SHEETS) {
            var alias = NeoECOPrototype.id(ROOT + sheet);
            // A resource pack can replace an addon sprite explicitly; otherwise follow eco's sheet.
            var replacement = manager.getResource(TEXTURE_ID_CONVERTER.idToFile(alias));
            var resource = replacement.or(() -> manager.getResource(TEXTURE_ID_CONVERTER.idToFile(
                ResourceLocation.fromNamespaceAndPath("neoecoae", ROOT + sheet))));
            resource.ifPresentOrElse(value -> output.add(alias, value),
                () -> NeoECOPrototype.LOGGER.warn("Missing workstation sheet {}", alias));
        }
        delegate.run(manager, output);
    }
}
