package net.earthcomputer.multiconnect.protocols.generic;

import com.google.common.collect.BiMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

public interface ISimpleRegistry<T> {

    int getNextId();

    void setNextId(int nextId);

    RegistryKey<Registry<T>> getRegistryKey();

    ObjectList<T> getAllEntries();

    Object2IntMap<T> getEntryIds();

    BiMap<Identifier, T> getEntriesById();

    BiMap<RegistryKey<T>, T> getEntriesByKey();

    default void register(T t, int id, RegistryKey<T> key) {
        register(t, id, key, true);
    }

    void register(T t, int id, RegistryKey<T> key, boolean sideEffects);

    default void registerInPlace(T t, int id, RegistryKey<T> key) {
        registerInPlace(t, id, key, true);
    }

    void registerInPlace(T t, int id, RegistryKey<T> key, boolean sideEffects);

    default void unregister(T t) {
        unregister(t, true);
    }

    void unregister(T t, boolean sideEffects);

    default void purge(T t) {
        purge(t, true);
    }

    void purge(T t, boolean sideEffects);

    void clear(boolean sideEffects);

    void onRestore(Iterable<T> added, Iterable<T> removed);

    void addRegisterListener(IRegistryUpdateListener<T> listener);

    void addUnregisterListener(IRegistryUpdateListener<T> listener);

    SimpleRegistry<T> copy();

    void dump();
}
