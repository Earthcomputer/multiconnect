package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.impl.IBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

public class FlowerPotBlock_1_12_2 extends FlowerPotBlock implements BlockEntityProvider {

    public FlowerPotBlock_1_12_2(Block block) {
        super(block, ((IBlockSettings) Block.Settings.of(Material.PART)).callBreakInstantly().nonOpaque());
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new FlowerPotBlockEntity();
    }
}
