package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.server.network.packet.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CustomPayloadC2SPacket.class)
public interface CustomPayloadC2SAccessor {

    @Accessor("channel")
    Identifier multiconnect_getChannel();

    @Accessor("data")
    PacketByteBuf multiconnect_getData();
}
