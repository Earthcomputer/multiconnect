package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;

public class OldFluidBlock extends FluidBlock {
    protected OldFluidBlock(FlowableFluid fluid, Block block) {
        super(fluid, AbstractBlock.Settings.copy(block));
    }
}
