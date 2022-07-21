package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;

public class OldLiquidBlock extends LiquidBlock {
    protected OldLiquidBlock(FlowingFluid fluid, Block block) {
        super(fluid, BlockBehaviour.Properties.copy(block));
    }
}
