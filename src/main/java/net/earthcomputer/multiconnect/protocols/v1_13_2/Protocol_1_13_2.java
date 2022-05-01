package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_13_2.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.tag.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.Difficulty;

import java.util.*;

public class Protocol_1_13_2 extends Protocol_1_14 {
    public static final Identifier CUSTOM_PAYLOAD_TRADE_LIST = new Identifier("trader_list");
    public static final Identifier CUSTOM_PAYLOAD_OPEN_BOOK = new Identifier("open_book");

    public static final TrackedData<Integer> OLD_FIREWORK_SHOOTER = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_VILLAGER_PROFESSION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Byte> OLD_ILLAGER_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<Boolean> OLD_SKELETON_ATTACKING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> OLD_ZOMBIE_ATTACKING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Integer> OLD_ZOMBIE_VILLAGER_PROFESSION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_HORSE_ARMOR = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    private static final Key<byte[][]> BLOCK_LIGHT_KEY = Key.create("blockLight");
    private static final Key<byte[][]> SKY_LIGHT_KEY = Key.create("skyLight");
    public static final Key<Difficulty> DIFFICULTY_KEY = Key.create("difficulty");

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == ZombieEntity.class && data == ZombieEntityAccessor.getConvertingInWater()) {
            DataTrackerManager.registerOldTrackedData(ZombieEntity.class, OLD_ZOMBIE_ATTACKING, false, MobEntity::setAttacking);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getPose())
            return false;
        if (clazz == EyeOfEnderEntity.class && data == EnderEyeEntityAccessor.getItem())
            return false;
        if (clazz == FireworkRocketEntity.class) {
            TrackedData<OptionalInt> fireworkShooter = FireworkEntityAccessor.getShooter();
            if (data == fireworkShooter) {
                DataTrackerManager.registerOldTrackedData(FireworkRocketEntity.class, OLD_FIREWORK_SHOOTER, 0,
                        (entity, val) -> entity.getDataTracker().set(fireworkShooter, val <= 0 ? OptionalInt.empty() : OptionalInt.of(val)));
                return false;
            }
            if (data == FireworkEntityAccessor.getShotAtAngle())
                return false;
        }
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getSleepingPosition())
            return false;
        if (clazz == VillagerEntity.class) {
            TrackedData<VillagerData> villagerData = VillagerEntityAccessor.getVillagerData();
            if (data == villagerData) {
                DataTrackerManager.registerOldTrackedData(VillagerEntity.class, OLD_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getDataTracker().set(villagerData, entity.getVillagerData().withProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == ZombieVillagerEntity.class) {
            TrackedData<VillagerData> villagerData = ZombieVillagerEntityAccessor.getVillagerData();
            if (data == villagerData) {
                DataTrackerManager.registerOldTrackedData(ZombieVillagerEntity.class, OLD_ZOMBIE_VILLAGER_PROFESSION, 0,
                        (entity, val) -> entity.getDataTracker().set(villagerData, entity.getVillagerData().withProfession(getVillagerProfession(val))));
                return false;
            }
        }
        if (clazz == MooshroomEntity.class && data == MooshroomEntityAccessor.getType())
            return false;
        if (clazz == CatEntity.class) {
            if (data == CatEntityAccessor.getInSleepingPose()
                || data == CatEntityAccessor.getHeadDown()
                || data == CatEntityAccessor.getCollarColor())
                return false;
        }
        if (clazz == PersistentProjectileEntity.class && data == ProjectileEntityAccessor.getPierceLevel())
            return false;
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void postEntityDataRegister(Class<? extends Entity> clazz) {
        if (clazz == IllagerEntity.class)
            DataTrackerManager.registerOldTrackedData(IllagerEntity.class, OLD_ILLAGER_FLAGS, (byte)0,
                    (entity, val) -> entity.setAttacking((val & 1) != 0));
        if (clazz == AbstractSkeletonEntity.class)
            DataTrackerManager.registerOldTrackedData(AbstractSkeletonEntity.class, OLD_SKELETON_ATTACKING, false, MobEntity::setAttacking);
        if (clazz == HorseEntity.class)
            DataTrackerManager.registerOldTrackedData(HorseEntity.class, OLD_HORSE_ARMOR, 0, (entity, val) -> {
                switch (val) {
                    case 1 -> entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_HORSE_ARMOR));
                    case 2 -> entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_HORSE_ARMOR));
                    case 3 -> entity.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_HORSE_ARMOR));
                    default -> entity.equipStack(EquipmentSlot.CHEST, ItemStack.EMPTY);
                }
            });
        super.postEntityDataRegister(clazz);
    }

    private static VillagerProfession getVillagerProfession(int id) {
        return switch (id) {
            case 0 -> VillagerProfession.FARMER;
            case 1 -> VillagerProfession.LIBRARIAN;
            case 2 -> VillagerProfession.CLERIC;
            case 3 -> VillagerProfession.ARMORER;
            case 4 -> VillagerProfession.BUTCHER;
            default -> VillagerProfession.NITWIT;
        };
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.WOODEN_FENCES, Blocks.OAK_FENCE, Blocks.ACACIA_FENCE, Blocks.DARK_OAK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE);
        tags.add(BlockTags.SMALL_FLOWERS, Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY);
        tags.addTag(BlockTags.ENDERMAN_HOLDABLE, BlockTags.SMALL_FLOWERS);
        tags.add(BlockTags.WALLS, Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL, Blocks.BRICK_WALL, Blocks.PRISMARINE_WALL, Blocks.RED_SANDSTONE_WALL, Blocks.MOSSY_STONE_BRICK_WALL, Blocks.GRANITE_WALL, Blocks.STONE_BRICK_WALL, Blocks.NETHER_BRICK_WALL, Blocks.ANDESITE_WALL, Blocks.RED_NETHER_BRICK_WALL, Blocks.SANDSTONE_WALL, Blocks.END_STONE_BRICK_WALL, Blocks.DIORITE_WALL);
        tags.add(BlockTags.BAMBOO_PLANTABLE_ON);
        tags.add(BlockTags.STANDING_SIGNS, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN);
        tags.add(BlockTags.WALL_SIGNS, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN);
        tags.addTag(BlockTags.SIGNS, BlockTags.STANDING_SIGNS);
        tags.addTag(BlockTags.SIGNS, BlockTags.WALL_SIGNS);
        tags.add(BlockTags.BEDS, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED);
        tags.addTag(BlockTags.FENCES, BlockTags.WOODEN_FENCES);
        tags.add(BlockTags.FENCES, Blocks.NETHER_BRICK_FENCE);
        tags.add(BlockTags.DRAGON_IMMUNE, Blocks.BARRIER, Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.MOVING_PISTON, Blocks.OBSIDIAN, Blocks.END_STONE, Blocks.IRON_BARS);
        tags.add(BlockTags.WITHER_IMMUNE, Blocks.BARRIER, Blocks.BEDROCK, Blocks.END_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.END_GATEWAY, Blocks.COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.MOVING_PISTON);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.WOODEN_FENCES, BlockTags.WOODEN_FENCES);
        copyBlocks(tags, blockTags, ItemTags.WALLS, BlockTags.WALLS);
        copyBlocks(tags, blockTags, ItemTags.SMALL_FLOWERS, BlockTags.SMALL_FLOWERS);
        copyBlocks(tags, blockTags, ItemTags.BEDS, BlockTags.BEDS);
        copyBlocks(tags, blockTags, ItemTags.FENCES, BlockTags.FENCES);
        copyBlocks(tags, blockTags, ItemTags.SIGNS, BlockTags.STANDING_SIGNS);
        tags.add(ItemTags.MUSIC_DISCS,
                Items.MUSIC_DISC_13,
                Items.MUSIC_DISC_CAT,
                Items.MUSIC_DISC_BLOCKS,
                Items.MUSIC_DISC_CHIRP,
                Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL,
                Items.MUSIC_DISC_MELLOHI,
                Items.MUSIC_DISC_STAL,
                Items.MUSIC_DISC_STRAD,
                Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11,
                Items.MUSIC_DISC_WAIT);
        tags.add(ItemTags.COALS,
                Items.COAL,
                Items.CHARCOAL);
        tags.add(ItemTags.ARROWS,
                Items.ARROW,
                Items.TIPPED_ARROW,
                Items.SPECTRAL_ARROW);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.SKELETONS, EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
        tags.add(EntityTypeTags.RAIDERS, EntityType.EVOKER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
        super.addExtraEntityTags(tags);
    }

    @ThreadSafe
    public static void updateCameraPosition() {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (networkHandler != null && player != null) {
                ChunkSectionPos chunkPos = ChunkSectionPos.from(player);
                networkHandler.onChunkRenderDistanceCenter(new ChunkRenderDistanceCenterS2CPacket(chunkPos.getSectionX(), chunkPos.getSectionZ()));
            }
        }
    }
}
