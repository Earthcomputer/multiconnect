package net.earthcomputer.multiconnect.mixin.bridge;

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
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(SimpleRegistry.class)
public abstract class MixinSimpleRegistry<T> extends MutableRegistry<T> implements ISimpleRegistry<T> {
    @Shadow @Final private ObjectList<T> rawIdToEntry;
    @Shadow @Final private Object2IntMap<T> entryToRawId;
    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;
    @Shadow @Final private Map<T, Lifecycle> entryToLifecycle;
    @Shadow private List<RegistryEntry.Reference<T>> field_36634;
    @Shadow private int nextId;

    @Shadow private boolean frozen;
    @Shadow private @Nullable Map<T, RegistryEntry.Reference<T>> unfrozenValueToEntry;

    @Unique private final Set<RegistryKey<T>> multiconnect_realEntries = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());

    public MixinSimpleRegistry(RegistryKey<? extends Registry<T>> registryKey, Lifecycle lifecycle) {
        super(registryKey, lifecycle);
    }

    @Override
    public void multiconnect_unfreeze() {
        if (this.frozen) {
            this.frozen = false;
            this.unfrozenValueToEntry = new IdentityHashMap<>();
            for (RegistryEntry.Reference<T> entry : this.keyToEntry.values()) {
                this.unfrozenValueToEntry.put(entry.value(), entry);
            }
        }
    }

    @Accessor("frozen")
    @Override
    public abstract boolean multiconnect_isFrozen();

    @Override
    public Set<RegistryKey<T>> multiconnect_getRealEntries() {
        return multiconnect_realEntries;
    }

    @Override
    public void multiconnect_lockRealEntries() {
        multiconnect_realEntries.clear();
        multiconnect_realEntries.addAll(keyToEntry.keySet());
    }

    @Unique private final List<IRegistryUpdateListener<T>> registerListeners = new ArrayList<>(0);
    @Unique private final List<IRegistryUpdateListener<T>> unregisterListeners = new ArrayList<>(0);

    @Override
    public void multiconnect_clear() {
        rawIdToEntry.clear();
        entryToRawId.clear();
        idToEntry.clear();
        keyToEntry.clear();
        valueToEntry.clear();
        entryToLifecycle.clear();
        field_36634 = null;
        nextId = 0;
    }

    @Override
    public void multiconnect_addRegisterListener(IRegistryUpdateListener<T> listener) {
        registerListeners.add(listener);
    }

    @Override
    public List<IRegistryUpdateListener<T>> multiconnect_getRegisterListeners() {
        return registerListeners;
    }

    @Override
    public void multiconnect_addUnregisterListener(IRegistryUpdateListener<T> listener) {
        unregisterListeners.add(listener);
    }

    @Override
    public List<IRegistryUpdateListener<T>> multiconnect_getUnregisterListeners() {
        return unregisterListeners;
    }

    @Override
    public void multiconnect_dump() {
        for (int id = 0; id < nextId; id++) {
            try {
                T val = rawIdToEntry.get(id);
                if (val != null)
                    System.out.println(id + ": " + this.valueToEntry.get(val).registryKey().getValue());
            } catch (Throwable t) {
                System.out.println(id + ": ERROR: " + t);
            }
        }
    }
}
