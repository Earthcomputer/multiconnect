package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

public class Old2VariantBlock extends AirBlock {
    public static final Property<Integer> VARIANT = IntegerProperty.create("variant", 1, 2);

    protected Old2VariantBlock(Block block) {
        super(BlockBehaviour.Properties.copy(block));
        registerDefaultState(getStateDefinition().any().setValue(VARIANT, 1));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
