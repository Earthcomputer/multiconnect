package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface IBlockConnectionsBlockView {
    BlockState getBlockState(BlockPos pos);
    void setBlockState(BlockPos pos, BlockState state);
}
