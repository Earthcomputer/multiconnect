package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LoginSuccessS2CPacket.class)
public class MixinLoginSuccessS2CPacket {
    @Shadow
    private GameProfile profile;

    @Inject(method = "read", at = @At("HEAD"), cancellable = true)
    public void onRead(PacketByteBuf buf, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            String string = buf.readString(36);
            String string2 = buf.readString(16);
            UUID uUID = UUID.fromString(string);
            profile = new GameProfile(uUID, string2);

            ci.cancel();
        }
    }
}
