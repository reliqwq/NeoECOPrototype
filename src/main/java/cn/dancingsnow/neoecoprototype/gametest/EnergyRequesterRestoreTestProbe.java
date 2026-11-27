package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.me.service.EnergyService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

public final class EnergyRequesterRestoreTestProbe {
    private EnergyRequesterRestoreTestProbe() {
    }

    public static void run(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(0, 1, 0));
        var storage = new PartialStorage();
        IManagedGridNode source = null;
        IManagedGridNode port = null;
        try {
            source = GridHelper.createManagedNode(new Object(), (target, node) -> { })
                    .setInWorldNode(false)
                    .setIdlePowerUsage(0.0)
                    .setTagName("neoecoprototype-energy-source-probe");
            source.create(level, pos);
            port = GridHelper.createManagedNode(storage, (target, node) -> { })
                    .addService(IAEPowerStorage.class, storage)
                    .setInWorldNode(false)
                    .setIdlePowerUsage(0.0)
                    .setTagName("neoecoprototype-energy-requester-probe");
            port.create(level, pos);
            var connection = GridHelper.createConnection(source.getNode(), port.getNode());
            IManagedGridNode activeSource = source;
            IManagedGridNode activePort = port;
            helper.runAfterDelay(1, () -> {
                try {
                    var grid = activePort.getGrid();
                    if (grid == null) {
                        helper.fail("the virtual requester has no grid one tick after connection");
                        return;
                    }
                    var energy = (EnergyService) grid.getEnergyService();
                    double first = energy.injectProviderPower(1_000.0, Actionable.MODULATE);
                    if (storage.current != 400.0 || first <= 0.0) {
                        helper.fail("the partial storage did not take exactly 400 of the first offer:"
                                + " overflow=" + first + ", stored=" + storage.current);
                        return;
                    }
                    double second = energy.injectProviderPower(1_000.0, Actionable.MODULATE);
                    if (storage.current != 800.0 || second <= 0.0) {
                        helper.fail("the storage removed by the first oversized offer was not restored:"
                                + " overflow=" + second + ", stored=" + storage.current);
                        return;
                    }
                    double third = energy.injectProviderPower(1_000.0, Actionable.MODULATE);
                    if (storage.current != 1_000.0) {
                        helper.fail("the restored storage did not finish charging: overflow=" + third
                                + ", stored=" + storage.current);
                        return;
                    }
                    helper.succeed();
                } finally {
                    connection.destroy();
                    activeSource.destroy();
                    activePort.destroy();
                }
            });
        } catch (RuntimeException e) {
            if (port != null) {
                port.destroy();
            }
            if (source != null) {
                source.destroy();
            }
            helper.fail("could not install the partial-storage probe: " + e);
        }
    }

    private static final class PartialStorage implements IAEPowerStorage {
        private static final double MAX_POWER = 1_000.0;
        private static final double MAX_PER_OFFER = 400.0;
        private double current;

        @Override
        public double injectAEPower(double amount, Actionable mode) {
            double accepted = Math.min(MAX_PER_OFFER, Math.min(amount, MAX_POWER - current));
            if (mode == Actionable.MODULATE) {
                current += accepted;
            }
            return amount - accepted;
        }

        @Override
        public double extractAEPower(double amount, Actionable mode, PowerMultiplier multiplier) {
            return 0.0;
        }

        @Override
        public double getAEMaxPower() {
            return MAX_POWER;
        }

        @Override
        public double getAECurrentPower() {
            return current;
        }

        @Override
        public boolean isAEPublicPowerStorage() {
            return true;
        }

        @Override
        public AccessRestriction getPowerFlow() {
            return AccessRestriction.READ_WRITE;
        }

        @Override
        public int getPriority() {
            return 1;
        }
    }
}
