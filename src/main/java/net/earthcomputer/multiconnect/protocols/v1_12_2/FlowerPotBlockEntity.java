package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.FlowerPotBlockAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public class FlowerPotBlockEntity extends BlockEntity {

    public FlowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities_1_12_2.FLOWER_POT, pos, state);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        super.readNbt(tag);
        Item flowerPotItem;
        if (tag.contains("Item", 8))
            flowerPotItem = Registry.ITEM.get(new Identifier(tag.getString("Item")));
        else
            flowerPotItem = Registry.ITEM.get(tag.getInt("Item"));
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
