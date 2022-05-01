package net.earthcomputer.multiconnect.protocols.v1_10;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.generic.DataTrackerManager;
import net.earthcomputer.multiconnect.protocols.v1_10.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_12_2.BlockEntities_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.RecipeSerializer;

import java.util.List;
import java.util.Set;

public class Protocol_1_10 extends Protocol_1_11 {
    public static final TrackedData<Byte> OLD_GUARDIAN_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<Integer> OLD_ZOMBIE_TYPE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Boolean> OLD_ZOMBIE_CONVERTING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Byte> OLD_HORSE_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    public static final TrackedData<Integer> OLD_HORSE_TYPE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_HORSE_VARIANT = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_HORSE_ARMOR = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_SKELETON_TYPE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    private static final BiMap<EntityType<?>, String> ENTITY_IDS = ImmutableBiMap.<EntityType<?>, String>builder()
            .put(EntityType.AREA_EFFECT_CLOUD, "AreaEffectCloud")
            .put(EntityType.ARMOR_STAND, "ArmorStand")
            .put(EntityType.ARROW, "Arrow")
            .put(EntityType.BAT, "Bat")
            .put(EntityType.BLAZE, "Blaze")
            .put(EntityType.BOAT, "Boat")
            .put(EntityType.CAVE_SPIDER, "CaveSpider")
            .put(EntityType.CHICKEN, "Chicken")
            .put(EntityType.COW, "Cow")
            .put(EntityType.CREEPER, "Creeper")
            .put(EntityType.DONKEY, "Donkey")
            .put(EntityType.DRAGON_FIREBALL, "DragonFireball")
            .put(EntityType.ELDER_GUARDIAN, "ElderGuardian")
            .put(EntityType.END_CRYSTAL, "EnderCrystal")
            .put(EntityType.ENDER_DRAGON, "EnderDragon")
            .put(EntityType.ENDERMAN, "Enderman")
            .put(EntityType.ENDERMITE, "Endermite")
            .put(EntityType.EYE_OF_ENDER, "EyeOfEnderSignal")
            .put(EntityType.FALLING_BLOCK, "FallingSand")
            .put(EntityType.FIREBALL, "Fireball")
            .put(EntityType.FIREWORK_ROCKET, "FireworksRocketEntity")
            .put(EntityType.GHAST, "Ghast")
            .put(EntityType.GIANT, "Giant")
            .put(EntityType.GUARDIAN, "Guardian")
            .put(EntityType.HORSE, "EntityHorse")
            .put(EntityType.ITEM, "Item")
            .put(EntityType.ITEM_FRAME, "ItemFrame")
            .put(EntityType.MAGMA_CUBE, "LavaSlime")
            .put(EntityType.LEASH_KNOT, "LeashKnot")
            .put(EntityType.CHEST_MINECART, "MinecartChest")
            .put(EntityType.COMMAND_BLOCK_MINECART, "MinecartCommandBlock")
            .put(EntityType.FURNACE_MINECART, "MinecartFurnace")
            .put(EntityType.HOPPER_MINECART, "MinecartHopper")
            .put(EntityType.MINECART, "MinecartRideable")
            .put(EntityType.SPAWNER_MINECART, "MinecartSpawner")
            .put(EntityType.TNT_MINECART, "MinecartTNT")
            .put(EntityType.MOOSHROOM, "MushroomCow")
            .put(EntityType.OCELOT, "Ozelot")
            .put(EntityType.PAINTING, "Painting")
            .put(EntityType.PIG, "Pig")
            .put(EntityType.ZOMBIFIED_PIGLIN, "PigZombie")
            .put(EntityType.POLAR_BEAR, "PolarBear")
            .put(EntityType.TNT, "PrimedTnt")
            .put(EntityType.RABBIT, "Rabbit")
            .put(EntityType.SHEEP, "Sheep")
            .put(EntityType.SHULKER, "Shulker")
            .put(EntityType.SHULKER_BULLET, "ShulkerBullet")
            .put(EntityType.SILVERFISH, "Silverfish")
            .put(EntityType.SKELETON, "Skeleton")
            .put(EntityType.SLIME, "Slime")
            .put(EntityType.SMALL_FIREBALL, "SmallFireball")
            .put(EntityType.SNOW_GOLEM, "SnowMan")
            .put(EntityType.SNOWBALL, "Snowball")
            .put(EntityType.SPECTRAL_ARROW, "SpectralArrow")
            .put(EntityType.SPIDER, "Spider")
            .put(EntityType.SQUID, "Squid")
            .put(EntityType.EGG, "ThrownEgg")
            .put(EntityType.ENDER_PEARL, "ThrownEnderpearl")
            .put(EntityType.EXPERIENCE_BOTTLE, "ThrownExpBottle")
            .put(EntityType.POTION, "ThrownPotion")
            .put(EntityType.VILLAGER, "Villager")
            .put(EntityType.IRON_GOLEM, "VillagerGolem")
            .put(EntityType.WITCH, "Witch")
            .put(EntityType.WITHER, "WitherBoss")
            .put(EntityType.WITHER_SKULL, "WitherSkull")
            .put(EntityType.WOLF, "Wolf")
            .put(EntityType.EXPERIENCE_ORB, "XPOrb")
            .put(EntityType.ZOMBIE, "Zombie")
            .put(EntityType.PLAYER, "Player")
            .put(EntityType.LIGHTNING_BOLT, "LightningBolt")
            .build();

