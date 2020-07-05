package net.earthcomputer.multiconnect.impl;

import java.util.Comparator;
import java.util.function.Function;

public interface IUtils {
    @SuppressWarnings("unchecked")
    default <T, U> Comparator<T> orderBy(Function<T, U> mapper, U... order) {
        return Utils.orderBy(mapper, order);
    }
}
