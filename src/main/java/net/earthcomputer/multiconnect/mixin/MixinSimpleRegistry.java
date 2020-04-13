package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.BiMap;
import net.earthcomputer.multiconnect.impl.IIntIdentityHashBiMap;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Mixin(SimpleRegistry.class)
public abstract class MixinSimpleRegistry<T> implements ISimpleRegistry<T> {

    @Shadow @Final protected IntIdentityHashBiMap<T> underlyingIntegerMap;
    @Shadow @Final protected BiMap<ResourceLocation, T> registryObjects;
    @Shadow protected Object[] values;
    @Shadow private int nextFreeId;

    @Shadow public abstract <V extends T> V register(int int_1, ResourceLocation identifier_1, V object_1);

    @Accessor
    @Override
    public abstract int getNextFreeId();

    @Accessor
    @Override
    public abstract void setNextFreeId(int nextFreeId);

    @Accessor
    @Override
    public abstract IntIdentityHashBiMap<T> getUnderlyingIntegerMap();

    @Accessor
    @Override
    public abstract BiMap<ResourceLocation, T> getRegistryObjects();

    @Unique private List<Consumer<T>> registerListeners = new ArrayList<>(0);
    @Unique private List<Consumer<T>> unregisterListeners = new ArrayList<>(0);

    @Override
    public void register(T t, int id, ResourceLocation name, boolean sideEffects) {
        for (int remapId = getNextFreeId(); remapId > id; remapId--) {
            T toRemap = underlyingIntegerMap.getByValue(remapId - 1);
            //noinspection unchecked
            ((IIntIdentityHashBiMap<T>) underlyingIntegerMap).multiconnect_remove(toRemap);
            underlyingIntegerMap.put(toRemap, remapId);
        }
        setNextFreeId(getNextFreeId() + 1);
        register(id, name, t);

        if (sideEffects)
            registerListeners.forEach(listener -> listener.accept(t));
    }

    @Override
    public void registerInPlace(T t, int id, ResourceLocation name, boolean sideEffects) {
        if (id == getNextFreeId())
            setNextFreeId(id + 1);
        register(id, name, t);

        if (sideEffects)
            registerListeners.forEach(listener -> listener.accept(t));
    }

    @Override
    public void unregister(T t, boolean sideEffects) {
        if (!registryObjects.containsValue(t))
            return;

        int id = underlyingIntegerMap.getId(t);
        //noinspection unchecked
        ((IIntIdentityHashBiMap<T>) underlyingIntegerMap).multiconnect_remove(t);
        registryObjects.inverse().remove(t);

        for (int remapId = id; remapId < getNextFreeId() - 1; remapId++) {
            T toRemap = underlyingIntegerMap.getByValue(remapId + 1);
            //noinspection unchecked
            ((IIntIdentityHashBiMap<T>) underlyingIntegerMap).multiconnect_remove(toRemap);
            underlyingIntegerMap.put(toRemap, remapId);
        }
        setNextFreeId(getNextFreeId() - 1);

        values = null;

        if (sideEffects)
            unregisterListeners.forEach(listener -> listener.accept(t));
    }

    @Override
    public void purge(T t, boolean sideEffects) {
        if (!registryObjects.containsValue(t))
            return;

        int id = underlyingIntegerMap.getId(t);
        //noinspection unchecked
        ((IIntIdentityHashBiMap<T>) underlyingIntegerMap).multiconnect_remove(t);
        registryObjects.inverse().remove(t);

        if (id == getNextFreeId() - 1)
            setNextFreeId(id);

        values = null;

        if (sideEffects)
            unregisterListeners.forEach(listener -> listener.accept(t));
    }

    @Override
    public void clear(boolean sideEffects) {
        if (sideEffects) {
            for (int id = getNextFreeId() - 1; id >= 0; id--) {
                T value = underlyingIntegerMap.getByValue(id);
                if (value != null)
                    unregisterListeners.forEach(listener -> listener.accept(value));
            }
        }
        registryObjects.clear();
        underlyingIntegerMap.clear();
        values = null;
        setNextFreeId(0);
    }

    @Override
    public void addRegisterListener(Consumer<T> listener) {
        registerListeners.add(listener);
    }

    @Override
    public void addUnregisterListener(Consumer<T> listener) {
        unregisterListeners.add(listener);
    }

    @Override
    public SimpleRegistry<T> copy() {
        SimpleRegistry<T> newRegistry = new SimpleRegistry<>();
        for (T t : underlyingIntegerMap) {
            newRegistry.register(underlyingIntegerMap.getId(t), registryObjects.inverse().get(t), t);
        }
        return newRegistry;
    }

    @Override
    public void dump() {
        for (int id = 0; id < nextFreeId; id++) {
            try {
                T val = underlyingIntegerMap.getByValue(id);
                if (val != null)
                    System.out.println(id + ": " + registryObjects.inverse().get(val));
            } catch (Throwable t) {
                System.out.println(id + ": ERROR: " + t);
            }
        }
    }
}
