package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;

public class OldLitBlock extends AirBlock {
    protected OldLitBlock(Block block, boolean lit) {
        super(AbstractBlock.Settings.copy(block));
        setDefaultState(getStateManager().getDefaultState().with(Properties.LIT, lit));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.LIT);
    }
}
