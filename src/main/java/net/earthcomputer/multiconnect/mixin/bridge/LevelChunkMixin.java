package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin implements IBlockConnectableChunk {
    @Unique private ChunkConnector multiconnect_chunkConnector;
    @Unique private boolean multiconnect_shouldReplaceBlockEntity;

    @Inject(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void preRemoveBlockEntity(
        BlockPos pos,
        BlockState newState,
        boolean isMoving,
        CallbackInfoReturnable<BlockState> ci,
        int y,
        LevelChunkSection section,
        boolean hasOnlyAir,
        int sectionX,
        int sectionY,
        int sectionZ,
        BlockState oldState
    ) {
        multiconnect_shouldReplaceBlockEntity = ConnectionInfo.protocol.shouldBlockChangeReplaceBlockEntity(oldState.getBlock(), newState.getBlock());
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private void redirectRemoveBlockEntity(LevelChunk chunk, BlockPos pos) {
        if (multiconnect_shouldReplaceBlockEntity) {
            chunk.removeBlockEntity(pos);
        }
    }

    @Override
    public ChunkConnector multiconnect_getChunkConnector() {
        return multiconnect_chunkConnector;
    }

    @Override
    public void multiconnect_setChunkConnector(ChunkConnector chunkConnector) {
        this.multiconnect_chunkConnector = chunkConnector;
    }

}
