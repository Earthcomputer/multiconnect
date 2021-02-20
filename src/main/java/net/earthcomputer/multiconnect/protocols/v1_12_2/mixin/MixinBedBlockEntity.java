package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BedBlockEntity.class)
public abstract class MixinBedBlockEntity extends BlockEntity {

    @Shadow public abstract void setColor(DyeColor color);

    public MixinBedBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        super.readNbt(tag);
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (tag.contains("color"))
                setBedColor(tag.getInt("color"));
        }
    }

    @Unique
    public void setBedColor(int color) {
        setColor(DyeColor.byId(color));

        BlockState state = getCachedState();
        if (!getType().supports(state))
            return;

        if (color < 0 || color > 15)
            color = 0;

        final Block[] beds = new Block[] {
                Blocks.WHITE_BED,
                Blocks.ORANGE_BED,
                Blocks.MAGENTA_BED,
                Blocks.LIGHT_BLUE_BED,
                Blocks.YELLOW_BED,
                Blocks.LIME_BED,
                Blocks.PINK_BED,
                Blocks.GRAY_BED,
                Blocks.LIGHT_GRAY_BED,
                Blocks.CYAN_BED,
                Blocks.PURPLE_BED,
                Blocks.BLUE_BED,
                Blocks.BROWN_BED,
                Blocks.GREEN_BED,
                Blocks.RED_BED,
                Blocks.BLACK_BED};
        assert world != null;
        world.setBlockState(pos, beds[color].getDefaultState().with(BedBlock.FACING, state.get(BedBlock.FACING)).with(BedBlock.PART, state.get(BedBlock.PART)), 18);
    }

}
