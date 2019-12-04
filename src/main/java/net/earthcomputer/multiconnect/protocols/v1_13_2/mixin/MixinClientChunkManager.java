package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
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

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Unique private static final Set<Heightmap.Type> CLIENT_HEIGHTMAPS = Arrays.stream(Heightmap.Type.values()).filter(Heightmap.Type::shouldSendToClient).collect(Collectors.toSet());

    @Unique private ThreadLocal<WorldChunk> chunk = new ThreadLocal<>();

    // ModifyVariable just to capture the chunk
    @ModifyVariable(method = "loadChunkFromPacket", ordinal = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;getSectionArray()[Lnet/minecraft/world/chunk/ChunkSection;"))
    private WorldChunk grabChunk(WorldChunk chunk) {
        this.chunk.set(chunk);
        return chunk;
    }

    @Inject(method = "loadChunkFromPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;getSectionArray()[Lnet/minecraft/world/chunk/ChunkSection;"))
    private void recalculateHeightmaps(World world, int x, int z, PacketByteBuf buf, CompoundTag tag, int verticalStripMask, boolean fullChunk, CallbackInfoReturnable<WorldChunk> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            WorldChunk chunk = this.chunk.get();
            for (ChunkSection section : chunk.getSectionArray()) {
                if (section != null) {
                    section.calculateCounts();
                }
            }
            Heightmap.populateHeightmaps(chunk, CLIENT_HEIGHTMAPS);
        }
        this.chunk.set(null);
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
