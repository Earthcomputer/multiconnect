package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public class FlowerPotBlockEntity extends BlockEntity {

    public FlowerPotBlockEntity() {
        super(BlockEntities_1_12_2.FLOWER_POT);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        super.fromTag(tag);
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
            block = CONTENT_TO_POTTED.getOrDefault(block, Blocks.FLOWER_POT);
        }
        assert world != null;
        world.setBlockState(pos, block.getDefaultState());
    }

    private static final Map<Block, Block> CONTENT_TO_POTTED;
    static {
        try {
            Field field = Arrays.stream(FlowerPotBlock.class.getDeclaredFields())
                    .filter(f -> Modifier.isStatic(f.getModifiers()) && f.getType() == Map.class)
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            field.setAccessible(true);
            //noinspection unchecked
            CONTENT_TO_POTTED = (Map<Block, Block>) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

}
