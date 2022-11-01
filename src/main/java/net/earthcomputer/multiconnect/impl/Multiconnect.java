package net.earthcomputer.multiconnect.impl;

import com.viaversion.viaversion.ViaManagerImpl;
import com.viaversion.viaversion.api.Via;
import net.fabricmc.api.ModInitializer;

public class Multiconnect implements ModInitializer {
    @Override
    public void onInitialize() {
        var manager = ViaManagerImpl.builder()
            .injector(new MulticonnectInjector())
            .loader(new MulticonnectLoader())
            .platform(new MulticonnectPlatform())
            .build();
        Via.init(manager);
        manager.init();
    }
}
