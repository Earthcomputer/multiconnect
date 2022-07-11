package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin extends Level {
    protected ClientLevelMixin(WritableLevelData properties, ResourceKey<Level> registryRef, Holder<DimensionType> dimension, Supplier<ProfilerFiller> profiler, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, dimension, profiler, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Inject(method = "setServerVerifiedBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", shift = At.Shift.AFTER), cancellable = true)
    private void onHandleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        multiconnect_onHandleBlockUpdate(pos, state, flags, ci);
    }

    @Inject(method = "syncBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER), cancellable = true)
    private void onHandlePendingUpdate(BlockPos pos, BlockState state, Vec3 playerPos, CallbackInfo ci) {
        multiconnect_onHandleBlockUpdate(pos, state, Block.UPDATE_ALL | Block.UPDATE_KNOWN_SHAPE, ci);
    }

    @Unique
    private void multiconnect_onHandleBlockUpdate(BlockPos pos, BlockState state, int flags, CallbackInfo ci) {
        ChunkAccess chunk = getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (chunk != null) {
            ChunkConnector connector = ((IBlockConnectableChunk) chunk).multiconnect_getChunkConnector();
            if (connector != null) {
                connector.onBlockChange(pos, state.getBlock(), true);
                ci.cancel();
            }

            BlockState currentState = getBlockState(pos);
            BlockState newState = ConnectionInfo.protocol.getActualState(this, pos, currentState);
            if (newState != currentState) {
                setBlock(pos, newState, flags, 512);
            }
        }
    }
}
