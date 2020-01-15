package net.earthcomputer.multiconnect.mixin;

import net.minecraft.server.network.packet.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandshakeC2SPacket.class)
public interface HandshakePacketAccessor {

    @Accessor
    void setVersion(int version);

}
