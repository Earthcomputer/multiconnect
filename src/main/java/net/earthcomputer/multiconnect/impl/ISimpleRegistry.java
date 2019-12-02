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

    void register(T t, int id, Identifier name);

    void unregister(T t);

    void addRegisterListener(Consumer<T> listener);

    void addUnregisterListener(Consumer<T> listener);

    SimpleRegistry<T> copy();
}
