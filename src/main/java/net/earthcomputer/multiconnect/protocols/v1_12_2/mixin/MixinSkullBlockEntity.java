package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.ISkullBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullBlockEntity.class)
public abstract class MixinSkullBlockEntity extends BlockEntity implements ISkullBlockEntity {
    @Unique private int skullType;
    @Unique private int rotation;

    public MixinSkullBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "readNbt", at = @At("RETURN"))
    private void onReadNbt(NbtCompound tag, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            this.skullType = tag.getByte("SkullType");
            this.rotation = tag.getByte("Rot");
            if (world != null) {
                world.setBlockState(pos, multiconnect_getActualState(), Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
            }
        }
    }

    @Override
    public BlockState multiconnect_getActualState() {
        BlockState state = getCachedState();
        if (!getType().supports(state))
            return state;

        int skullType = this.skullType;
        if (skullType < 0 || skullType > 5)
            skullType = 0;

        final Block[] skullBlocks = {Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_HEAD, Blocks.PLAYER_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD};
        final Block[] wallSkullBlocks = {Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.ZOMBIE_WALL_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_WALL_HEAD};
        assert world != null;
        if (state.getBlock() instanceof WallSkullBlock) {
            return wallSkullBlocks[skullType].getDefaultState().with(WallSkullBlock.FACING, state.get(WallSkullBlock.FACING));
        } else {
            return skullBlocks[skullType].getDefaultState().with(SkullBlock.ROTATION, rotation & 15);
        }
    }
}
