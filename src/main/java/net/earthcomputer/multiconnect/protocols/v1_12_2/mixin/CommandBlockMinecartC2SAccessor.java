package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(UpdateCommandBlockMinecartC2SPacket.class)
public interface CommandBlockMinecartC2SAccessor {

    @Accessor
    int getEntityId();

}
