package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.ChunkConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectableChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk implements IBlockConnectableChunk {

    @Shadow @Final private World world;
    @Shadow @Final private ChunkPos pos;
    @Unique private ChunkConnector chunkConnector;
    @Unique private boolean shouldReplaceBlockEntity;

    @ModifyVariable(method = "setBlockState", ordinal = 1, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getBlock()Lnet/minecraft/block/Block;", ordinal = 0))
    private BlockState calcShouldReplaceBlockEntity(BlockState oldState, BlockPos pos, BlockState newState, boolean bl) {
        shouldReplaceBlockEntity = ConnectionInfo.protocol.shouldBlockChangeReplaceBlockEntity(oldState.getBlock(), newState.getBlock());
        return oldState;
    }

    @Redirect(method = "setBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlockEntity(Lnet/minecraft/util/math/BlockPos;)V"))
    private void redirectRemoveBlockEntity(World world, BlockPos pos) {
        if (shouldReplaceBlockEntity)
            world.removeBlockEntity(pos);
    }

    @Inject(method = "setBlockState", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/chunk/WorldChunk;shouldSave:Z"))
    private void connectBlocks(BlockPos pos, BlockState state, boolean moved, CallbackInfoReturnable<BlockState> ci) {
        if (chunkConnector != null) {
            chunkConnector.onBlockChange(pos, state.getBlock());
        }
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
