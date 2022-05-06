package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.tag.TagPacketSerializer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(TagPacketSerializer.Serialized.class)
public interface TagPacketSerializerSerializedAccessor {
    @Invoker("<init>")
    static TagPacketSerializer.Serialized createSerialized(Map<Identifier, IntList> contents) {
        return MixinHelper.fakeInstance();
    }
}
