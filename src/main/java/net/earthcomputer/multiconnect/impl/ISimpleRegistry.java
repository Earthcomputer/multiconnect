package net.earthcomputer.multiconnect.impl;

import com.google.common.collect.BiMap;
import net.minecraft.util.Identifier;
import net.minecraft.util.Int2ObjectBiMap;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.function.Consumer;

public interface ISimpleRegistry<T> {

    int getNextId();

    void setNextId(int nextId);

    Int2ObjectBiMap<T> getIndexedEntries();

    BiMap<Identifier, T> getEntries();

    default void register(T t, int id, Identifier name) {
        register(t, id, name, true);
    }

    void register(T t, int id, Identifier name, boolean sideEffects);

    default void unregister(T t) {
        unregister(t, true);
    }

    void unregister(T t, boolean sideEffects);

    void addRegisterListener(Consumer<T> listener);

    void addUnregisterListener(Consumer<T> listener);

    SimpleRegistry<T> copy();

    void dump();
}
