package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
    @Shadow private ClientLevel level;

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void onLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            if (eventId == Protocol_1_8.LEVEL_EVENT_QUIET_GHAST_SHOOT) {
                level.playLocalSound(pos, SoundEvents.GHAST_SHOOT, SoundSource.HOSTILE, 2, (level.random.nextFloat() - level.random.nextFloat()) * 0.2f + 1, false);
                ci.cancel();
            }
        }
    }
}
