package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BedBlockEntity.class)
public abstract class BedBlockEntityMixin extends BlockEntity {

    @Shadow public abstract void setColor(DyeColor color);

    public BedBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (tag.contains("color"))
                multiconnect_setBedColor(tag.getInt("color"));
        }
    }

    @Unique
    public void multiconnect_setBedColor(int color) {
        setColor(DyeColor.byId(color));

        BlockState state = getBlockState();
        if (!getType().isValid(state))
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
        assert level != null;
        level.setBlock(worldPosition, beds[color].defaultBlockState().setValue(BedBlock.FACING, state.getValue(BedBlock.FACING)).setValue(BedBlock.PART, state.getValue(BedBlock.PART)), 18);
    }

}
