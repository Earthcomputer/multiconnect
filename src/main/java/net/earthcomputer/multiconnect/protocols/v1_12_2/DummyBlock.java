package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;

class DummyBlock extends AirBlock {
    DummyBlock() {
        super(Block.Settings.of(Material.AIR));
    }
}
