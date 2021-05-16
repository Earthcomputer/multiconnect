package net.earthcomputer.multiconnect.protocols.generic;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RegistryMutator {

    private final Map<Registry<?>, Int2ObjectMap<Consumer<ISimpleRegistry<?>>>> mutators = new HashMap<>();
    private final IntSet protocolsNeedingUpdate = new IntOpenHashSet();

    @SuppressWarnings("unchecked")
    public <T> void mutate(int protocol, Registry<T> registry, Consumer<ISimpleRegistry<T>> mutator) {
        protocolsNeedingUpdate.add(protocol);
        mutators.computeIfAbsent(registry, k -> new Int2ObjectOpenHashMap<>()).put(protocol, (Consumer<ISimpleRegistry<?>>) (Consumer<?>) mutator);
    }

    public <T extends Registry<?>> void runMutations(Iterable<T> registries) {
        // validate
        int containingCount = 0;
        for (Registry<?> registry : registries) {
            if (mutators.containsKey(registry)) {
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

        // run the mutators
        for (i = protocols.length - 1; i >= 0; i--) {
            int protocol = protocols[i];
            for (Registry<?> registry : registries) {
                var registryMutators = mutators.get(registry);
                if (registryMutators != null) {
                    var mutator = registryMutators.get(protocol);
                    if (mutator != null) {
                        mutator.accept((ISimpleRegistry<?>) registry);
                    }
                }
            }
        }
    }

}
