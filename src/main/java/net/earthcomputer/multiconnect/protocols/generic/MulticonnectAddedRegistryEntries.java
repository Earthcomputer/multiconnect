package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.protocols.v1_12.Particles_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_14.SoundEvents_1_14_4;

public final class MulticonnectAddedRegistryEntries {
    private MulticonnectAddedRegistryEntries() {}

    public static void register() {
        Particles_1_12_2.register();
        SoundEvents_1_14_4.register();
    }

    public static void initializeClient() {
        Particles_1_12_2.registerFactories();
    }
}
