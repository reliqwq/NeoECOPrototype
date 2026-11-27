package cn.dancingsnow.neoecoprototype.integration.ae2;

import appeng.api.networking.energy.IAEPowerStorage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

/** Records upstream iterator removals so they can be restored after the iteration has finished. */
public final class RemovedRequesterIterator implements Iterator<IAEPowerStorage> {
    private final Iterator<IAEPowerStorage> delegate;
    private IAEPowerStorage current;
    private List<IAEPowerStorage> removed;

    public RemovedRequesterIterator(Iterator<IAEPowerStorage> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public IAEPowerStorage next() {
        current = delegate.next();
        return current;
    }

    @Override
    public void remove() {
        delegate.remove();
        if (removed == null) {
            removed = new ArrayList<>();
        }
        removed.add(current);
        current = null;
    }

    public void restoreEligible(SortedSet<IAEPowerStorage> requesters) {
        if (removed == null) {
            return;
        }
        for (IAEPowerStorage storage : removed) {
            if (storage.isAEPublicPowerStorage()
                    && storage.getPowerFlow().isAllowInsertion()
                    && storage.getAECurrentPower() < storage.getAEMaxPower()) {
                requesters.add(storage);
            }
        }
    }
}
