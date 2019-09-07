package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class Protocol_1_13_2 extends Protocol_1_14 {

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        super.modifyRegistry(registry);

        if (registry == Registry.BLOCK) {
            modifyBlockRegistry((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            modifyItemRegistry((ISimpleRegistry<Item>) registry);
        } else if (registry == Registry.ENTITY_TYPE) {
            modifyEntityTypeRegistry((ISimpleRegistry<EntityType<?>>) registry);
        } else if (registry == Registry.BIOME) {
            modifyBiomeRegistry((ISimpleRegistry<Biome>) registry);
        } else if (registry == Registry.STATUS_EFFECT) {
            modifyStatusEffectRegistry((ISimpleRegistry<StatusEffect>) registry);
        } else if (registry == Registry.PARTICLE_TYPE) {
            modifyParticleTypeRegistry((ISimpleRegistry<ParticleType<? extends ParticleEffect>>) registry);
        } else if (registry == Registry.ENCHANTMENT) {
            modifyEnchantmentRegistry((ISimpleRegistry<Enchantment>) registry);
        } else if (registry == Registry.BLOCK_ENTITY) {
            modifyBlockEntityRegistry((ISimpleRegistry<BlockEntityType<?>>) registry);
        }
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.BAMBOO);
        registry.unregister(Blocks.BAMBOO_SAPLING);
        registry.unregister(Blocks.POTTED_BAMBOO);
        registry.unregister(Blocks.BARREL);
        registry.unregister(Blocks.BELL);
        registry.unregister(Blocks.BLAST_FURNACE);
        registry.unregister(Blocks.CAMPFIRE);
        registry.unregister(Blocks.CARTOGRAPHY_TABLE);
        registry.unregister(Blocks.COMPOSTER);
        registry.unregister(Blocks.FLETCHING_TABLE);
        registry.unregister(Blocks.CORNFLOWER);
        registry.unregister(Blocks.LILY_OF_THE_VALLEY);
        registry.unregister(Blocks.WITHER_ROSE);
        registry.unregister(Blocks.GRINDSTONE);
        registry.unregister(Blocks.JIGSAW);
        registry.unregister(Blocks.LANTERN);
        registry.unregister(Blocks.LECTERN);
        registry.unregister(Blocks.LOOM);
        registry.unregister(Blocks.SCAFFOLDING);
        registry.unregister(Blocks.SPRUCE_SIGN);
        registry.unregister(Blocks.SPRUCE_WALL_SIGN);
        registry.unregister(Blocks.BIRCH_SIGN);
        registry.unregister(Blocks.BIRCH_WALL_SIGN);
        registry.unregister(Blocks.JUNGLE_SIGN);
        registry.unregister(Blocks.JUNGLE_WALL_SIGN);
        registry.unregister(Blocks.ACACIA_SIGN);
        registry.unregister(Blocks.ACACIA_WALL_SIGN);
        registry.unregister(Blocks.DARK_OAK_SIGN);
        registry.unregister(Blocks.DARK_OAK_WALL_SIGN);
        registry.unregister(Blocks.STONE_SLAB);
        registry.unregister(Blocks.STONE_STAIRS);
        registry.unregister(Blocks.ANDESITE_SLAB);
        registry.unregister(Blocks.ANDESITE_STAIRS);
        registry.unregister(Blocks.POLISHED_ANDESITE_SLAB);
        registry.unregister(Blocks.POLISHED_ANDESITE_STAIRS);
        registry.unregister(Blocks.DIORITE_SLAB);
        registry.unregister(Blocks.DIORITE_STAIRS);
        registry.unregister(Blocks.POLISHED_DIORITE_SLAB);
        registry.unregister(Blocks.POLISHED_DIORITE_STAIRS);
        registry.unregister(Blocks.GRANITE_SLAB);
        registry.unregister(Blocks.GRANITE_STAIRS);
        registry.unregister(Blocks.POLISHED_GRANITE_SLAB);
        registry.unregister(Blocks.POLISHED_GRANITE_STAIRS);
        registry.unregister(Blocks.MOSSY_STONE_BRICK_SLAB);
        registry.unregister(Blocks.MOSSY_STONE_BRICK_STAIRS);
        registry.unregister(Blocks.MOSSY_COBBLESTONE_SLAB);
        registry.unregister(Blocks.MOSSY_COBBLESTONE_STAIRS);
        registry.unregister(Blocks.SMOOTH_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_SANDSTONE_STAIRS);
        registry.unregister(Blocks.CUT_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_RED_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        registry.unregister(Blocks.CUT_RED_SANDSTONE_SLAB);
        registry.unregister(Blocks.SMOOTH_QUARTZ_SLAB);
        registry.unregister(Blocks.SMOOTH_QUARTZ_STAIRS);
        registry.unregister(Blocks.RED_NETHER_BRICK_SLAB);
        registry.unregister(Blocks.RED_NETHER_BRICK_STAIRS);
        registry.unregister(Blocks.END_STONE_BRICK_SLAB);
        registry.unregister(Blocks.END_STONE_BRICK_STAIRS);
        registry.unregister(Blocks.SMITHING_TABLE);
        registry.unregister(Blocks.SMOKER);
        registry.unregister(Blocks.STONECUTTER);
        registry.unregister(Blocks.SWEET_BERRY_BUSH);
        registry.unregister(Blocks.BRICK_WALL);
        registry.unregister(Blocks.ANDESITE_WALL);
        registry.unregister(Blocks.DIORITE_WALL);
        registry.unregister(Blocks.GRANITE_WALL);
        registry.unregister(Blocks.PRISMARINE_WALL);
        registry.unregister(Blocks.STONE_BRICK_WALL);
        registry.unregister(Blocks.MOSSY_STONE_BRICK_WALL);
        registry.unregister(Blocks.SANDSTONE_WALL);
        registry.unregister(Blocks.RED_SANDSTONE_WALL);
        registry.unregister(Blocks.NETHER_BRICK_WALL);
        registry.unregister(Blocks.RED_NETHER_BRICK_WALL);
        registry.unregister(Blocks.END_STONE_BRICK_WALL);
    }

    private void modifyItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.CROSSBOW);
        registry.unregister(Items.BLUE_DYE);
        registry.unregister(Items.BROWN_DYE);
        registry.unregister(Items.BLACK_DYE);
        registry.unregister(Items.WHITE_DYE);
        registry.unregister(Items.LEATHER_HORSE_ARMOR);
        registry.unregister(Items.SUSPICIOUS_STEW);
        registry.unregister(Items.SWEET_BERRIES);
    }

    private void modifyEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.CAT);
        registry.unregister(EntityType.FOX);
        registry.unregister(EntityType.PANDA);
        registry.unregister(EntityType.PILLAGER);
        registry.unregister(EntityType.RAVAGER);
        registry.unregister(EntityType.TRADER_LLAMA);
        registry.unregister(EntityType.WANDERING_TRADER);
    }

    private void modifyBiomeRegistry(ISimpleRegistry<Biome> registry) {
        registry.unregister(Biomes.BAMBOO_JUNGLE);
        registry.unregister(Biomes.BAMBOO_JUNGLE_HILLS);
    }

    private void modifyStatusEffectRegistry(ISimpleRegistry<StatusEffect> registry) {
        registry.unregister(StatusEffects.BAD_OMEN);
        registry.unregister(StatusEffects.HERO_OF_THE_VILLAGE);
    }

    private void modifyParticleTypeRegistry(ISimpleRegistry<ParticleType<? extends ParticleEffect>> registry) {
        registry.unregister(ParticleTypes.CAMPFIRE_COSY_SMOKE);
        registry.unregister(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
        registry.unregister(ParticleTypes.SNEEZE);
    }

    private void modifyEnchantmentRegistry(ISimpleRegistry<Enchantment> registry) {
        registry.unregister(Enchantments.QUICK_CHARGE);
        registry.unregister(Enchantments.MULTISHOT);
        registry.unregister(Enchantments.PIERCING);
    }

    private void modifyBlockEntityRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.BARREL);
        registry.unregister(BlockEntityType.SMOKER);
        registry.unregister(BlockEntityType.BLAST_FURNACE);
        registry.unregister(BlockEntityType.LECTERN);
        registry.unregister(BlockEntityType.BELL);
        registry.unregister(BlockEntityType.JIGSAW);
        registry.unregister(BlockEntityType.CAMPFIRE);
    }
}
