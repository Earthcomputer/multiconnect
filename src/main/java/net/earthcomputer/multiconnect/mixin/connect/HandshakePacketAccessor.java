package net.earthcomputer.multiconnect.mixin.connect;

import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandshakeC2SPacket.class)
public interface HandshakePacketAccessor {

    @Accessor
    @Mutable
    void setProtocolVersion(int version);

}
