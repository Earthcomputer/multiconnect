package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class OldNodropSkullBlock extends AirBlock {
    protected OldNodropSkullBlock(Block block) {
        super(AbstractBlock.Settings.copy(block));
        setDefaultState(getStateManager().getDefaultState().with(Properties.FACING, Direction.DOWN));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.FACING);
    }
}
