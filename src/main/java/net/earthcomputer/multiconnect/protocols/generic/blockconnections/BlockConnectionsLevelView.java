package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class BlockConnectionsLevelView implements IBlockConnectionsBlockView {
    private final LevelAccessor level;

    public BlockConnectionsLevelView(LevelAccessor level) {
        this.level = level;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return level.getBlockState(pos);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 18);
    }

    @Override
    public int getMinY() {
        return level.dimensionType().minY();
    }

    @Override
    public int getMaxY() {
        return getMinY() + level.dimensionType().height() - 1;
    }
}
