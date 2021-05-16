package net.earthcomputer.multiconnect.transformer;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TranslatorRegistry {

    private final Map<Object, List<Pair<Integer, InboundTranslator<?>>>> inboundTranslators = new HashMap<>();
    private final Map<Object, List<Pair<Integer, OutboundTranslator<?>>>> outboundTranslators = new HashMap<>();

    // usually you want type and translator to handle the same type
    public <T> TranslatorRegistry registerInboundTranslator(int version, Class<T> type, InboundTranslator<T> translator) {
        return registerInboundTranslatorComplexType(version, type, translator);
    }

    public TranslatorRegistry registerInboundTranslatorComplexType(int version, Object type, InboundTranslator<?> translator) {
        inboundTranslators.computeIfAbsent(type, k -> new ArrayList<>()).add(Pair.of(version, translator));
        return this;
    }

    public <T> TranslatorRegistry registerOutboundTranslator(int version, Class<T> type, OutboundTranslator<T> translator) {
        return registerOutboundTranslatorComplexType(version, type, translator);
    }

    public TranslatorRegistry registerOutboundTranslatorComplexType(int version, Object type, OutboundTranslator<?> translator) {
        outboundTranslators.computeIfAbsent(type, k -> new ArrayList<>()).add(Pair.of(version, translator));
        return this;
    }

    // min version inclusive, max version exclusive
    @SuppressWarnings("unchecked")
    public <STORED> List<Pair<Integer, InboundTranslator<STORED>>> getInboundTranslators(Object type, int minVersion, int maxVersion) {
        var translators = (List<Pair<Integer, InboundTranslator<STORED>>>) (List<?>) inboundTranslators.get(type);
        return translators == null ? Collections.emptyList() : Lists.reverse(getTranslatorRange(translators,
                minVersion, maxVersion));
    }

    // min version inclusive, max version exclusive
    @SuppressWarnings("unchecked")
    public <T> List<Pair<Integer, OutboundTranslator<T>>> getOutboundTranslators(Object type, int minVersion, int maxVersion) {
        var translators = (List<Pair<Integer, OutboundTranslator<T>>>) (List<?>) outboundTranslators.get(type);
        return translators == null ? Collections.emptyList() : getTranslatorRange(translators, minVersion, maxVersion);
    }

    // min version inclusive, max version exclusive
    private <T> List<Pair<Integer, T>> getTranslatorRange(List<Pair<Integer, T>> translators, int minVersion, int maxVersion) {
        int minIndex = Collections.binarySearch(
                translators,
                Pair.of(maxVersion, null),
                Comparator.<Pair<Integer, T>, Integer>comparing(Pair::getLeft).reversed());
        minIndex = minIndex < 0 ? -minIndex - 1 : minIndex + 1;
        int maxIndex = Collections.binarySearch(
                translators,
                Pair.of(minVersion, null),
                Comparator.<Pair<Integer, T>, Integer>comparing(Pair::getLeft).reversed());
        maxIndex = maxIndex < 0 ? -maxIndex - 1 : maxIndex + 1;
        return translators.subList(minIndex, maxIndex);
    }
}
