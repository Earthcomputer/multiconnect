package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

import java.util.Map;

public interface ITagContainer<T> {

    void multiconnect_setEntries(Map<Identifier, Tag<T>> entries);

}
