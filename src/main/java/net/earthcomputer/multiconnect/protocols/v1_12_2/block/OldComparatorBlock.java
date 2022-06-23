package net.earthcomputer.multiconnect.protocols.v1_12_2.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ComparatorBlock;

public class OldComparatorBlock extends ComparatorBlock {
    public OldComparatorBlock(Block block) {
        super(AbstractBlock.Settings.copy(block));
    }
}
