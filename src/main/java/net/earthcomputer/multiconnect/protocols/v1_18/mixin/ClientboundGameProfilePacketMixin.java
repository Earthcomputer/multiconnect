package net.earthcomputer.multiconnect.protocols.v1_18.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ClientboundGameProfilePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

// TODO: fix login packets on the network layer
@Mixin(ClientboundGameProfilePacket.class)
public class ClientboundGameProfilePacketMixin {
    @Redirect(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;readGameProfile()Lcom/mojang/authlib/GameProfile;"))
    private GameProfile onReadGameProfile(FriendlyByteBuf buf) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            String uuidStr = buf.readUtf(36);
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                uuid = Util.NIL_UUID;
            }
            String name = buf.readUtf(16);
            return new GameProfile(uuid, name);
        } else if (ConnectionInfo.protocolVersion <= Protocols.V1_18_2) {
            int[] uuidInts = new int[4];
            for (int i = 0; i < 4; i++) {
                uuidInts[i] = buf.readInt();
            }
            UUID uuid = UUIDUtil.uuidFromIntArray(uuidInts);
            String name = buf.readUtf(16);
            return new GameProfile(uuid, name);
        } else {
            return buf.readGameProfile();
        }
    }
}
