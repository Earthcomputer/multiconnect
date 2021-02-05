package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

public class BlockConnectionsWorldView implements IBlockConnectionsBlockView {
    private final WorldAccess world;

    public BlockConnectionsWorldView(WorldAccess world) {
        this.world = world;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return world.getBlockState(pos);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        world.setBlockState(pos, state, 18);
    }

    @Override
    public int getMinY() {
        return world.getDimension().getMinimumY();
    }

    @Override
    public int getMaxY() {
        return getMinY() + world.getDimension().getHeight() - 1;
    }
}
