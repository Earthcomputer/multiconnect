package net.earthcomputer.multiconnect.protocols.v1_12;

import net.earthcomputer.multiconnect.protocols.v1_12.mixin.FlowerPotBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FlowerPotBlockEntity extends BlockEntity {
    private BlockState flowerPotState;

    public FlowerPotBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities_1_12_2.FLOWER_POT, pos, state);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Item flowerPotItem;
        if (tag.contains("Item", 8))
            flowerPotItem = Registry.ITEM.get(new ResourceLocation(tag.getString("Item")));
        else
            flowerPotItem = Registry.ITEM.byId(tag.getInt("Item"));
        int meta = tag.getInt("Data");
        // TODO: rewrite 1.12.2
//        setFlowerPotItemStack(Items_1_12_2.oldItemStackToNew(new ItemStack(flowerPotItem), meta));
    }

    public void setFlowerPotItemStack(ItemStack stack) {
        Block block;
        if (!(stack.getItem() instanceof BlockItem)) {
            block = Blocks.FLOWER_POT;
        } else {
            block = ((BlockItem) stack.getItem()).getBlock();
            block = FlowerPotBlockAccessor.getPottedByContent().getOrDefault(block, Blocks.FLOWER_POT);
        }
        assert level != null;
        flowerPotState = block.defaultBlockState();
        level.setBlockAndUpdate(worldPosition, flowerPotState);
    }

    @Nullable
    public BlockState getFlowerPotState() {
        return flowerPotState;
    }

}
