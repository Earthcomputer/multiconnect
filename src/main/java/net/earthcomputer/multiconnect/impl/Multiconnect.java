package net.earthcomputer.multiconnect.impl;

import com.mojang.logging.LogUtils;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslator;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

import java.util.Comparator;
import java.util.ServiceLoader;

public class Multiconnect implements ModInitializer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static IMulticonnectTranslator translator;

    @Override
    public void onInitialize() {
        try {
            // sanity check that our via subproject is on the classpath
            Class.forName("net.earthcomputer.multiconnect.impl.via.ViaMulticonnectTranslator");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        IMulticonnectTranslatorApi api = new TranslatorApiImpl();
        ServiceLoader<IMulticonnectTranslator> loader = ServiceLoader.load(IMulticonnectTranslator.class);
        translator = loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(translator -> translator.isApplicableInEnvironment(api))
            .max(Comparator.comparingInt(IMulticonnectTranslator::priority))
            .orElseThrow();
        LOGGER.info("Using multiconnect translator: {}", translator.getClass().getName());
        translator.init(api);
    }
}
