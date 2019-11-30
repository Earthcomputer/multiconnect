package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.BiMap;
import net.earthcomputer.multiconnect.impl.IInt2ObjectBiMap;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Int2ObjectBiMap;
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

    @Shadow @Final protected Int2ObjectBiMap<T> indexedEntries;
    @Shadow @Final protected BiMap<Identifier, T> entries;
    @Shadow protected Object[] randomEntries;

    @Shadow public abstract <V extends T> V set(int int_1, Identifier identifier_1, V object_1);

    @Accessor
    @Override
    public abstract int getNextId();

    @Accessor
    @Override
    public abstract void setNextId(int nextId);

    @Accessor
    @Override
    public abstract Int2ObjectBiMap<T> getIndexedEntries();

    @Accessor
    @Override
    public abstract BiMap<Identifier, T> getEntries();

    @Unique private List<Consumer<T>> unregisterListeners = new ArrayList<>(0);

    @Override
    public void register(T t, int id, Identifier name) {
        for (int remapId = getNextId(); remapId > id; remapId--) {
            T toRemap = indexedEntries.get(remapId - 1);
            //noinspection unchecked
            ((IInt2ObjectBiMap<T>) indexedEntries).remove(toRemap);
            indexedEntries.put(toRemap, remapId);
        }
        setNextId(getNextId() + 1);
        set(id, name, t);
    }

    @Override
    public void unregister(T t) {
        if (!entries.containsValue(t))
            return;

        int id = indexedEntries.getId(t);
        //noinspection unchecked
        ((IInt2ObjectBiMap<T>) indexedEntries).remove(t);
        entries.inverse().remove(t);

        for (int remapId = id; remapId < getNextId(); remapId++) {
            T toRemap = indexedEntries.get(remapId + 1);
            //noinspection unchecked
            ((IInt2ObjectBiMap<T>) indexedEntries).remove(toRemap);
            indexedEntries.put(toRemap, remapId);
        }
        setNextId(getNextId() - 1);

        randomEntries = null;

        unregisterListeners.forEach(listener -> listener.accept(t));
    }

    @Override
    public void addUnregisterListener(Consumer<T> listener) {
        unregisterListeners.add(listener);
    }

    @Override
    public SimpleRegistry<T> copy() {
        SimpleRegistry<T> newRegistry = new SimpleRegistry<>();
        for (T t : indexedEntries) {
            newRegistry.set(indexedEntries.getId(t), entries.inverse().get(t), t);
        }
        return newRegistry;
    }
}
