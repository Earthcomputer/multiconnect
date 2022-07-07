package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;

class OldBlock extends AirBlock {
    protected OldBlock(Block block) {
        super(AbstractBlock.Settings.copy(block));
    }
}
