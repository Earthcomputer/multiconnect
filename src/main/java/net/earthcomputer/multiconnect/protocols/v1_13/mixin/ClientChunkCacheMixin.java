package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.SPacketChunkRenderDistanceCenter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier fallingBehind, boolean normalTick, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2) {
            return;
        }
        ClientPacketListener networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler == null) {
            return;
        }

        Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            cameraEntity = Minecraft.getInstance().player;
            if (cameraEntity == null) {
                return;
            }
        }

        var packet = new SPacketChunkRenderDistanceCenter();
        packet.x = Mth.floor(cameraEntity.getX() / 16);
        packet.z = Mth.floor(cameraEntity.getZ() / 16);
        PacketSystem.sendToClient(networkHandler, Protocols.V1_14, packet);
    }
}
