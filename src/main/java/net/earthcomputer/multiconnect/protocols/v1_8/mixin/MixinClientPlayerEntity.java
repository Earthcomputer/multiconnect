package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.mojang.authlib.GameProfile;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.IClientPlayer;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity implements IClientPlayer {
    @Shadow private boolean lastOnGround;

    @Unique private boolean areSwingsCanceledThisTick = false;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    private void checkSwingHandRate(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && areSwingsCanceledThisTick) {
            // the first tick of hand swinging, we may have sent the hand swing earlier in 1.8
            ci.cancel();
        }
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;lastOnGround:Z", opcode = Opcodes.GETFIELD))
    private boolean redirectLastOnGround(ClientPlayerEntity player) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return !this.onGround; // make sure player packets are sent every tick to tick the server-side player entity
        } else {
            return this.lastOnGround;
        }
    }

    @Override
    public void multiconnect_cancelSwingsThisTick() {
        areSwingsCanceledThisTick = true;
    }

    @Override
    public void multiconnect_uncancelSwings() {
        areSwingsCanceledThisTick = false;
    }
}
