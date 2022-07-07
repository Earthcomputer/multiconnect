package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;

public class Old3VariantBlock extends AirBlock {
    public static final Property<Integer> VARIANT = IntProperty.of("variant", 1, 3);

    protected Old3VariantBlock(Block block) {
        super(Settings.copy(block));
        setDefaultState(getStateManager().getDefaultState().with(VARIANT, 1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
