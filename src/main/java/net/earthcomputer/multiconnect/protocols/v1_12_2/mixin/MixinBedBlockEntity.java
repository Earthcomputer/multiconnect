package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BedTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BedTileEntity.class)
public abstract class MixinBedBlockEntity extends TileEntity {

    @Shadow public abstract void setColor(DyeColor color);

    public MixinBedBlockEntity(TileEntityType<?> type) {
        super(type);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            if (tag.contains("color"))
                setBedColor(tag.getInt("color"));
        }
    }

    @Unique
    public void setBedColor(int color) {
        setColor(DyeColor.byId(color));

        BlockState state = getBlockState();
        if (!getType().isValidBlock(state.getBlock()))
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
        world.setBlockState(pos, beds[color].getDefaultState().with(BedBlock.HORIZONTAL_FACING, state.get(BedBlock.HORIZONTAL_FACING)).with(BedBlock.PART, state.get(BedBlock.PART)), 18);
    }

}
