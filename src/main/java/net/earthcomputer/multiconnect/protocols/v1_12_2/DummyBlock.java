package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;

class DummyBlock extends AirBlock {

    final BlockState original;

    DummyBlock(Block original) {
        this(original.getDefaultState());
    }

    DummyBlock(BlockState original) {
        super(Block.Properties.create(Material.AIR));
        this.original = original;
    }
}
