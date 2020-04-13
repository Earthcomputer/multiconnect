package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.FlowerPotBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class FlowerPotBlockEntity extends TileEntity {

    public FlowerPotBlockEntity() {
        super(BlockEntities_1_12_2.FLOWER_POT);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        Item flowerPotItem;
        if (tag.contains("Item", 8))
            flowerPotItem = Registry.ITEM.getOrDefault(new ResourceLocation(tag.getString("Item")));
        else
            flowerPotItem = Registry.ITEM.getByValue(tag.getInt("Item"));
        int meta = tag.getInt("Data");
        setFlowerPotItemStack(Items_1_12_2.oldItemStackToNew(new ItemStack(flowerPotItem), meta));
    }

    public void setFlowerPotItemStack(ItemStack stack) {
        Block block;
        if (!(stack.getItem() instanceof BlockItem)) {
            block = Blocks.FLOWER_POT;
        } else {
            block = ((BlockItem) stack.getItem()).getBlock();
            block = FlowerPotBlockAccessor.getContentToPotted().getOrDefault(block, Blocks.FLOWER_POT);
        }
        assert world != null;
        world.setBlockState(pos, block.getDefaultState());
    }

}
