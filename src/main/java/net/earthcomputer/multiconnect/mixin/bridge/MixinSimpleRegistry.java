package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.earthcomputer.multiconnect.protocols.generic.IRegistryUpdateListener;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.minecraft.util.Identifier;
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

@Mixin(SimpleRegistry.class)
public abstract class MixinSimpleRegistry<T> extends MutableRegistry<T> implements ISimpleRegistry<T> {

    @Shadow @Final private ObjectList<T> field_26682;
    @Shadow @Final private Object2IntMap<T> field_26683;
    @Shadow @Final private BiMap<Identifier, T> entriesById;
    @Shadow @Final private BiMap<RegistryKey<T>, T> entriesByKey;
    @Shadow protected Object[] randomEntries;
    @Shadow private int nextId;

    public MixinSimpleRegistry(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
        super(registryKey, lifecycle);
    }

    @Shadow public abstract <V extends T> V set(int rawId, RegistryKey<T> id, V value);

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

    @Accessor("field_26682")
    @Override
    public abstract ObjectList<T> getAllEntries();

    @Accessor("field_26683")
    @Override
    public abstract Object2IntMap<T> getEntryIds();

    @Accessor
    @Override
    public abstract BiMap<Identifier, T> getEntriesById();

    @Accessor
    @Override
    public abstract BiMap<RegistryKey<T>, T> getEntriesByKey();

    @Unique private final List<IRegistryUpdateListener<T>> registerListeners = new ArrayList<>(0);
    @Unique private final List<IRegistryUpdateListener<T>> unregisterListeners = new ArrayList<>(0);

    @Override
    public void register(T t, int id, RegistryKey<T> key, boolean sideEffects) {
        // add null values if we're higher than the entry list size
        while (id > field_26682.size()) {
            field_26682.add(null);
        }

        // id shift
        for (int remapId = getNextId(); remapId > id; remapId--) {
            T toRemap = field_26682.get(remapId - 1);
            if (toRemap != null) {
                field_26683.put(toRemap, remapId);
            }
        }
        field_26682.add(id, null);
        setNextId(getNextId() + 1);

        // now we've made room, replace the value at this id
        set(id, key, t);

        if (sideEffects)
            registerListeners.forEach(listener -> listener.onUpdate(t, false));
    }

    @Override
    public void registerInPlace(T t, int id, RegistryKey<T> key, boolean sideEffects) {
        if (id == getNextId())
            setNextId(id + 1);
        set(id, key, t);

        if (sideEffects)
            registerListeners.forEach(listener -> listener.onUpdate(t, true));
    }

    @Override
    public void unregister(T t, boolean sideEffects) {
        if (!entriesById.containsValue(t))
            return;

        int id = field_26683.removeInt(t);
        entriesById.inverse().remove(t);
        entriesByKey.inverse().remove(t);

        // id shift
        for (int remapId = id; remapId < getNextId() - 1; remapId++) {
            T toRemap = field_26682.get(remapId + 1);
            if (toRemap != null) {
                field_26683.put(toRemap, remapId);
            }
        }
        field_26682.remove(id);
        setNextId(getNextId() - 1);

        randomEntries = null;

        if (sideEffects)
            unregisterListeners.forEach(listener -> listener.onUpdate(t, false));
    }

    @Override
    public void purge(T t, boolean sideEffects) {
        if (!entriesById.containsValue(t))
            return;

        int id = field_26683.removeInt(t);
        field_26682.set(id, null);
        entriesById.inverse().remove(t);
        entriesByKey.inverse().remove(t);

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
                T value = field_26682.get(id);
                if (value != null)
                    unregisterListeners.forEach(listener -> listener.onUpdate(value, false));
            }
        }
        field_26682.clear();
        field_26683.clear();
        entriesById.clear();
        entriesByKey.clear();
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

    @SuppressWarnings("unchecked")
    @Override
    public SimpleRegistry<T> copy() {
        SimpleRegistry<T> newRegistry = new SimpleRegistry<>(getRegistryKey(), ((RegistryAccessor<T>) this).getLifecycle());
        for (Map.Entry<RegistryKey<T>, T> entry : entriesByKey.entrySet()) {
            newRegistry.set(field_26683.getInt(entry.getValue()), entry.getKey(), entry.getValue());
        }
        return newRegistry;
    }

    @Override
    public void dump() {
        for (int id = 0; id < nextId; id++) {
            try {
                T val = field_26682.get(id);
                if (val != null)
                    System.out.println(id + ": " + entriesById.inverse().get(val));
            } catch (Throwable t) {
                System.out.println(id + ": ERROR: " + t);
            }
        }
    }
}
