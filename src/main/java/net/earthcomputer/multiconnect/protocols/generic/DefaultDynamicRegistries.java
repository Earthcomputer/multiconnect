package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultDynamicRegistries<T> {
    private static final Map<RegistryKey<? extends Registry<?>>, DefaultDynamicRegistries<?>> INSTANCES = new HashMap<>();

    private final RegistryKey<? extends Registry<T>> registry;
    private final Map<RegistryKey<T>, RegistryKey<T>> newToOldMappings = new HashMap<>();

    private DefaultDynamicRegistries(RegistryKey<? extends Registry<T>> registry) {
        this.registry = registry;
    }

    @SuppressWarnings("unchecked")
    public static <T> DefaultDynamicRegistries<T> getInstance(RegistryKey<? extends Registry<T>> registry) {
        DefaultDynamicRegistries<?> instance = INSTANCES.get(registry);
        if (instance == null) {
            instance = new DefaultDynamicRegistries<>(registry);
            INSTANCES.put(registry, instance);
        }
        return (DefaultDynamicRegistries<T>) instance;
    }

    public static Collection<DefaultDynamicRegistries<?>> getInstances() {
        return INSTANCES.values();
    }

    public void add(String oldKey, RegistryKey<T> newKey) {
        add(RegistryKey.of(registry, new Identifier(oldKey)), newKey);
    }

    public void add(RegistryKey<T> oldKey, RegistryKey<T> newKey) {
        newToOldMappings.put(newKey, oldKey);
    }

    public RegistryKey<T> getOld(RegistryKey<T> newKey) {
        return newToOldMappings.getOrDefault(newKey, newKey);
    }

    public void clear() {
        newToOldMappings.clear();
    }
}
