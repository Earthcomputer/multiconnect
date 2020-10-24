package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityAttachS2CPacket.class)
public interface EntityAttachS2CAccessor {
    @Accessor
    void setAttachedId(int attachedId);

    @Accessor
    void setHoldingId(int holdingId);
}
