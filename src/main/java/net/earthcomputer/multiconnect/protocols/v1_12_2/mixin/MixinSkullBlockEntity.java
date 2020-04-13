package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkullTileEntity.class)
public abstract class MixinSkullBlockEntity extends TileEntity {

    public MixinSkullBlockEntity(TileEntityType<?> type) {
        super(type);
    }

    @Inject(method = "read", at = @At("RETURN"))
    private void onFromTag(CompoundNBT tag, CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            setSkullType(tag.getByte("SkullType"));
            setRotation(tag.getByte("Rot"));
        }
    }

    @Unique
    public void setSkullType(int skullType) {
        BlockState state = getBlockState();
        if (!getType().isValidBlock(state.getBlock()))
            return;

        if (skullType < 0 || skullType > 5)
            skullType = 0;

        final Block[] skullBlocks = {Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.ZOMBIE_HEAD, Blocks.PLAYER_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD};
        final Block[] wallSkullBlocks = {Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.ZOMBIE_WALL_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_WALL_HEAD};
        assert world != null;
        if (state.getBlock() instanceof WallSkullBlock) {
            world.setBlockState(pos, wallSkullBlocks[skullType].getDefaultState().with(WallSkullBlock.FACING, state.get(WallSkullBlock.FACING)), 18);
        } else {
            world.setBlockState(pos, skullBlocks[skullType].getDefaultState().with(SkullBlock.ROTATION, state.get(SkullBlock.ROTATION)), 18);
        }
    }

    @Unique
    public void setRotation(int rot) {
        BlockState state = getBlockState();
        if (!getType().isValidBlock(state.getBlock()) || !(state.getBlock() instanceof SkullBlock))
            return;

        assert world != null;
        world.setBlockState(pos, state.with(SkullBlock.ROTATION, rot & 15));
    }

}
