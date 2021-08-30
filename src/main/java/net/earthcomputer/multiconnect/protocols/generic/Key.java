package net.earthcomputer.multiconnect.protocols.generic;

import java.util.function.Supplier;

public final class Key<T> {
    private final String name;
    private final Supplier<T> defaultValue;
    private Key(String name, Supplier<T> defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public static <T> Key<T> create(String name) {
        return new Key<>(name, () -> null);
    }

    public static <T> Key<T> create(String name, T defaultValue) {
        return new Key<>(name, () -> defaultValue);
    }

    public static <T> Key<T> create(String name, Supplier<T> defaultValue) {
        return new Key<>(name, defaultValue);
    }

    public T getDefaultValue() {
        return defaultValue.get();
    }

    @Override
    public String toString() {
        return name;
    }
}
