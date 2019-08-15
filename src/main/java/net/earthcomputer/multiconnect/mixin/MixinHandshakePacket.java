package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IHandshakePacket;
import net.minecraft.server.network.packet.HandshakeC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandshakeC2SPacket.class)
public abstract class MixinHandshakePacket implements IHandshakePacket {

    @Accessor
    @Override
    public abstract void setVersion(int version);
}
