package net.earthcomputer.multiconnect.protocols.generic;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RegistryMutator {
    private final Map<RegistryKey<? extends Registry<?>>, Int2ObjectMap<Consumer<RegistryBuilder<?>>>> mutators = new HashMap<>();
    private final IntSet protocolsNeedingUpdate = new IntOpenHashSet();

    @SuppressWarnings("unchecked")
    public <T> void mutate(int protocol, RegistryKey<? extends Registry<T>> registry, Consumer<RegistryBuilder<T>> mutator) {
        protocolsNeedingUpdate.add(protocol);
        mutators.computeIfAbsent(registry, k -> new Int2ObjectOpenHashMap<>()).put(protocol, (Consumer<RegistryBuilder<?>>) (Consumer<?>) mutator);
    }

    public <T extends SimpleRegistry<?>> void runMutations(Iterable<T> registries) {
        // validate
        int containingCount = 0;
        for (Registry<?> registry : registries) {
            if (mutators.containsKey(registry.getKey())) {
                containingCount++;
            }
        }
        if (mutators.size() > containingCount) {
            throw new IllegalStateException("Tried to mutate an unmodifiable registry");
        }

        // get protocols needing update
        int[] protocols = new int[protocolsNeedingUpdate.size()];
        int i = 0;
        IntIterator itr = protocolsNeedingUpdate.iterator();
        while (itr.hasNext()) {
            protocols[i++] = itr.nextInt();
        }
        Arrays.sort(protocols);

        var builders = new HashMap<Registry<?>, RegistryBuilder<?>>();
        RegistryBuilderSupplier builderSupplier = new RegistryBuilderSupplier() {
            @SuppressWarnings("unchecked")
            @Override
            public <U> RegistryBuilder<U> get(RegistryKey<? extends Registry<U>> key) {
                Registry<U> registry = getRegistry(key);
                return (RegistryBuilder<U>) builders.computeIfAbsent(registry,
                        k -> makeBuilder(registry, this));
            }
        };

        // run the mutators
        for (i = protocols.length - 1; i >= 0; i--) {
            int protocol = protocols[i];
            for (Registry<?> registry : registries) {
                var registryMutators = mutators.get(registry.getKey());
                if (registryMutators != null) {
                    RegistryBuilder<?> builder = builders.computeIfAbsent(registry, k -> makeBuilder(registry, builderSupplier));
                    var mutator = registryMutators.get(protocol);
                    if (mutator != null) {
                        mutator.accept(builder);
                    }
                }
            }
        }

        for (RegistryBuilder<?> builder : builders.values()) {
            builder.apply();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Registry<?>> T getRegistry(RegistryKey<T> registryKey) {
        return ((Registry<T>) Registry.REGISTRIES).get(registryKey);
    }

    @SuppressWarnings("unchecked")
    private static <T> RegistryBuilder<T> makeBuilder(Registry<?> registry, RegistryBuilderSupplier builderSupplier) {
        return new RegistryBuilder<>((SimpleRegistry<T>) registry, builderSupplier);
    }

    @FunctionalInterface
    public interface RegistryBuilderSupplier {
        <T> RegistryBuilder<T> get(RegistryKey<? extends Registry<T>> key);
    }
}
