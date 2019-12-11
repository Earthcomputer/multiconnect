package net.earthcomputer.multiconnect.protocols;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13_1.Protocol_1_13_1;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.earthcomputer.multiconnect.protocols.v1_14_2.Protocol_1_14_2;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.earthcomputer.multiconnect.transformer.InboundTranslator;
import net.earthcomputer.multiconnect.transformer.OutboundTranslator;
import net.minecraft.SharedConstants;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

import static net.earthcomputer.multiconnect.api.Protocols.*;

public class ProtocolRegistry {

    private static Int2ObjectOpenHashMap<AbstractProtocol> protocols = new Int2ObjectOpenHashMap<>();

    public static boolean isSupported(int version) {
        return protocols.containsKey(version);
    }

    public static AbstractProtocol get(int version) {
        return protocols.get(version);
    }

    public static AbstractProtocol latest() {
        return protocols.get(SharedConstants.getGameVersion().getProtocolVersion());
    }

    private static int registeringProtocol;
    private static void register(int version, AbstractProtocol protocol) {
        registeringProtocol = version;
        protocols.put(version, protocol);
        protocol.registerTranslators();
    }

    private static Map<Class<?>, List<Pair<Integer, InboundTranslator<?>>>> inboundTranslators = new HashMap<>();
    private static Map<Class<?>, List<Pair<Integer, OutboundTranslator<?>>>> outboundTranslators = new HashMap<>();

    // usually you want type and translator to handle the same type
    public static <T> void registerInboundTranslator(Class<T> type, InboundTranslator<T> translator) {
        registerInboundTranslatorUnchecked(type, translator);
    }

    public static void registerInboundTranslatorUnchecked(Class<?> type, InboundTranslator<?> translator) {
        inboundTranslators.computeIfAbsent(type, k -> new ArrayList<>()).add(Pair.of(registeringProtocol, translator));
    }

    public static <T> void registerOutboundTranslator(Class<T> type, OutboundTranslator<T> translator) {
        outboundTranslators.computeIfAbsent(type, k -> new ArrayList<>()).add(Pair.of(registeringProtocol, translator));
    }

    @SuppressWarnings("unchecked")
    public static <STORED> List<Pair<Integer, InboundTranslator<STORED>>> getInboundTranslators(Class<?> type, int minVersion, int maxVersion) {
        List<Pair<Integer, InboundTranslator<STORED>>> translators = (List<Pair<Integer, InboundTranslator<STORED>>>) (List<?>) inboundTranslators.get(type);
        return translators == null ? Collections.emptyList() : Lists.reverse(getTranslatorRange(translators, minVersion, maxVersion));
    }

    @SuppressWarnings("unchecked")
    public static <T> List<Pair<Integer, OutboundTranslator<T>>> getOutboundTranslators(Class<T> type, int minVersion, int maxVersion) {
        List<Pair<Integer, OutboundTranslator<T>>> translators = (List<Pair<Integer, OutboundTranslator<T>>>) (List<?>) outboundTranslators.get(type);
        return translators == null ? Collections.emptyList() : getTranslatorRange(translators, minVersion, maxVersion);
    }

    private static <T> List<Pair<Integer, T>> getTranslatorRange(List<Pair<Integer, T>> translators, int minVersion, int maxVersion) {
        int minIndex = Collections.binarySearch(
                translators,
                Pair.of(minVersion, null),
                Comparator.<Pair<Integer, T>, Integer>comparing(Pair::getLeft).reversed());
        if (minIndex < 0) minIndex = -minIndex;
        int maxIndex = Collections.binarySearch(
                translators,
                Pair.of(maxVersion, null),
                Comparator.<Pair<Integer, T>, Integer>comparing(Pair::getLeft).reversed());
        if (maxIndex < 0) maxIndex = -maxIndex;
        return translators.subList(minIndex, maxIndex);
    }

    static {
        register(V1_14_4, new Protocol_1_14_4());
        register(V1_14_3, new Protocol_1_14_3());
        register(V1_14_2, new Protocol_1_14_2());
        register(V1_14_1, new Protocol_1_14_1());
        register(V1_14, new Protocol_1_14());
        register(V1_13_2, new Protocol_1_13_2());
        register(V1_13_1, new Protocol_1_13_1());
        register(V1_13, new Protocol_1_13());
    }

}
