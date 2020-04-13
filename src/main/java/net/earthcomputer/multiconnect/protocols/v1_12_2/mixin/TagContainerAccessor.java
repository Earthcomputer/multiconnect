package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(TagCollection.class)
public interface TagContainerAccessor<T> {

    @Accessor("tagMap")
    void multiconnect_setEntries(Map<ResourceLocation, Tag<T>> entries);
}
