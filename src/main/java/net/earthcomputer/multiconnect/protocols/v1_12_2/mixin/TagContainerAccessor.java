package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.common.collect.BiMap;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TagContainer.class)
public interface TagContainerAccessor<T> {

    @Accessor("entries")
    BiMap<Identifier, Tag<T>> multiconnect_getEntries();
}
