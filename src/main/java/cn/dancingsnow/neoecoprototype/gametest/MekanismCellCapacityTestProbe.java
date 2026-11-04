package cn.dancingsnow.neoecoprototype.gametest;

/**
 * The only place the game test source touches Mekanism or Applied Mekanistics classes.
 *
 * <p>Same reason {@link AppFluxTestProbe} exists: the game test bootstrap resolves every {@code @GameTest}
 * method on its own class, so a reference to a class from a mod that is not installed stops the test server
 * from starting at all - far worse than one skipped row. This class is only linked when a caller has already
 * checked that both mods are loaded, and it hands back plain {@code AEKey}s so the filling logic itself needs
 * nothing from here.</p>
 */
final class MekanismCellCapacityTestProbe {

    /** A registered Mekanism chemical as an AE key, or null if that id is not in the game. */
    static appeng.api.stacks.AEKey chemicalKey(String path) {
        var chemical = mekanism.api.MekanismAPI.CHEMICAL_REGISTRY.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("mekanism", path));
        if (chemical == null || chemical.isEmptyType()) {
            return null;
        }
        return me.ramidzkh.mekae2.ae2.MekanismKey.of(new mekanism.api.chemical.ChemicalStack(chemical, 1));
    }

    private MekanismCellCapacityTestProbe() {
    }
}
