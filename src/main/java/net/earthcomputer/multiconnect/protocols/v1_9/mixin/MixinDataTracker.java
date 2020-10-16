package net.earthcomputer.multiconnect.protocols.v1_9.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DataTracker.class)
public class MixinDataTracker {
    @Redirect(method = "deserializePacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readVarInt()I"))
    private static int redirectReadVarInt(PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_9) {
            return buf.readUnsignedByte();
        } else {
            return buf.readVarInt();
        }
    }
}
