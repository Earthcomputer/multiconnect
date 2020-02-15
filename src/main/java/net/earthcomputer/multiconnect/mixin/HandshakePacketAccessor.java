package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IHandshakePacket;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandshakeC2SPacket.class)
public class HandshakePacketAccessor implements IHandshakePacket {

    @Shadow
    private int version;

    @Override
    public void setProtocolVersion(int newVersion) {
        version = newVersion;
    }

}
