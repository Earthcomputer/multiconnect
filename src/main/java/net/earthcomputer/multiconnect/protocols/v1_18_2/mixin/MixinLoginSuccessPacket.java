package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import com.mojang.authlib.GameProfile;
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
    @Redirect(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readGameProfile()Lcom/mojang/authlib/GameProfile;"))
    private GameProfile onReadGameProfile(PacketByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            String uuidStr = buf.readString(36);
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                uuid = Util.NIL_UUID;
            }
            String name = buf.readString(16);
            return new GameProfile(uuid, name);
        } else if (ConnectionInfo.protocolVersion <= Protocols.V1_18_2) {
            int[] uuidInts = new int[4];
            for (int i = 0; i < 4; i++) {
                uuidInts[i] = buf.readInt();
            }
            UUID uuid = DynamicSerializableUuid.toUuid(uuidInts);
            String name = buf.readString(16);
            return new GameProfile(uuid, name);
        } else {
            return buf.readGameProfile();
        }
    }
}
