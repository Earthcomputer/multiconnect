package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow public abstract void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket_1);

    @Inject(method = "onEntitySpawn", at = @At("TAIL"))
    private void onOnEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        // TODO: move this to new packet system
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (packet.getEntityTypeId() == EntityType.ITEM
                    || packet.getEntityTypeId() == EntityType.ARROW
                    || packet.getEntityTypeId() == EntityType.SPECTRAL_ARROW
                    || packet.getEntityTypeId() == EntityType.TRIDENT) {
                onEntityVelocityUpdate(new EntityVelocityUpdateS2CPacket(packet.getId(),
                        new Vec3d(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ())));
            }
        }
    }
}
