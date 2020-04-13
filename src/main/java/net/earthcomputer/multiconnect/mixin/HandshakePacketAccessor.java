package net.earthcomputer.multiconnect.mixin;

import net.minecraft.network.handshake.client.CHandshakePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CHandshakePacket.class)
public interface HandshakePacketAccessor {

    @Accessor
    void setProtocolVersion(int version);

}
