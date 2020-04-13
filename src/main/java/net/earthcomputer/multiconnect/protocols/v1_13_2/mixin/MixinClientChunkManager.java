package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.PendingChunkDataPackets;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@Mixin(ClientChunkProvider.class)
public class MixinClientChunkManager {

    @Unique private static final Set<Heightmap.Type> CLIENT_HEIGHTMAPS = Arrays.stream(Heightmap.Type.values()).filter(Heightmap.Type::isUsageClient).collect(Collectors.toSet());

    @Unique private ThreadLocal<Chunk> chunk = new ThreadLocal<>();
    @Unique private int lastCenterX;
    @Unique private int lastCenterZ;

    @Inject(method = "loadChunk", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", ordinal = 0, remap = false), cancellable = true)
    private void cancelErrorMessage(int x, int z, BiomeContainer biomeArray, PacketBuffer buf, CompoundNBT tag, int verticalStripMask, CallbackInfoReturnable<Chunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2 && !PendingChunkDataPackets.isProcessingQueuedPackets())
            ci.setReturnValue(null);
    }

    // ModifyVariable just to capture the chunk
    @ModifyVariable(method = "loadChunk", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getSections()[Lnet/minecraft/world/chunk/ChunkSection;"))
    private Chunk grabChunk(Chunk chunk) {
        this.chunk.set(chunk);
        return chunk;
    }

    @Inject(method = "loadChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;getSections()[Lnet/minecraft/world/chunk/ChunkSection;"))
    private void recalculateHeightmaps(int x, int z, BiomeContainer biomeArray, PacketBuffer buf, CompoundNBT tag, int verticalStripMask, CallbackInfoReturnable<Chunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            Chunk chunk = this.chunk.get();
            for (ChunkSection section : chunk.getSections()) {
                if (section != null) {
                    section.recalculateRefCounts();
                }
            }
            Heightmap.updateChunkHeightmaps(chunk, CLIENT_HEIGHTMAPS);
        }
        this.chunk.set(null);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier fallingBehind, CallbackInfo ci) {
        if (Minecraft.getInstance().getRenderViewEntity() != null
                && Minecraft.getInstance().getRenderViewEntity() != Minecraft.getInstance().player
                && Minecraft.getInstance().getRenderViewEntity().isAlive()) {
            Protocol_1_13_2.updateCameraPosition();
        }
    }

    @Inject(method = "setCenter", at = @At("TAIL"))
    private void onSetChunkMapCenter(int x, int z, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            if (x != lastCenterX || z != lastCenterZ) {
                lastCenterX = x;
                lastCenterZ = z;
                assert Minecraft.getInstance().getConnection() != null;
                PendingChunkDataPackets.processPackets(Minecraft.getInstance().getConnection()::handleChunkData);
            }
        }
    }

}
