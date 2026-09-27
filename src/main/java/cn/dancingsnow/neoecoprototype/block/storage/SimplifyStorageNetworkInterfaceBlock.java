package cn.dancingsnow.neoecoprototype.block.storage;

/**
 * Alternate L1 storage interface, the one the one-click builder puts in the cell.
 *
 * <p>Unlike the C1 and F1 pairs this one carries no behavioural difference at all: both blocks share
 * the full storage interface behaviour, including eco's interface UI (verified in game -- our
 * {@code ECOMachineInterfaceBlockEntityMixin} title wrapper, which only runs while a UI is actually
 * being built, logs for this block). The two ids exist so a pack can give them separate looks.
 */
public class SimplifyStorageNetworkInterfaceBlock extends SimplifyStorageInterfaceBlock {
    public SimplifyStorageNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }
}
