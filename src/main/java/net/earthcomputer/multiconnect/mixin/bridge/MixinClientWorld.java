package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld extends World {
    protected MixinClientWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> dimension, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "handleBlockUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", shift = At.Shift.AFTER), cancellable = true)
    private void onHandleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        multiconnect_onHandleBlockUpdate(pos, state, flags, ci);
    }

    @Inject(method = "processPendingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z", shift = At.Shift.AFTER), cancellable = true)
    private void onHandlePendingUpdate(BlockPos pos, BlockState state, Vec3d playerPos, CallbackInfo ci) {
        multiconnect_onHandleBlockUpdate(pos, state, Block.NOTIFY_ALL | Block.FORCE_STATE, ci);
    }

    @Unique
    private void multiconnect_onHandleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        Chunk chunk = getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk != null) {
            ChunkConnector connector = ((IBlockConnectableChunk) chunk).multiconnect_getChunkConnector();
            if (connector != null) {
                connector.onBlockChange(pos, state.getBlock(), true);
                ci.cancel();
            }

            BlockState currentState = getBlockState(pos);
            BlockState newState = ConnectionInfo.protocol.getActualState(this, pos, currentState);
            if (newState != currentState) {
                setBlockState(pos, newState, flags, 512);
            }
        }
    }
}
