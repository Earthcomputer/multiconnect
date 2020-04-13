package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CCustomPayloadPacket.class)
public interface CustomPayloadC2SAccessor {

    @Accessor("channel")
    ResourceLocation multiconnect_getChannel();

    @Accessor("data")
    PacketBuffer multiconnect_getData();
}
