package cn.dancingsnow.neoecoprototype.api;

/**
 * Power tuning for addon-owned machine hosts.
 *
 * <p>The base values mirror Eco's L4 controller idle usage. L1 keeps the same
 * machine behavior but runs at one eighth of that idle rate.</p>
 */
public enum SimplifyPowerProfile {
    L1(1.0D / 8.0D, 16.0D, 64.0D, 256.0D, 258.0D, 16.0D, 64.0D);

    private final double idlePowerMultiplier;
    private final double baseComponentIdlePower;
    private final double highIdleComponentPower;
    private final double storageDriveBaseIdlePower;
    private final double storageControllerBaseIdlePower;
    private final double computationControllerBaseIdlePower;
    private final double craftingControllerBaseIdlePower;

    SimplifyPowerProfile(
            double idlePowerMultiplier,
            double baseComponentIdlePower,
            double highIdleComponentPower,
            double storageDriveBaseIdlePower,
            double storageControllerBaseIdlePower,
            double computationControllerBaseIdlePower,
            double craftingControllerBaseIdlePower) {
        this.idlePowerMultiplier = idlePowerMultiplier;
        this.baseComponentIdlePower = baseComponentIdlePower;
        this.highIdleComponentPower = highIdleComponentPower;
        this.storageDriveBaseIdlePower = storageDriveBaseIdlePower;
        this.storageControllerBaseIdlePower = storageControllerBaseIdlePower;
        this.computationControllerBaseIdlePower = computationControllerBaseIdlePower;
        this.craftingControllerBaseIdlePower = craftingControllerBaseIdlePower;
    }

    public double idlePowerMultiplier() {
        return idlePowerMultiplier;
    }

    public double baseComponentIdlePower() {
        return scaled(baseComponentIdlePower);
    }

    public double highIdleComponentPower() {
        return scaled(highIdleComponentPower);
    }

    public double storageDriveIdlePower() {
        return scaled(storageDriveBaseIdlePower);
    }

    public double scale(double basePower) {
        return scaled(basePower);
    }

    public double storageControllerIdlePower() {
        return scaled(storageControllerBaseIdlePower);
    }

    public double computationControllerIdlePower() {
        return scaled(computationControllerBaseIdlePower);
    }

    public double craftingControllerIdlePower() {
        return scaled(craftingControllerBaseIdlePower);
    }

    private double scaled(double basePower) {
        return basePower * idlePowerMultiplier;
    }
}
