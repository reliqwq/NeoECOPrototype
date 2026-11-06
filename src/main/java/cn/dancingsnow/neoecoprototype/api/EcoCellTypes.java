package cn.dancingsnow.neoecoprototype.api;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * eco's cell types, read out of eco's own registry by id.
 *
 * <p>Why the bulk family needs a fallback rather than a hard answer: those ids are registered by eco *inside
 * its MEGA Cells integration*, so whether {@code neoecoae:mega_item} exists is a fact about a future eco
 * release, not about this mod. Reading it as "throw when missing" meant the first call from a storage host
 * panel or a drive renderer took the whole base's cell view down with it, long after startup was clean -
 * the item constructors only store the supplier, so nothing resolves the type until a player opens
 * something. Falling back to the plain medium's own type keeps the small-bulk cells working (their 3/10
 * type budget and their long byte capacity are ours, not the type's) and costs only the row label.</p>
 */
public final class EcoCellTypes {

    /** Already announced, so a panel redraw does not bury the log in the same line. */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private EcoCellTypes() {
    }

    /** What eco registers under {@code neoecoae:<path>}, or null when it registers no such thing. */
    public static @Nullable ECOCellType find(String path) {
        return NERegistries.CELL_TYPE.get(ResourceLocation.fromNamespaceAndPath("neoecoae", path));
    }

    /**
     * The type eco registers under {@code path}, throwing when it is gone - for the media eco has always
     * registered itself and whose absence really does mean a broken environment.
     */
    public static ECOCellType require(String path) {
        ECOCellType type = find(path);
        if (type == null) {
            throw new IllegalStateException("Missing eco cell type: " + path);
        }
        return type;
    }

    /**
     * A bulk-family type, or the plain medium's type when eco has stopped registering it. The fallback is
     * announced once per bulk id, because a silent swap would leave the row named after a medium the player
     * never chose and nothing in the log would say why.
     */
    public static ECOCellType bulkOrPlain(String bulkPath, String plainPath) {
        ECOCellType bulk = find(bulkPath);
        if (bulk != null) {
            return bulk;
        }
        ECOCellType plain = require(plainPath);
        if (WARNED.add(bulkPath)) {
            NeoECOPrototype.LOGGER.warn("eco registers no cell type {} any more, so the small-bulk cells "
                            + "read as {}; their type budget and byte capacity are ours and stay as they were",
                    bulkPath, plainPath);
        }
        return plain;
    }
}
