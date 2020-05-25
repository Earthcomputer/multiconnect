package net.earthcomputer.multiconnect.protocols;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_11_2.Protocol_1_11_2;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12_1.Protocol_1_12_1;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13_1.Protocol_1_13_1;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.earthcomputer.multiconnect.protocols.v1_14_2.Protocol_1_14_2;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.earthcomputer.multiconnect.protocols.v1_15_1.Protocol_1_15_1;
import net.earthcomputer.multiconnect.protocols.v1_15_2.Protocol_1_15_2;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.earthcomputer.multiconnect.transformer.InboundTranslator;
import net.earthcomputer.multiconnect.transformer.OutboundTranslator;
import net.earthcomputer.multiconnect.transformer.TranslatorRegistry;
import net.minecraft.SharedConstants;

import static net.earthcomputer.multiconnect.api.Protocols.*;

public class ProtocolRegistry {

    private static Int2ObjectOpenHashMap<AbstractProtocol> protocols = new Int2ObjectOpenHashMap<>();

    private static TranslatorRegistry translatorRegistry = new TranslatorRegistry();

    public static AbstractProtocol get(int version) {
        return protocols.get(version);
    }

    public static AbstractProtocol latest() {
        return protocols.get(SharedConstants.getGameVersion().getProtocolVersion());
    }

    public static TranslatorRegistry getTranslatorRegistry() {
        return translatorRegistry;
    }

    // usually you want type and translator to handle the same type
    public static <T> void registerInboundTranslator(Class<T> type, InboundTranslator<T> translator) {
        translatorRegistry.registerInboundTranslator(registeringProtocol, type, translator);
    }

    public static void registerInboundTranslatorUnchecked(Class<?> type, InboundTranslator<?> translator) {
        translatorRegistry.registerInboundTranslatorUnchecked(registeringProtocol, type, translator);
    }

    public static <T> void registerOutboundTranslator(Class<T> type, OutboundTranslator<T> translator) {
        translatorRegistry.registerOutboundTranslator(registeringProtocol, type, translator);
    }


    private static int registeringProtocol;
    private static void register(int version, AbstractProtocol protocol) {
        register(version, protocol, () -> {});
    }

    private static void register(int version, AbstractProtocol protocol, Runnable registerTranslators) {
        registeringProtocol = version;
        protocols.put(version, protocol);
        registerTranslators.run();
    }

    static {
        register(V1_16, new Protocol_1_16());
        register(V1_15_2, new Protocol_1_15_2(), Protocol_1_15_2::registerTranslators);
        register(V1_15_1, new Protocol_1_15_1());
        register(V1_15, new Protocol_1_15());
        register(V1_14_4, new Protocol_1_14_4(), Protocol_1_14_4::registerTranslators);
        register(V1_14_3, new Protocol_1_14_3());
        register(V1_14_2, new Protocol_1_14_2(), Protocol_1_14_2::registerTranslators);
        register(V1_14_1, new Protocol_1_14_1());
        register(V1_14, new Protocol_1_14());
        register(V1_13_2, new Protocol_1_13_2(), Protocol_1_13_2::registerTranslators);
        register(V1_13_1, new Protocol_1_13_1(), Protocol_1_13_1::registerTranslators);
        register(V1_13, new Protocol_1_13(), Protocol_1_13::registerTranslators);
        register(V1_12_2, new Protocol_1_12_2(), Protocol_1_12_2::registerTranslators);
        register(V1_12_1, new Protocol_1_12_1(), Protocol_1_12_1::registerTranslators);
        register(V1_12, new Protocol_1_12());
        register(V1_11_2, new Protocol_1_11_2(), Protocol_1_11_2::registerTranslators);
        register(V1_11, new Protocol_1_11());
    }

}
