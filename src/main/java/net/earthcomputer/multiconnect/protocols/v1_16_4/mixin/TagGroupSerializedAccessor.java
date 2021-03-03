package net.earthcomputer.multiconnect.protocols.v1_16_4.mixin;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TagGroup.Serialized.class)
public interface TagGroupSerializedAccessor {
    @Invoker("<init>")
    static TagGroup.Serialized createTagGroupSerialized(Map<Identifier, IntList> map) {
        return MixinHelper.fakeInstance();
    }
}
