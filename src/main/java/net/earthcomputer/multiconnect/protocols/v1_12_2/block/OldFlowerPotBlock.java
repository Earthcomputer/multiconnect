package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;

public class OldFlowerPotBlock extends AirBlock {
    public static final Property<Integer> LEGACY_DATA = IntProperty.of("legacy_data", 0, 15);

    protected OldFlowerPotBlock(Block block) {
        super(AbstractBlock.Settings.copy(block));
        setDefaultState(getStateManager().getDefaultState().with(LEGACY_DATA, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LEGACY_DATA);
    }
}
