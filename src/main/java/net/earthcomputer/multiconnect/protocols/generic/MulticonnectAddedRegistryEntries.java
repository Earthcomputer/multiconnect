package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.protocols.v1_12.BlockConnectors_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.block.Blocks_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.Particles_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13.AddBannerPatternRecipe;
import net.earthcomputer.multiconnect.protocols.v1_14.SoundEvents_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15_2;
import net.earthcomputer.multiconnect.protocols.v1_17.Particles_1_17_1;

public final class MulticonnectAddedRegistryEntries {
    private MulticonnectAddedRegistryEntries() {}

    public static void register() {
        Blocks_1_12_2.register();
        Particles_1_12_2.register();
        AddBannerPatternRecipe.register();
        SoundEvents_1_14_4.register();
        Particles_1_17_1.register();

        registerConnectors();
    }

    public static void initializeClient() {
        Particles_1_12_2.registerFactories();
        Particles_1_17_1.registerFactories();
    }

    private static void registerConnectors() {
        BlockConnectors_1_12_2.register();
        Protocol_1_15_2.registerConnectors();
    }
}
