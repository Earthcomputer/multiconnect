package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;

public class Entities_1_12_2 {

    private static void register(ISimpleRegistry<EntityType<?>> registry, EntityType<?> entity, int id, String name, String oldName) {
        RegistryKey<EntityType<?>> key = RegistryKey.of(registry.getRegistryKey(), new Identifier(name));
        registry.register(entity, id, key, false);
    }

    public static void registerEntities(ISimpleRegistry<EntityType<?>> registry) {
        registry.clear(false);

        register(registry, EntityType.ITEM, 1, "item", "Item");
        register(registry, EntityType.EXPERIENCE_ORB, 2, "xp_orb", "XPOrb");
        register(registry, EntityType.AREA_EFFECT_CLOUD, 3, "area_effect_cloud", "AreaEffectCloud");
        register(registry, EntityType.ELDER_GUARDIAN, 4, "elder_guardian", "ElderGuardian");
        register(registry, EntityType.WITHER_SKELETON, 5, "wither_skeleton", "WitherSkeleton");
        register(registry, EntityType.STRAY, 6, "stray", "Stray");
        register(registry, EntityType.EGG, 7, "egg", "ThrownEgg");
        register(registry, EntityType.LEASH_KNOT, 8, "leash_knot", "LeashKnot");
        register(registry, EntityType.PAINTING, 9, "painting", "Painting");
        register(registry, EntityType.ARROW, 10, "arrow", "Arrow");
        register(registry, EntityType.SNOWBALL, 11, "snowball", "Snowball");
        register(registry, EntityType.FIREBALL, 12, "fireball", "Fireball");
        register(registry, EntityType.SMALL_FIREBALL, 13, "small_fireball", "SmallFireball");
        register(registry, EntityType.ENDER_PEARL, 14, "ender_pearl", "ThrownEnderpearl");
        register(registry, EntityType.EYE_OF_ENDER, 15, "eye_of_ender_signal", "EyeOfEnderSignal");
        register(registry, EntityType.POTION, 16, "potion", "ThrownPotion");
        register(registry, EntityType.EXPERIENCE_BOTTLE, 17, "xp_bottle", "ThrownExpBottle");
        register(registry, EntityType.ITEM_FRAME, 18, "item_frame", "ItemFrame");
        register(registry, EntityType.WITHER_SKULL, 19, "wither_skull", "WitherSkull");
        register(registry, EntityType.TNT, 20, "tnt", "PrimedTnt");
        register(registry, EntityType.FALLING_BLOCK, 21, "falling_block", "FallingSand");
        register(registry, EntityType.FIREWORK_ROCKET, 22, "fireworks_rocket", "FireworksRocketEntity");
        register(registry, EntityType.HUSK, 23, "husk", "Husk");
        register(registry, EntityType.SPECTRAL_ARROW, 24, "spectral_arrow", "SpectralArrow");
        register(registry, EntityType.SHULKER_BULLET, 25, "shulker_bullet", "ShulkerBullet");
        register(registry, EntityType.DRAGON_FIREBALL, 26, "dragon_fireball", "DragonFireball");
        register(registry, EntityType.ZOMBIE_VILLAGER, 27, "zombie_villager", "ZombieVillager");
        register(registry, EntityType.SKELETON_HORSE, 28, "skeleton_horse", "SkeletonHorse");
        register(registry, EntityType.ZOMBIE_HORSE, 29, "zombie_horse", "ZombieHorse");
        register(registry, EntityType.ARMOR_STAND, 30, "armor_stand", "ArmorStand");
        register(registry, EntityType.DONKEY, 31, "donkey", "Donkey");
        register(registry, EntityType.MULE, 32, "mule", "Mule");
        register(registry, EntityType.EVOKER_FANGS, 33, "evocation_fangs", "EvocationFangs");
        register(registry, EntityType.EVOKER, 34, "evocation_illager", "EvocationIllager");
        register(registry, EntityType.VEX, 35, "vex", "Vex");
        register(registry, EntityType.VINDICATOR, 36, "vindication_illager", "VindicationIllager");
        register(registry, EntityType.ILLUSIONER, 37, "illusion_illager", "IllusionIllager");
        register(registry, EntityType.COMMAND_BLOCK_MINECART, 40, "commandblock_minecart", "MinecartCommandBlock");
        register(registry, EntityType.BOAT, 41, "boat", "Boat");
        register(registry, EntityType.MINECART, 42, "minecart", "MinecartRideable");
        register(registry, EntityType.CHEST_MINECART, 43, "chest_minecart", "MinecartChest");
        register(registry, EntityType.FURNACE_MINECART, 44, "furnace_minecart", "MinecartFurnace");
        register(registry, EntityType.TNT_MINECART, 45, "tnt_minecart", "MinecartTNT");
        register(registry, EntityType.HOPPER_MINECART, 46, "hopper_minecart", "MinecartHopper");
        register(registry, EntityType.SPAWNER_MINECART, 47, "spawner_minecart", "MinecartSpawner");
        register(registry, EntityType.CREEPER, 50, "creeper", "Creeper");
        register(registry, EntityType.SKELETON, 51, "skeleton", "Skeleton");
        register(registry, EntityType.SPIDER, 52, "spider", "Spider");
        register(registry, EntityType.GIANT, 53, "giant", "Giant");
        register(registry, EntityType.ZOMBIE, 54, "zombie", "Zombie");
        register(registry, EntityType.SLIME, 55, "slime", "Slime");
        register(registry, EntityType.GHAST, 56, "ghast", "Ghast");
        register(registry, EntityType.ZOMBIFIED_PIGLIN, 57, "zombie_pigman", "PigZombie");
        register(registry, EntityType.ENDERMAN, 58, "enderman", "Enderman");
        register(registry, EntityType.CAVE_SPIDER, 59, "cave_spider", "CaveSpider");
        register(registry, EntityType.SILVERFISH, 60, "silverfish", "Silverfish");
        register(registry, EntityType.BLAZE, 61, "blaze", "Blaze");
        register(registry, EntityType.MAGMA_CUBE, 62, "magma_cube", "LavaSlime");
        register(registry, EntityType.ENDER_DRAGON, 63, "ender_dragon", "EnderDragon");
        register(registry, EntityType.WITHER, 64, "wither", "WitherBoss");
        register(registry, EntityType.BAT, 65, "bat", "Bat");
        register(registry, EntityType.WITCH, 66, "witch", "Witch");
        register(registry, EntityType.ENDERMITE, 67, "endermite", "Endermite");
        register(registry, EntityType.GUARDIAN, 68, "guardian", "Guardian");
        register(registry, EntityType.SHULKER, 69, "shulker", "Shulker");
        register(registry, EntityType.PIG, 90, "pig", "Pig");
        register(registry, EntityType.SHEEP, 91, "sheep", "Sheep");
        register(registry, EntityType.COW, 92, "cow", "Cow");
        register(registry, EntityType.CHICKEN, 93, "chicken", "Chicken");
        register(registry, EntityType.SQUID, 94, "squid", "Squid");
        register(registry, EntityType.WOLF, 95, "wolf", "Wolf");
        register(registry, EntityType.MOOSHROOM, 96, "mooshroom", "MushroomCow");
        register(registry, EntityType.SNOW_GOLEM, 97, "snowman", "SnowMan");
        register(registry, EntityType.CAT, 98, "ocelot", "Ozelot");
        register(registry, EntityType.IRON_GOLEM, 99, "villager_golem", "VillagerGolem");
        register(registry, EntityType.HORSE, 100, "horse", "Horse");
        register(registry, EntityType.RABBIT, 101, "rabbit", "Rabbit");
        register(registry, EntityType.POLAR_BEAR, 102, "polar_bear", "PolarBear");
        register(registry, EntityType.LLAMA, 103, "llama", "Llama");
        register(registry, EntityType.LLAMA_SPIT, 104, "llama_spit", "LlamaSpit");
        register(registry, EntityType.PARROT, 105, "parrot", "Parrot");
        register(registry, EntityType.VILLAGER, 120, "villager", "Villager");
        register(registry, EntityType.END_CRYSTAL, 200, "ender_crystal", "EnderCrystal");
        // these entities were never registered in 1.12
        register(registry, EntityType.FISHING_BOBBER, 201, "fishing_bobber", "FishingBobber");
        register(registry, EntityType.LIGHTNING_BOLT, 202, "lightning_bolt", "LightningBolt");
        register(registry, EntityType.PLAYER, 203, "player", "Player");
    }

}
