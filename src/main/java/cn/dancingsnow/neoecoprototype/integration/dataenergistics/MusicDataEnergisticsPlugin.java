package cn.dancingsnow.neoecoprototype.integration.dataenergistics;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsEntrypoint;
import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsPlugin;
import com.fish_dan_.data_energistics.api.entrypoint.DataEnergisticsRegistry;
import com.fish_dan_.data_energistics.api.registry.digitalsupply.AeKeyTypeRegistration;

/**
 * Handed to Data Energistics' annotation scan, which resolves this class only when {@code data_energistics}
 * is loaded. Nothing in our own code calls it.
 *
 * <p>It registers the key type and nothing else. The only way music ticks enter a network is playback in
 * our jukebox; the only way they leave as data flow is the data reassembler recipe. There is deliberately
 * no supply-interface adapter, because that door took a recorded disc out of the world to pay for something
 * the player can already hear.
 */
@DataEnergisticsEntrypoint(requiredMods = "data_energistics")
public final class MusicDataEnergisticsPlugin implements DataEnergisticsPlugin {

    @Override
    public void register(DataEnergisticsRegistry registry) {
        registry.aeKeyTypes().register(new AeKeyTypeRegistration(
                MusicKeyType.ID, MusicKeyType.TYPE, NeoECOPrototype.id("music")));
    }
}
