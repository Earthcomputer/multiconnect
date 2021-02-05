package net.earthcomputer.multiconnect.protocols.generic;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class TagRegistry<T> extends HashMap<Identifier, Set<T>> {
    private final Registry<T> registry;

    public TagRegistry(Registry<T> registry) {
        this.registry = registry;
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    @SafeVarargs
    public final void add(Tag.Identified<T> tag, T... things) {
        add(tag, Arrays.asList(things));
    }

    public final void add(Tag.Identified<T> tag, Collection<T> things) {
        Set<T> values = computeIfAbsent(tag.getId(), k -> new HashSet<>());
        for (T thing : things) {
            if (MultiConnectAPI.instance().doesServerKnow(registry, thing)) {
                values.add(thing);
            }
        }
    }

    public final void addTag(Tag.Identified<T> tag, Tag.Identified<T> otherTag) {
        Set<T> value = get(otherTag.getId());
        if (value != null) {
            add(tag, value);
        } else {
            add(tag);
        }
    }

}
