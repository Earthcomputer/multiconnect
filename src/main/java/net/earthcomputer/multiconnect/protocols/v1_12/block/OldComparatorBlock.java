package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class OldComparatorBlock extends ComparatorBlock {
    public OldComparatorBlock(Block block) {
        super(BlockBehaviour.Properties.copy(block));
    }
}
