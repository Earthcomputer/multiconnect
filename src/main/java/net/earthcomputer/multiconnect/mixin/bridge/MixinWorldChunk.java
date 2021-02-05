package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements IBlockConnectableChunk {

    @Unique private ChunkConnector chunkConnector;
    @Unique private boolean shouldReplaceBlockEntity;

    @ModifyVariable(method = "setBlockState", ordinal = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 0))
    private BlockState calcShouldReplaceBlockEntity(BlockState oldState, BlockPos pos, BlockState newState, boolean bl) {
        shouldReplaceBlockEntity = ConnectionInfo.protocol.shouldBlockChangeReplaceBlockEntity(oldState.getBlock(), newState.getBlock());
        return oldState;
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;removeBlockEntity(Lnet/minecraft/util/math/BlockPos;)V"))
    private void redirectRemoveBlockEntity(WorldChunk chunk, BlockPos pos) {
        if (shouldReplaceBlockEntity)
            chunk.removeBlockEntity(pos);
    }

    @Override
    public ChunkConnector multiconnect_getChunkConnector() {
        return chunkConnector;
    }

    @Override
    public void multiconnect_setChunkConnector(ChunkConnector chunkConnector) {
        this.chunkConnector = chunkConnector;
    }

}
