package net.earthcomputer.multiconnect.protocols.v1_10;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.protocols.generic.SynchedDataManager;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_12.BlockEntities_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12.command.BrigadierRemover;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Protocol_1_10 extends Protocol_1_11 {
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
                return recipe.getRecipeType() == RecipeSerializer.SMELTING_RECIPE;
            } else {
                return false;
            }
        });
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("locate").remove();
        BrigadierRemover.of(dispatcher).get("title").get("player").get("actionbar").remove();
    }

    private static Entity changeEntityType(Entity entity, EntityType<?> newType) {
        ClientLevel level = (ClientLevel) entity.level;
        Entity destEntity = newType.create(level);
        if (destEntity == null) {
            return entity;
        }

        // copy the entity
        destEntity.load(entity.saveWithoutId(new CompoundTag()));
        VecDeltaCodec positionCodec = entity.getPositionCodec();
        // yes it's stupid we have to do it this way
        Vec3 pos = positionCodec.delta(Vec3.ZERO).reverse();
        destEntity.syncPacketPositionCodec(pos.x, pos.y, pos.z);

        // replace entity in level and exchange entity id
        int entityId = entity.getId();
        level.removeEntity(entityId, Entity.RemovalReason.DISCARDED);
        destEntity.setId(entityId);
        level.putNonPlayerEntity(entityId, destEntity);

        // exchange entity data (this may be part of a series of entity data updates, need the same entity data instance)
        SynchedDataManager.transferEntityData(entity, destEntity);
        return destEntity;
    }

    public static String getEntityId(EntityType<?> entityType) {
        return ENTITY_IDS.get(entityType);
    }

    public static EntityType<?> getEntityById(String id) {
        return ENTITY_IDS.inverse().get(id);
    }
}
