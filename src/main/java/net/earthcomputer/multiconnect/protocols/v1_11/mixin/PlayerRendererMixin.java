package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Redirect(
        method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/AbstractClientPlayer;isCrouching()Z"
        )
    )
    private boolean disableSneakPositionOffset(AbstractClientPlayer player) {
        return (ConnectionInfo.protocolVersion > Protocols.V1_11_2) && player.isCrouching();
    }
}
