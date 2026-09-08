package cn.dancingsnow.neoecoprototype.integration.ae2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SimplifyGridFacadeTest {
    @Test
    void nullNodeIsNotConnected() {
        assertFalse(SimplifyGridFacade.isConnected(null));
    }

    @Test
    void nullNodesCannotShareGrid() {
        assertFalse(SimplifyGridFacade.isOnSameGrid(null, null));
    }

    @Test
    void storageUpdateIgnoresMissingNode() {
        assertDoesNotThrow(() -> SimplifyGridFacade.requestStorageUpdate(null));
    }

    @Test
    void powerNotificationsIgnoreMissingNode() {
        assertDoesNotThrow(() -> SimplifyGridFacade.postProvidePowerEvent(null, null));
        assertDoesNotThrow(() -> SimplifyGridFacade.postReceivePowerEvent(null, null));
        assertDoesNotThrow(() -> SimplifyGridFacade.alertDevice(null));
    }
}
