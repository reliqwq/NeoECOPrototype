package cn.dancingsnow.neoecoprototype.worldgen;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** The two registries a hand-built structure needs: its type, and the piece that writes its blocks. */
public final class ModWorldgen {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, NeoECOPrototype.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, NeoECOPrototype.MOD_ID);

    public static final Supplier<StructureType<CryotheumMeteoriteStructure>> CRYOTHEUM_METEORITE =
            STRUCTURE_TYPES.register("cryotheum_meteorite",
                    () -> () -> CryotheumMeteoriteStructure.CODEC);
    public static final Supplier<StructurePieceType> METEORITE_PIECE =
            STRUCTURE_PIECES.register("cryotheum_meteorite",
                    () -> (context, tag) -> new CryotheumMeteoriteStructure.MeteoritePiece(tag));

    private ModWorldgen() {
    }
}
