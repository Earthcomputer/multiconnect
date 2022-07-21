package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class OldFlowerPotBlock extends AirBlock {
    public static final Property<Integer> LEGACY_DATA = IntegerProperty.create("legacy_data", 0, 15);

    protected OldFlowerPotBlock(Block block) {
        super(BlockBehaviour.Properties.copy(block));
        registerDefaultState(getStateDefinition().any().setValue(LEGACY_DATA, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEGACY_DATA);
    }
}
