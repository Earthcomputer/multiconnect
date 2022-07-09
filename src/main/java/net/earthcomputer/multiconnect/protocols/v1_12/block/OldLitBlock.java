package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class OldLitBlock extends AirBlock {
    protected OldLitBlock(Block block, boolean lit) {
        super(BlockBehaviour.Properties.copy(block));
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.LIT, lit));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT);
    }
}
