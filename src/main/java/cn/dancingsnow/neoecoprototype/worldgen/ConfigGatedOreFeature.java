package cn.dancingsnow.neoecoprototype.worldgen;

import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.List;

/**
 * The vanilla ore feature, but only where the server config says so.
 *
 * <p>The shipped biome modifier deliberately names all three dimension biome tags: which of them
 * actually place is decided here, at generate time, so moving the ore to another dimension is a
 * config edit and not a datapack. An empty list turns generation off without touching the worldgen
 * files.
 */
public class ConfigGatedOreFeature extends OreFeature {
    public ConfigGatedOreFeature() {
        super(OreConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        ServerLevel level = context.level().getLevel();
        return level == null
                || dimensionAllowed(level.dimension(),
                        NeoECOPrototypeServerConfig.CRYOTHEUM_ORE_DIMENSIONS.get())
                ? super.place(context)
                : false;
    }

    /** Exact {@code namespace:path} match; a bare path is not accepted, so a typo shows up as no ore. */
    public static boolean dimensionAllowed(ResourceKey<Level> dimension, List<? extends String> allowed) {
        var id = dimension.location().toString();
        for (var entry : allowed) {
            if (entry.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
