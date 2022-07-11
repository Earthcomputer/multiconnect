package net.earthcomputer.multiconnect.mixin.connect;

import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientIntentionPacket.class)
public interface ClientIntentionPacketAccessor {
    @Accessor
    @Mutable
    void setProtocolVersion(int version);
}
