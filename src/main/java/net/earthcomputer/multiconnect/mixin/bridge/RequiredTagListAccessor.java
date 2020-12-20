package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.tag.RequiredTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RequiredTagList.class)
public interface RequiredTagListAccessor<T> {
    @Accessor
    List<RequiredTagList.TagWrapper<T>> getTags();
}
