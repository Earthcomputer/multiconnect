package net.earthcomputer.multiconnect.protocols.v1_15_2;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_13_2.mixin.ProjectileEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.TameableEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.WolfEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;

import java.util.*;

public class Protocol_1_15_2 extends Protocol_1_16 {

    private static final TrackedData<Optional<UUID>> OLD_PROJECTILE_OWNER = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Byte> OLD_TAMEABLE_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.CRIMSON_STEMS);
        tags.add(BlockTags.WARPED_STEMS);
        tags.addTag(BlockTags.LOGS_THAT_BURN, BlockTags.LOGS);
        tags.add(BlockTags.STONE_PRESSURE_PLATES, Blocks.STONE_PRESSURE_PLATE);
        tags.addTag(BlockTags.PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES);
        tags.addTag(BlockTags.PRESSURE_PLATES, BlockTags.STONE_PRESSURE_PLATES);
        tags.add(BlockTags.WITHER_SUMMON_BASE_BLOCKS, Blocks.SOUL_SAND);
        tags.add(BlockTags.FIRE, Blocks.FIRE);
        tags.add(BlockTags.NYLIUM);
        tags.add(BlockTags.WART_BLOCKS);
        tags.add(BlockTags.BEACON_BASE_BLOCKS, Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK);
        tags.add(BlockTags.SOUL_SPEED_BLOCKS);
        tags.add(BlockTags.WALL_POST_OVERRIDE, Blocks.REDSTONE_TORCH, Blocks.TRIPWIRE);
        tags.addTag(BlockTags.WALL_POST_OVERRIDE, BlockTags.SIGNS);
        tags.addTag(BlockTags.WALL_POST_OVERRIDE, BlockTags.BANNERS);
        tags.addTag(BlockTags.WALL_POST_OVERRIDE, BlockTags.PRESSURE_PLATES);
        tags.add(BlockTags.CLIMBABLE, Blocks.LADDER, Blocks.VINE, Blocks.SCAFFOLDING);
        tags.add(BlockTags.PIGLIN_REPELLENTS);
        tags.add(BlockTags.HOGLIN_REPELLENTS);
        tags.add(BlockTags.GOLD_ORES, Blocks.GOLD_ORE);
        tags.add(BlockTags.SOUL_FIRE_BASE_BLOCKS);
        tags.add(BlockTags.NON_FLAMMABLE_WOOD);
        tags.add(BlockTags.STRIDER_WARM_BLOCKS);
        tags.add(BlockTags.CAMPFIRES, Blocks.CAMPFIRE);
        tags.add(BlockTags.GUARDED_BY_PIGLINS);
        tags.addTag(BlockTags.PREVENT_MOB_SPAWNING_INSIDE, BlockTags.RAILS);
        tags.add(BlockTags.FENCE_GATES, Blocks.ACACIA_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.OAK_FENCE, Blocks.SPRUCE_FENCE_GATE);
        tags.addTag(BlockTags.UNSTABLE_BOTTOM_CENTER, BlockTags.FENCE_GATES);
        tags.add(BlockTags.INFINIBURN_OVERWORLD, Blocks.NETHERRACK, Blocks.MAGMA_BLOCK);
        tags.addTag(BlockTags.INFINIBURN_NETHER, BlockTags.INFINIBURN_OVERWORLD);
        tags.addTag(BlockTags.INFINIBURN_END, BlockTags.INFINIBURN_OVERWORLD);
        tags.add(BlockTags.INFINIBURN_END, Blocks.BEDROCK);
        super.addExtraBlockTags(tags);

        tags.get(BlockTags.HOE_MINEABLE).clear();
        Set<Block> pickaxeMineableTag = tags.get(BlockTags.PICKAXE_MINEABLE);
        Arrays.asList(Blocks.PISTON, Blocks.STICKY_PISTON, Blocks.PISTON_HEAD).forEach(pickaxeMineableTag::remove);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.CRIMSON_STEMS, BlockTags.CRIMSON_STEMS);
        copyBlocks(tags, blockTags, ItemTags.WARPED_STEMS, BlockTags.WARPED_STEMS);
        copyBlocks(tags, blockTags, ItemTags.LOGS_THAT_BURN, BlockTags.LOGS_THAT_BURN);
        copyBlocks(tags, blockTags, ItemTags.GOLD_ORES, BlockTags.GOLD_ORES);
        copyBlocks(tags, blockTags, ItemTags.SOUL_FIRE_BASE_BLOCKS, BlockTags.SOUL_FIRE_BASE_BLOCKS);
        tags.addTag(ItemTags.CREEPER_DROP_MUSIC_DISCS, ItemTags.MUSIC_DISCS);
        tags.add(ItemTags.BEACON_PAYMENT_ITEMS, Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT);
        tags.add(ItemTags.PIGLIN_REPELLENTS);
        tags.add(ItemTags.PIGLIN_LOVED);
        tags.add(ItemTags.NON_FLAMMABLE_WOOD);
        tags.add(ItemTags.STONE_TOOL_MATERIALS, Items.COBBLESTONE);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.addTag(EntityTypeTags.IMPACT_PROJECTILES, EntityTypeTags.ARROWS);
        tags.add(EntityTypeTags.IMPACT_PROJECTILES,
                EntityType.SNOWBALL,
                EntityType.FIREBALL,
                EntityType.SMALL_FIREBALL,
                EntityType.EGG,
                EntityType.TRIDENT,
                EntityType.DRAGON_FIREBALL,
                EntityType.WITHER_SKULL);
        super.addExtraEntityTags(tags);
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == PersistentProjectileEntity.class && data == ProjectileEntityAccessor.getPierceLevel()) {
            DataTrackerManager.registerOldTrackedData(PersistentProjectileEntity.class, OLD_PROJECTILE_OWNER, Optional.empty(), (entity, val) -> {});
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == TameableEntity.class && data == TameableEntityAccessor.getTameableFlags()) {
            DataTrackerManager.registerOldTrackedData(TameableEntity.class, OLD_TAMEABLE_FLAGS, (byte)0, (entity, val) -> {
                byte newVal = val;
                if (entity instanceof WolfEntity wolf) {
                    wolf.setAngerTime((newVal & 2) != 0 ? 400 : 0);
                    newVal = (byte) (newVal & ~2);
                }
                entity.getDataTracker().set(TameableEntityAccessor.getTameableFlags(), newVal);
            });
            return false;
        }
        if (clazz == WolfEntity.class && data == WolfEntityAccessor.getAngerTime()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        hardness = super.getBlockHardness(state, hardness);
        if (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON || state.getBlock() == Blocks.PISTON_HEAD) {
            hardness = 0.5f;
        }
        return hardness;
    }

    @Override
    public float getBlockResistance(Block block, float resistance) {
        resistance = super.getBlockResistance(block, resistance);
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD) {
            resistance = 0.5f;
        }
        return resistance;
    }
}
