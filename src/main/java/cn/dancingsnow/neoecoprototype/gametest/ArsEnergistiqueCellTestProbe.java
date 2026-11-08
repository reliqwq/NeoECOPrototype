package cn.dancingsnow.neoecoprototype.gametest;

/**
 * The only place the game test source touches arseng's classes.
 *
 * <p>Same reason {@link MekanismCellCapacityTestProbe} exists: the game test bootstrap resolves every
 * {@code @GameTest} method on its own class, so a reference to a class from a mod that is not installed
 * stops the test server from starting at all. Every caller has already checked that arseng is loaded, and
 * what comes back is a plain {@code AEKey} / {@code AEKeyType} so the tests themselves need nothing from
 * this package.</p>
 */
final class ArsEnergistiqueCellTestProbe {

    /**
     * Ars Nouveau's source is a singleton: arseng exports one key, so the pour test has no second key to
     * open a new type with. Null only if arseng is absent - nothing here is allowed to answer a key that
     * does not exist.
     */
    static appeng.api.stacks.AEKey sourceKey() {
        return gripe._90.arseng.me.key.SourceKey.KEY;
    }

    /** The key type the cell has to report, at arseng's own 1000 source per AE byte. */
    static appeng.api.stacks.AEKeyType sourceKeyType() {
        return gripe._90.arseng.me.key.SourceKeyType.TYPE;
    }

    private ArsEnergistiqueCellTestProbe() {
    }
}
