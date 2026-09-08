package cn.dancingsnow.neoecoprototype.integration.jade;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyDriveBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationDriveBlock;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.integration.jade.provider.SimplifyComputationDriveProvider;
import cn.dancingsnow.neoecoprototype.integration.jade.provider.SimplifyDriveProvider;
import cn.dancingsnow.neoecoprototype.integration.jade.provider.SimplifyStorageHostProvider;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Jade support for the L1 storage subsystem (mirrors eco's own Jade plugin). */
@WailaPlugin
public class SimplifyJadePlugin implements IWailaPlugin {

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path);
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                SimplifyDriveProvider.INSTANCE, SimplifyDriveBlockEntity.class);
        registration.registerBlockDataProvider(
                SimplifyComputationDriveProvider.INSTANCE, SimplifyComputationDriveBlockEntity.class);
        registration.registerBlockDataProvider(
                SimplifyStorageHostProvider.INSTANCE, SimplifyStorageHostBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(SimplifyDriveProvider.INSTANCE, SimplifyDriveBlock.class);
        registration.registerBlockComponent(
                SimplifyComputationDriveProvider.INSTANCE, SimplifyComputationDriveBlock.class);
        registration.registerBlockComponent(SimplifyStorageHostProvider.INSTANCE, SimplifyStorageControllerBlock.class);
    }
}
