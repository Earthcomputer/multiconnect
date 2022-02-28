package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class TagRegistry<T> extends HashMap<TagKey<T>, Set<T>> {
    private final Registry<T> registry;

    public TagRegistry(Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    @SafeVarargs
    public final void add(TagKey<T> tag, T... things) {
        add(tag, Arrays.asList(things));
    }

    public final void add(TagKey<T> tag, Collection<T> things) {
        Set<T> values = computeIfAbsent(tag, k -> new HashSet<>());
        for (T thing : things) {
            if (MultiConnectAPI.instance().doesServerKnow(registry, thing)) {
                values.add(thing);
            }
        }
    }

    public final void addTag(TagKey<T> tag, TagKey<T> otherTag) {
        Set<T> value = get(otherTag);
        if (value != null) {
            add(tag, value);
        } else {
            add(tag);
        }
    }

}
