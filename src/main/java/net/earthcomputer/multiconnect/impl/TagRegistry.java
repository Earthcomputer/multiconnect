package net.earthcomputer.multiconnect.impl;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.*;

public class TagRegistry<T> extends HashMap<Identifier, Set<T>> {

    @SafeVarargs
    public final void add(Tag.Identified<T> tag, T... things) {
        add(tag, Arrays.asList(things));
    }

    public final void add(Tag.Identified<T> tag, Collection<T> things) {
        computeIfAbsent(tag.getId(), k -> new HashSet<>()).addAll(things);
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
