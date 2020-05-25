package net.earthcomputer.multiconnect.impl;

@FunctionalInterface
public interface IRegistryUpdateListener<T> {
    void onUpdate(T thing, boolean inPlace);

    default boolean callOnRestore() {
        return false;
    }
}
