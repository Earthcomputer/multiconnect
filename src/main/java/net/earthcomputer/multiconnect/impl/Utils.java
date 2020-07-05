package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.ImmutableMap;

import java.util.Comparator;
import java.util.function.Function;

public class Utils {
    @SafeVarargs
    public static <T, U> Comparator<T> orderBy(Function<T, U> mapper, U... order) {
        ImmutableMap.Builder<U, Integer> indexBuilder = ImmutableMap.builder();
        for (int i = 0; i < order.length; i++) {
            indexBuilder.put(order[i], i);
        }
        ImmutableMap<U, Integer> indexes = indexBuilder.build();
        Integer absent = indexes.size();
        return Comparator.comparing(val -> indexes.getOrDefault(mapper.apply(val), absent));
    }
}
