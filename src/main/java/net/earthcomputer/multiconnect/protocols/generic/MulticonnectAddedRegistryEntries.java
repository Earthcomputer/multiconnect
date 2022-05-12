package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.protocols.v1_17_1.Particles_1_17_1;

public final class MulticonnectAddedRegistryEntries {
    private MulticonnectAddedRegistryEntries() {}

    public static void register() {
        Particles_1_17_1.register();
    }

    public static void initializeClient() {
        Particles_1_17_1.registerFactories();
    }
}
