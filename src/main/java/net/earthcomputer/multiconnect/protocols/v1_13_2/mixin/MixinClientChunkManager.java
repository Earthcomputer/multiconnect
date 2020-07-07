package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.PendingChunkDataPackets;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BooleanSupplier;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(method = "loadChunkFromPacket", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 0, remap = false), cancellable = true)
    private void cancelErrorMessage(int x, int z, BiomeArray biomeArray, PacketByteBuf buf, CompoundTag tag, int verticalStripMask, boolean bl, CallbackInfoReturnable<WorldChunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2 && !PendingChunkDataPackets.isProcessingQueuedPackets())
            ci.setReturnValue(null);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier fallingBehind, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getCameraEntity() != null
                && MinecraftClient.getInstance().getCameraEntity() != MinecraftClient.getInstance().player
                && MinecraftClient.getInstance().getCameraEntity().isAlive()) {
            Protocol_1_13_2.updateCameraPosition();
        }
    }

}
