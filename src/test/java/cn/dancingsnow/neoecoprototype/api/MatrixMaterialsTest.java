package cn.dancingsnow.neoecoprototype.api;

import cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.MatrixSize;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.MatrixType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Guards the "a matrix shows its own kind's material" rule used by the KubeJS builders.
 *
 * <p>The test source set has no Minecraft on its runtime classpath, so this covers
 * {@link MatrixMaterials#of} only: anything that goes through {@code Kind.valueOf} (such as
 * {@code parse}) makes the JVM reflect over the enum's methods, which reference
 * {@code ResourceLocation} and therefore cannot load here.
 */
class MatrixMaterialsTest {

    @Test
    void finiteMatricesKeepTheirKindPalette() {
        assertEquals(MatrixMaterials.Kind.ITEM, MatrixMaterials.of(MatrixType.ITEM, MatrixSize.K1));
        assertEquals(MatrixMaterials.Kind.FLUID, MatrixMaterials.of(MatrixType.FLUID, MatrixSize.K1));
        assertEquals(MatrixMaterials.Kind.CHEMICAL, MatrixMaterials.of(MatrixType.CHEMICAL, MatrixSize.K1));
    }

    @Test
    void onlyUnboundedItemMatricesUseTheInfinitePalette() {
        assertEquals(MatrixMaterials.Kind.INFINITE,
                MatrixMaterials.of(MatrixType.ITEM, MatrixSize.INFINITE));
        // A fluid or chemical matrix keeps its own palette even when it has no capacity limit.
        assertEquals(MatrixMaterials.Kind.FLUID,
                MatrixMaterials.of(MatrixType.FLUID, MatrixSize.INFINITE));
        assertEquals(MatrixMaterials.Kind.CHEMICAL,
                MatrixMaterials.of(MatrixType.CHEMICAL, MatrixSize.INFINITE));
    }

    @Test
    void customCapacityStillResolvesByKind() {
        assertEquals(MatrixMaterials.Kind.ITEM, MatrixMaterials.of(MatrixType.ITEM, MatrixSize.CUSTOM));
        assertEquals(MatrixMaterials.Kind.FLUID, MatrixMaterials.of(MatrixType.FLUID, MatrixSize.CUSTOM));
        assertEquals(MatrixMaterials.Kind.CHEMICAL, MatrixMaterials.of(MatrixType.CHEMICAL, MatrixSize.CUSTOM));
    }

    /** Keeps the script-facing family names stable; renaming one is a breaking change for scripts. */
    @Test
    void familyNamesAreStable() {
        assertEquals(List.of("other", "small_bulk", "pigcat", "universal", "quantum"),
                MatrixMaterials.FAMILY_NAMES);
    }
}
