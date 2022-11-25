package net.earthcomputer.multiconnect.impl.via;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class NonModCheck implements ModInitializer {
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            throw new RuntimeException("Added multiconnect via translator to the mod list, you should not do this!");
            // ... if you really want to add it manually, add it to .minecraft/config/multiconnect/via-translator*.jar
        }
    }
}
