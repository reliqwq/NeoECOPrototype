package cn.dancingsnow.neoecoprototype.block.storage;

/**
 * The L1 storage subsystem communication interface: the block the player opens. Same cluster behaviour
 * as {@code simplify_storage_interface} plus eco's interface UI, which is what the {@code ae2:terminal}
 * in its recipe buys -- and the one {@code supportsStorageInterfaceUi()} answers true for.
 */
public class SimplifyStorageNetworkInterfaceBlock extends SimplifyStorageInterfaceBlock {
    public SimplifyStorageNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }
}
