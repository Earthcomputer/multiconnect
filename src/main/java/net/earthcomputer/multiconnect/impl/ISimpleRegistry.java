package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.BiMap;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.Consumer;

public interface ISimpleRegistry<T> {

    int getNextFreeId();

    void setNextFreeId(int nextId);

    IntIdentityHashBiMap<T> getUnderlyingIntegerMap();

    BiMap<ResourceLocation, T> getRegistryObjects();

    default void register(T t, int id, ResourceLocation name) {
        register(t, id, name, true);
    }

    void register(T t, int id, ResourceLocation name, boolean sideEffects);

    default void registerInPlace(T t, int id, ResourceLocation name) {
        registerInPlace(t, id, name, true);
    }

    void registerInPlace(T t, int id, ResourceLocation name, boolean sideEffects);

    default void unregister(T t) {
        unregister(t, true);
    }

    void unregister(T t, boolean sideEffects);

    default void purge(T t) {
        purge(t, true);
    }

    void purge(T t, boolean sideEffects);

    void clear(boolean sideEffects);

    void addRegisterListener(Consumer<T> listener);

    void addUnregisterListener(Consumer<T> listener);

    SimpleRegistry<T> copy();

    void dump();
}
