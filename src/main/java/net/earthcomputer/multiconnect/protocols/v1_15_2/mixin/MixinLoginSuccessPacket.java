package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

// TODO: fix login packets on the network layer
@Mixin(LoginSuccessS2CPacket.class)
public class MixinLoginSuccessPacket {
    private int[] multiconnect_uuid;
    private int multiconnect_uuidIndex;

    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readInt()I", ordinal = 0))
    private int onReadUuid(PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_15_2) {
            return buf.readInt();
        }
        if (multiconnect_uuidIndex == 0) {
            String uuidStr = buf.readString(36);
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                uuid = Util.NIL_UUID;
            }
            multiconnect_uuid = DynamicSerializableUuid.toIntArray(uuid);
        }

        return multiconnect_uuid[multiconnect_uuidIndex++];
    }
}