    private static final BiMap<BlockEntityType<?>, String> BLOCK_ENTITY_IDS = ImmutableBiMap.<BlockEntityType<?>, String>builder()
            .put(BlockEntityType.END_PORTAL, "Airportal")
            .put(BlockEntityType.BANNER, "Banner")
            .put(BlockEntityType.BEACON, "Beacon")
            .put(BlockEntityType.BREWING_STAND, "Cauldron")
            .put(BlockEntityType.CHEST, "Chest")
            .put(BlockEntityType.COMPARATOR, "Comparator")
            .put(BlockEntityType.COMMAND_BLOCK, "Control")
            .put(BlockEntityType.DAYLIGHT_DETECTOR, "DLDetector")
            .put(BlockEntityType.DROPPER, "Dropper")
            .put(BlockEntityType.ENCHANTING_TABLE, "EnchantTable")
            .put(BlockEntityType.ENDER_CHEST, "EnderChest")
            .put(BlockEntityType.END_GATEWAY, "EndGateway")
            .put(BlockEntities_1_12_2.FLOWER_POT, "FlowerPot")
            .put(BlockEntityType.FURNACE, "Furnace")
            .put(BlockEntityType.HOPPER, "Hopper")
            .put(BlockEntityType.MOB_SPAWNER, "MobSpawner")
            .put(BlockEntities_1_12_2.NOTE_BLOCK, "Music")
            .put(BlockEntityType.PISTON, "Piston")
            .put(BlockEntityType.JUKEBOX, "RecordPlayer")
            .put(BlockEntityType.SIGN, "Sign")
            .put(BlockEntityType.SKULL, "Skull")
            .put(BlockEntityType.STRUCTURE_BLOCK, "Structure")
            .put(BlockEntityType.DISPENSER, "Trap")
            .put(BlockEntityType.TRAPPED_CHEST, "TrappedChest") // Not actually in 1.10 but useful to have an ID
            .build();

    public static String getBlockEntityId(BlockEntityType<?> blockEntityType) {
        if (blockEntityType == BlockEntityType.TRAPPED_CHEST) {
            return "Chest";
        }
        return BLOCK_ENTITY_IDS.get(blockEntityType);
    }

    public static BlockEntityType<?> getBlockEntityById(String id) {
        return BLOCK_ENTITY_IDS.inverse().get(id);
    }

