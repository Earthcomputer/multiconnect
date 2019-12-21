package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_12_2.ICustomPaylaodC2SPacket;
import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomPayloadC2SPacket.class)
public abstract class MixinCustomPayloadC2S implements ICustomPaylaodC2SPacket {

    @Accessor
    @Override
    public abstract Identifier getChannel();

    @Accessor
    @Override
    public abstract PacketByteBuf getData();
}
