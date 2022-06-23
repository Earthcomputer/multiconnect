package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;

public class OldTripwireAlternativeBlock extends AirBlock {
    protected OldTripwireAlternativeBlock(Block block) {
        super(AbstractBlock.Settings.copy(block));
        setDefaultState(getStateManager().getDefaultState()
                .with(Properties.ATTACHED, false)
                .with(Properties.DISARMED, false)
                .with(Properties.POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.ATTACHED, Properties.DISARMED, Properties.POWERED);
    }
}
