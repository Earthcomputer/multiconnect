package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.network.packet.s2c.play.SynchronizeTagsS2CPacket;
import net.minecraft.tag.TagManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SynchronizeTagsS2CPacket.class)
public interface SynchronizeTagsS2CAccessor {
    @Accessor
    void setTagManager(TagManager tagManager);
}
