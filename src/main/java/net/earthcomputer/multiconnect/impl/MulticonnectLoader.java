package net.earthcomputer.multiconnect.impl;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.bungee.providers.BungeeMovementTransmitter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import net.earthcomputer.multiconnect.provider.MulticonnectHandItemProvider;
import net.earthcomputer.multiconnect.provider.MulticonnectVersionProvider;

public class MulticonnectLoader implements ViaPlatformLoader {
    @Override
    public void load() {
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
        Via.getManager().getProviders().use(VersionProvider.class, new MulticonnectVersionProvider());
        Via.getManager().getProviders().use(HandItemProvider.class, new MulticonnectHandItemProvider());
    }

    @Override
    public void unload() {
    }
}
