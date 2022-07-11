package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class OldNodropSkullBlock extends AirBlock {
    protected OldNodropSkullBlock(Block block) {
        super(BlockBehaviour.Properties.copy(block));
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.FACING, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }
}
