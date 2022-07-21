package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Shadow public abstract void handleSetEntityMotion(ClientboundSetEntityMotionPacket entityVelocityUpdateS2CPacket_1);

    @Inject(method = "handleAddEntity", at = @At("TAIL"))
    private void onHandleAddEntity(ClientboundAddEntityPacket packet, CallbackInfo ci) {
        // TODO: move this to new packet system
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (packet.getType() == EntityType.ITEM
                    || packet.getType() == EntityType.ARROW
                    || packet.getType() == EntityType.SPECTRAL_ARROW
                    || packet.getType() == EntityType.TRIDENT) {
                handleSetEntityMotion(new ClientboundSetEntityMotionPacket(packet.getId(),
                        new Vec3(packet.getXa(), packet.getYa(), packet.getZa())));
            }
        }
    }
}
