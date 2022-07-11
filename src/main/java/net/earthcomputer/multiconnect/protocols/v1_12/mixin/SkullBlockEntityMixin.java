package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.ISkullBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public abstract class SkullBlockEntityMixin extends BlockEntity implements ISkullBlockEntity {
    @Unique private int multiconnect_skullType;
    @Unique private int multiconnect_rotation;

    public SkullBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(CompoundTag tag, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            this.multiconnect_skullType = tag.getByte("SkullType");
            this.multiconnect_rotation = tag.getByte("Rot");
            if (level != null) {
                level.setBlock(worldPosition, multiconnect_getActualState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    @Override
    public BlockState multiconnect_getActualState() {
        BlockState state = getBlockState();
        if (!getType().isValid(state))
            return state;

        int skullType = this.multiconnect_skullType;
        if (skullType < 0 || skullType > 5)
            skullType = 0;

        final Block[] skullBlocks = {Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_HEAD, Blocks.PLAYER_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD};
        final Block[] wallSkullBlocks = {Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.ZOMBIE_WALL_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_WALL_HEAD};
        assert level != null;
        if (state.getBlock() instanceof WallSkullBlock) {
            return wallSkullBlocks[skullType].defaultBlockState().setValue(WallSkullBlock.FACING, state.getValue(WallSkullBlock.FACING));
        } else {
            return skullBlocks[skullType].defaultBlockState().setValue(SkullBlock.ROTATION, multiconnect_rotation & 15);
        }
    }
}
