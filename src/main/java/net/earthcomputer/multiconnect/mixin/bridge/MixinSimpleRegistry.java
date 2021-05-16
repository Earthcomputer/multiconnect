package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.earthcomputer.multiconnect.protocols.generic.IRegistryUpdateListener;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(SimpleRegistry.class)
public abstract class MixinSimpleRegistry<T> extends MutableRegistry<T> implements ISimpleRegistry<T> {

    @Shadow @Final private ObjectList<T> rawIdToEntry;
    @Shadow @Final private Object2IntMap<T> entryToRawId;
    @Shadow @Final private BiMap<Identifier, T> idToEntry;
    @Shadow @Final private BiMap<RegistryKey<T>, T> keyToEntry;
    @Shadow @Final private Map<T, Lifecycle> entryToLifecycle;
    @Shadow protected Object[] randomEntries;
    @Shadow private int nextId;

    @Unique private final Set<RegistryKey<T>> realEntries = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());

    public MixinSimpleRegistry(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
        super(registryKey, lifecycle);
    }

    @Shadow public abstract <V extends T> V set(int rawId, RegistryKey<T> id, V value, Lifecycle lifecycle);

    @Accessor
    @Override
    public abstract int getNextId();

    @Accessor
    @Override
    public abstract void setNextId(int nextId);

    @SuppressWarnings("unchecked")
    @Override
    public RegistryKey<Registry<T>> getRegistryKey() {
        return (RegistryKey<Registry<T>>) getKey();
    }

    @Accessor
    @Override
    public abstract ObjectList<T> getRawIdToEntry();

    @Accessor
    @Override
    public abstract Object2IntMap<T> getEntryToRawId();

    @Accessor
    @Override
    public abstract BiMap<Identifier, T> getIdToEntry();

    @Accessor
    @Override
    public abstract BiMap<RegistryKey<T>, T> getKeyToEntry();

    @Accessor
    @Override
    public abstract Map<T, Lifecycle> getEntryToLifecycle();

    @Accessor
    @Override
    public abstract void setRandomEntries(Object[] randomEntries);

    @Override
    public Set<RegistryKey<T>> getRealEntries() {
        return realEntries;
    }

    @Override
    public void lockRealEntries() {
        realEntries.clear();
        realEntries.addAll(keyToEntry.keySet());
    }

    @Unique private final List<IRegistryUpdateListener<T>> registerListeners = new ArrayList<>(0);
    @Unique private final List<IRegistryUpdateListener<T>> unregisterListeners = new ArrayList<>(0);

    @Override
    public void register(T t, int id, RegistryKey<T> key, boolean sideEffects) {
        // add null values if we're higher than the entry list size
        while (id > rawIdToEntry.size()) {
            rawIdToEntry.add(null);
        }

        // id shift
        for (int remapId = getNextId(); remapId > id; remapId--) {
            T toRemap = rawIdToEntry.get(remapId - 1);
            if (toRemap != null) {
                entryToRawId.put(toRemap, remapId);
            }
        }
        rawIdToEntry.add(id, null);
        setNextId(getNextId() + 1);

        // now we've made room, replace the value at this id
        set(id, key, t, Lifecycle.stable());

        if (sideEffects)
            registerListeners.forEach(listener -> listener.onUpdate(t, false));
    }

    @Override
    public void registerInPlace(T t, int id, RegistryKey<T> key, boolean sideEffects) {
        if (id == getNextId())
            setNextId(id + 1);
        set(id, key, t, Lifecycle.stable());

        if (sideEffects)
            registerListeners.forEach(listener -> listener.onUpdate(t, true));
    }

    @Override
    public void unregister(T t, boolean sideEffects) {
        if (!idToEntry.containsValue(t))
            return;

        int id = entryToRawId.removeInt(t);
        idToEntry.inverse().remove(t);
        keyToEntry.inverse().remove(t);
        entryToLifecycle.remove(t);

        // id shift
        for (int remapId = id; remapId < getNextId() - 1; remapId++) {
            T toRemap = rawIdToEntry.get(remapId + 1);
            if (toRemap != null) {
                entryToRawId.put(toRemap, remapId);
            }
        }
        rawIdToEntry.remove(id);
        setNextId(getNextId() - 1);

        randomEntries = null;

        if (sideEffects)
            unregisterListeners.forEach(listener -> listener.onUpdate(t, false));
    }

    @Override
    public void purge(T t, boolean sideEffects) {
        if (!idToEntry.containsValue(t))
            return;

        int id = entryToRawId.removeInt(t);
        rawIdToEntry.set(id, null);
        idToEntry.inverse().remove(t);
        keyToEntry.inverse().remove(t);
        entryToLifecycle.remove(t);

        if (id == getNextId() - 1)
            setNextId(id);

        randomEntries = null;

        if (sideEffects)
            unregisterListeners.forEach(listener -> listener.onUpdate(t, true));
    }

    @Override
    public void clear(boolean sideEffects) {
        if (sideEffects) {
            for (int id = getNextId() - 1; id >= 0; id--) {
                T value = rawIdToEntry.get(id);
                if (value != null)
                    unregisterListeners.forEach(listener -> listener.onUpdate(value, false));
            }
        }
        rawIdToEntry.clear();
        entryToRawId.clear();
        idToEntry.clear();
        keyToEntry.clear();
        entryToLifecycle.clear();
        randomEntries = null;
        setNextId(0);
    }

    @Override
    public void onRestore(Iterable<T> added, Iterable<T> removed) {
        registerListeners.forEach(listener -> {
            if (listener.callOnRestore()) {
                added.forEach(thing -> listener.onUpdate(thing, false));
            }
        });
        unregisterListeners.forEach(listener -> {
            if (listener.callOnRestore()) {
                removed.forEach(thing -> listener.onUpdate(thing, false));
            }
        });
    }

    @Override
    public void addRegisterListener(IRegistryUpdateListener<T> listener) {
        registerListeners.add(listener);
    }

    @Override
    public void addUnregisterListener(IRegistryUpdateListener<T> listener) {
        unregisterListeners.add(listener);
    }

    @Override
    public SimpleRegistry<T> copy() {
        SimpleRegistry<T> newRegistry = new SimpleRegistry<>(getRegistryKey(), getLifecycle());
        for (var entry : keyToEntry.entrySet()) {
            newRegistry.set(entryToRawId.getInt(entry.getValue()), entry.getKey(), entry.getValue(), entryToLifecycle.get(entry.getValue()));
        }
        return newRegistry;
    }

    @Override
    public void dump() {
        for (int id = 0; id < nextId; id++) {
            try {
                T val = rawIdToEntry.get(id);
                if (val != null)
                    System.out.println(id + ": " + idToEntry.inverse().get(val));
            } catch (Throwable t) {
                System.out.println(id + ": ERROR: " + t);
            }
        }
    }
}
