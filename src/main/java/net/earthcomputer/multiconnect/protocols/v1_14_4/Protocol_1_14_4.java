package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.EndermanEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.LivingEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.TridentEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.WolfEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.world.biome.Biome;

import java.util.List;

public class Protocol_1_14_4 extends Protocol_1_15 {

    public static final TrackedData<Float> OLD_WOLF_HEALTH = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.FLOAT);
    public static final Key<Biome[]> BIOME_DATA_KEY = Key.create("biomeData");
    public static final Key<List<DataTracker.Entry<?>>> DATA_TRACKER_ENTRIES_KEY = Key.create("dataTrackerEntries");

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == WolfEntity.class && data == WolfEntityAccessor.getBegging()) {
            DataTrackerManager.registerOldTrackedData(WolfEntity.class, OLD_WOLF_HEALTH, 20f, LivingEntity::setHealth);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getStingerCount())
            return false;
        if (clazz == TridentEntity.class && data == TridentEntityAccessor.getHasEnchantmentGlint())
            return false;
        if (clazz == EndermanEntity.class && data == EndermanEntityAccessor.getProvoked())
            return false;

        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.TALL_FLOWERS, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH);
        tags.addTag(BlockTags.FLOWERS, BlockTags.SMALL_FLOWERS);
        tags.addTag(BlockTags.FLOWERS, BlockTags.TALL_FLOWERS);
        tags.add(BlockTags.BEEHIVES);
        tags.add(BlockTags.CROPS, Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        tags.add(BlockTags.BEE_GROWABLES);
        tags.add(BlockTags.SHULKER_BOXES,
                Blocks.SHULKER_BOX,
                Blocks.BLACK_SHULKER_BOX,
                Blocks.BLUE_SHULKER_BOX,
                Blocks.BROWN_SHULKER_BOX,
                Blocks.CYAN_SHULKER_BOX,
                Blocks.GRAY_SHULKER_BOX,
                Blocks.GREEN_SHULKER_BOX,
                Blocks.LIGHT_BLUE_SHULKER_BOX,
                Blocks.LIGHT_GRAY_SHULKER_BOX,
                Blocks.LIME_SHULKER_BOX,
                Blocks.MAGENTA_SHULKER_BOX,
                Blocks.ORANGE_SHULKER_BOX,
                Blocks.PINK_SHULKER_BOX,
                Blocks.PURPLE_SHULKER_BOX,
                Blocks.RED_SHULKER_BOX,
                Blocks.WHITE_SHULKER_BOX,
                Blocks.YELLOW_SHULKER_BOX);
        tags.add(BlockTags.PORTALS, Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.TALL_FLOWERS, BlockTags.TALL_FLOWERS);
        copyBlocks(tags, blockTags, ItemTags.FLOWERS, BlockTags.FLOWERS);
        tags.add(ItemTags.LECTERN_BOOKS, Items.WRITTEN_BOOK, Items.WRITABLE_BOOK);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.BEEHIVE_INHABITORS);
        tags.add(EntityTypeTags.ARROWS, EntityType.ARROW, EntityType.SPECTRAL_ARROW);
        super.addExtraEntityTags(tags);
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        hardness = super.getBlockHardness(state, hardness);
        Block block = state.getBlock();
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            hardness = 0.8f;
        }
        return hardness;
    }

    @Override
    public float getBlockResistance(Block block, float resistance) {
        resistance = super.getBlockResistance(block, resistance);
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            resistance = 0.8f;
        }
        return resistance;
    }
}
