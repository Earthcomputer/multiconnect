package net.earthcomputer.multiconnect.impl.via;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.platform.ViaPlatformLoader;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.bungee.providers.BungeeMovementTransmitter;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.HandItemProvider;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.providers.MovementTransmitterProvider;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.earthcomputer.multiconnect.impl.via.provider.MulticonnectHandItemProvider;
import net.earthcomputer.multiconnect.impl.via.provider.MulticonnectVersionProvider;

public class MulticonnectLoader implements ViaPlatformLoader {
    private final IMulticonnectTranslatorApi api;

    public MulticonnectLoader(IMulticonnectTranslatorApi api) {
        this.api = api;
    }

    @Override
    public void load() {
        Via.getManager().getProviders().use(MovementTransmitterProvider.class, new BungeeMovementTransmitter());
        Via.getManager().getProviders().use(VersionProvider.class, new MulticonnectVersionProvider(api));
        Via.getManager().getProviders().use(HandItemProvider.class, new MulticonnectHandItemProvider(api));
    }

    @Override
    public void unload() {
    }
}
