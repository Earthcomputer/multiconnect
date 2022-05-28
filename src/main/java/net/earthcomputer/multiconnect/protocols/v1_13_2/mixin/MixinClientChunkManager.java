package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.SPacketChunkRenderDistanceCenter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier fallingBehind, boolean normalTick, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_13_2) {
            return;
        }
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler == null) {
            return;
        }

        Entity cameraEntity = MinecraftClient.getInstance().getCameraEntity();
        if (cameraEntity == null) {
            cameraEntity = MinecraftClient.getInstance().player;
            if (cameraEntity == null) {
                return;
            }
        }

        var packet = new SPacketChunkRenderDistanceCenter();
        packet.x = MathHelper.floor(cameraEntity.getX() / 16);
        packet.z = MathHelper.floor(cameraEntity.getZ() / 16);
        PacketSystem.sendToClient(networkHandler, Protocols.V1_14, packet);
    }
}
