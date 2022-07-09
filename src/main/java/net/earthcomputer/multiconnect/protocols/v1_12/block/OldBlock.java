package net.earthcomputer.multiconnect.protocols.v1_12.block;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

class OldBlock extends AirBlock {
    protected OldBlock(Block block) {
        super(BlockBehaviour.Properties.copy(block));
    }
}
