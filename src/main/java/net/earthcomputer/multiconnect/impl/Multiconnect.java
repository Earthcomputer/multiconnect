package net.earthcomputer.multiconnect.impl;

import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import org.slf4j.Logger;

public class Multiconnect implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static IMulticonnectTranslator translator;

    @Override
    public void onInitialize() {
        IMulticonnectTranslatorApi api = new TranslatorApiImpl();
        translator = TranslatorDiscoverer.discoverTranslator(api);
        LOGGER.info("Using multiconnect translator: {}", translator.getClass().getName());
        translator.init(api);
    }

    public static String getVersion() {
        return VersionHolder.VERSION;
    }

    // used to get the version lazily
    private static class VersionHolder {
        private static final String VERSION = Util.make(() -> {
            String overriddenVersion = System.getProperty("multiconnect.overrideVersion");
            if (overriddenVersion != null) {
                return overriddenVersion;
            }
            return FabricLoader.getInstance().getModContainer("multiconnect")
                .orElseThrow(() -> new RuntimeException("Could not find multiconnect mod container"))
                .getMetadata().getVersion().getFriendlyString();
        });
    }
}