    @Override
    public List<RecipeInfo<?>> getRecipes() {
        List<RecipeInfo<?>> recipes = super.getRecipes();
        recipes.removeIf(recipe -> {
            Item item = recipe.getOutput().getItem();
            if (item instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                    return true;
                }
                return false;
            } else if (item == Items.OBSERVER || item == Items.IRON_NUGGET) {
                return true;
            } else if (item == Items.GOLD_NUGGET) {
                return recipe.getRecipeType() == RecipeSerializer.SMELTING;
            } else {
                return false;
            }
        });
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("locate").remove();
        BrigadierRemover.of(dispatcher).get("title").get("player").get("actionbar").remove();
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == AbstractSkeletonEntity.class && data == Protocol_1_13_2.OLD_SKELETON_ATTACKING) {
            DataTrackerManager.registerOldTrackedData(AbstractSkeletonEntity.class, OLD_SKELETON_TYPE, 0, (entity, val) -> {
                EntityType<?> newType = switch (val) {
                    case 1 -> EntityType.WITHER_SKELETON;
                    case 2 -> EntityType.STRAY;
                    default -> EntityType.SKELETON;
                };
                if (newType != entity.getType()) {
                    changeEntityType(entity, newType);
                }
            });
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == GuardianEntity.class && data == GuardianEntityAccessor.getSpikesRetracted()) {
            DataTrackerManager.registerOldTrackedData(GuardianEntity.class, OLD_GUARDIAN_FLAGS, (byte)0, (entity, val) -> {
                entity.getDataTracker().set(GuardianEntityAccessor.getSpikesRetracted(), (val & 2) != 0);
                boolean isElder = entity.getType() == EntityType.ELDER_GUARDIAN;
                boolean shouldBeElder = (val & 4) != 0;
                if (isElder != shouldBeElder) {
                    changeEntityType(entity, shouldBeElder ? EntityType.ELDER_GUARDIAN : EntityType.GUARDIAN);
                }
            });
            return false;
        }
        if (clazz == ShulkerEntity.class && data == ShulkerEntityAccessor.getColor()) {
            return false;
        }
        if (clazz == ZombieVillagerEntity.class && data == ZombieVillagerEntityAccessor.getConverting()) {
            return false;
        }
        if (clazz == ZombieEntity.class && data == ZombieEntityAccessor.getZombieType()) {
            DataTrackerManager.registerOldTrackedData(ZombieEntity.class, OLD_ZOMBIE_TYPE, 0, (entity, val) -> {
                EntityType<?> newType = switch (val) {
                    case 1, 2, 3, 4, 5 -> EntityType.ZOMBIE_VILLAGER;
                    case 6 -> EntityType.HUSK;
                    default -> EntityType.ZOMBIE;
                };
                if (newType != entity.getType()) {
                    entity = (ZombieEntity) changeEntityType(entity, newType);
                }
                if (newType == EntityType.ZOMBIE_VILLAGER) {
                    entity.getDataTracker().set(Protocol_1_13_2.OLD_ZOMBIE_VILLAGER_PROFESSION, val - 1);
                }
            });
            DataTrackerManager.registerOldTrackedData(ZombieEntity.class, OLD_ZOMBIE_CONVERTING, false, (entity, val) -> {
                if (entity instanceof ZombieVillagerEntity) {
                    entity.getDataTracker().set(ZombieVillagerEntityAccessor.getConverting(), val);
                }
            });
            return false;
        }
        if (clazz == HorseBaseEntity.class && data == HorseBaseEntityAccessor.getHorseFlags()) {
            DataTrackerManager.registerOldTrackedData(HorseBaseEntity.class, OLD_HORSE_FLAGS, (byte)0, (entity, val) -> {
                // keep the bottom 3 flags, skip the 4th and shift the higher ones down
                entity.getDataTracker().set(HorseBaseEntityAccessor.getHorseFlags(), (byte) ((val & 7) | ((val & ~15) >>> 1)));
                if (entity instanceof AbstractDonkeyEntity donkey) {
                    donkey.setHasChest((val & 8) != 0);
                }
            });
            DataTrackerManager.registerOldTrackedData(HorseBaseEntity.class, OLD_HORSE_TYPE, 0, (entity, val) -> {
                EntityType<?> newType = switch (val) {
                    case 1 -> EntityType.DONKEY;
                    case 2 -> EntityType.MULE;
                    case 3 -> EntityType.ZOMBIE_HORSE;
                    case 4 -> EntityType.SKELETON_HORSE;
                    default -> EntityType.HORSE;
                };
                if (newType != entity.getType()) {
                    changeEntityType(entity, newType);
                }
            });
            DataTrackerManager.registerOldTrackedData(HorseBaseEntity.class, OLD_HORSE_VARIANT, 0, (entity, val) -> {
                if (entity instanceof HorseEntity) {
                    entity.getDataTracker().set(HorseEntityAccessor.getVariant(), val);
                }
            });
            return false;
        }
        if (clazz == HorseEntity.class && (data == HorseEntityAccessor.getVariant() || data == Protocol_1_13_2.OLD_HORSE_ARMOR)) {
            return false;
        }
        if (clazz == AbstractDonkeyEntity.class && data == AbstractDonkeyEntityAccessor.getChest()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void postEntityDataRegister(Class<? extends Entity> clazz) {
        super.postEntityDataRegister(clazz);
        if (clazz == HorseBaseEntity.class) {
            DataTrackerManager.registerOldTrackedData(HorseBaseEntity.class, OLD_HORSE_ARMOR, 0, (entity, val) -> {
                if (entity instanceof HorseEntity) {
                    entity.getDataTracker().set(Protocol_1_13_2.OLD_HORSE_ARMOR, val);
                }
            });
        }
    }

    private static Entity changeEntityType(Entity entity, EntityType<?> newType) {
        ClientWorld world = (ClientWorld) entity.world;
        Entity destEntity = newType.create(world);
        if (destEntity == null) {
            return entity;
        }

        // copy the entity
        destEntity.readNbt(entity.writeNbt(new NbtCompound()));
        destEntity.updateTrackedPosition(entity.getTrackedPosition());

        // replace entity in world and exchange entity id
        int entityId = entity.getId();
        world.removeEntity(entityId, Entity.RemovalReason.DISCARDED);
        destEntity.setId(entityId);
        world.addEntity(entityId, destEntity);

        // exchange data tracker (this may be part of a series of data tracker updates, need the same data tracker instance)
        DataTrackerManager.transferDataTracker(entity, destEntity);
        return destEntity;
    }

    public static String getEntityId(EntityType<?> entityType) {
        return ENTITY_IDS.get(entityType);
    }

    public static EntityType<?> getEntityById(String id) {
        return ENTITY_IDS.inverse().get(id);
    }
}
