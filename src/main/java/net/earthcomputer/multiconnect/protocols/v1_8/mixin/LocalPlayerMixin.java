package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.IClientPlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends AbstractClientPlayer implements IClientPlayer {
    @Shadow private boolean lastOnGround;

    @Unique private boolean multiconnect_cancelSwingPacket = false;

    public LocalPlayerMixin(ClientLevel world, GameProfile profile, @Nullable ProfilePublicKey publicKey) {
        super(world, profile, publicKey);
    }

    @Inject(method = "swing", at = @At("HEAD"), cancellable = true)
    private void checkSwingHandRate(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && multiconnect_cancelSwingPacket) {
            ci.cancel();
            multiconnect_cancelSwingPacket = false;
        }
    }

    @Redirect(method = "sendPosition", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;lastOnGround:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectLastOnGround(LocalPlayer player) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return !this.onGround; // make sure player packets are sent every tick to tick the server-side player entity
        } else {
            return this.lastOnGround;
        }
    }

    @Override
    public void multiconnect_cancelSwingPacket() {
        multiconnect_cancelSwingPacket = true;
    }
}
