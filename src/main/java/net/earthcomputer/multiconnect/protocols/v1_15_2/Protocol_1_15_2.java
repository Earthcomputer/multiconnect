package net.earthcomputer.multiconnect.protocols.v1_15_2;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Entities_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class Protocol_1_15_2 extends Protocol_1_16 {
    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        if (registry == Registry.BLOCK) {
            modifyBlockRegistry((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            modifyItemRegistry((ISimpleRegistry<Item>) registry);
        }  else if (registry == Registry.ENTITY_TYPE) {
            modifyEntityTypeRegistry((ISimpleRegistry<EntityType<?>>) registry);
        } else if (registry == Registry.BIOME) {
            modifyBiomeRegistry((ISimpleRegistry<Biome>) registry);
        } else if (registry == Registry.PARTICLE_TYPE) {
            modifyParticleTypeRegistry((ISimpleRegistry<ParticleType<? extends ParticleEffect>>) registry);
        } else if (registry == Registry.BLOCK_ENTITY_TYPE) {
            modifyBlockEntityRegistry((ISimpleRegistry<BlockEntityType<?>>) registry);
        } else if (registry == Registry.SOUND_EVENT) {
            modifySoundEventRegistry((ISimpleRegistry<SoundEvent>) registry);
        }
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.ANCIENT_DEBRIS);
        registry.unregister(Blocks.BASALT);
        registry.unregister(Blocks.CRIMSON_BUTTON);
        registry.unregister(Blocks.WARPED_BUTTON);
        registry.unregister(Blocks.CRIMSON_DOOR);
        registry.unregister(Blocks.WARPED_DOOR);
        registry.unregister(Blocks.CRIMSON_FENCE);
        registry.unregister(Blocks.WARPED_FENCE);
        registry.unregister(Blocks.CRIMSON_FENCE_GATE);
        registry.unregister(Blocks.WARPED_FENCE_GATE);
        registry.unregister(Blocks.CRIMSON_FUNGI);
        registry.unregister(Blocks.WARPED_FUNGI);
        registry.unregister(Blocks.NETHER_SPROUTS);
        registry.unregister(Blocks.CRIMSON_NYLIUM);
        registry.unregister(Blocks.WARPED_NYLIUM);
        registry.unregister(Blocks.CRIMSON_PLANKS);
        registry.unregister(Blocks.WARPED_PLANKS);
        registry.unregister(Blocks.CRIMSON_PRESSURE_PLATE);
        registry.unregister(Blocks.WARPED_PRESSURE_PLATE);
        registry.unregister(Blocks.CRIMSON_ROOTS);
        registry.unregister(Blocks.WARPED_ROOTS);
        registry.unregister(Blocks.CRIMSON_SIGN);
        registry.unregister(Blocks.WARPED_SIGN);
        registry.unregister(Blocks.CRIMSON_SLAB);
        registry.unregister(Blocks.WARPED_SLAB);
        registry.unregister(Blocks.CRIMSON_STAIRS);
        registry.unregister(Blocks.WARPED_STAIRS);
        registry.unregister(Blocks.CRIMSON_STEM);
        registry.unregister(Blocks.WARPED_STEM);
        registry.unregister(Blocks.STRIPPED_CRIMSON_STEM);
        registry.unregister(Blocks.STRIPPED_WARPED_STEM);
        registry.unregister(Blocks.CRIMSON_TRAPDOOR);
        registry.unregister(Blocks.WARPED_TRAPDOOR);
        registry.unregister(Blocks.CRIMSON_WALL_SIGN);
        registry.unregister(Blocks.WARPED_WALL_SIGN);
        registry.unregister(Blocks.NETHERITE_BLOCK);
        registry.unregister(Blocks.SHROOMLIGHT);
        registry.unregister(Blocks.SOUL_FIRE);
        registry.unregister(Blocks.SOUL_FIRE_LANTERN);
        registry.unregister(Blocks.SOUL_FIRE_TORCH);
        registry.unregister(Blocks.SOUL_FIRE_WALL_TORCH);
        registry.unregister(Blocks.SOUL_SOIL);
        registry.unregister(Blocks.WARPED_WART_BLOCK);
        registry.unregister(Blocks.WEEPING_VINES);
        registry.unregister(Blocks.WEEPING_VINES_PLANT);
    }

    private void modifyItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.field_22013);
        registry.unregister(Items.field_22015);
        registry.unregister(Items.field_22031);
        registry.unregister(Items.field_22032);
        registry.unregister(Items.field_21981);
        registry.unregister(Items.field_21982);
        registry.unregister(Items.field_21983);
        registry.unregister(Items.field_21984);
        registry.unregister(Items.field_21987);
        registry.unregister(Items.field_21988);
        registry.unregister(Items.field_21989);
        registry.unregister(Items.field_21990);
        registry.unregister(Items.field_21991);
        registry.unregister(Items.field_21992);
        registry.unregister(Items.field_21985);
        registry.unregister(Items.field_21986);
        registry.unregister(Items.field_21993);
        registry.unregister(Items.field_21994);
        registry.unregister(Items.field_21995);
        registry.unregister(Items.field_21996);
        registry.unregister(Items.field_21999);
        registry.unregister(Items.field_22000);
        registry.unregister(Items.field_22001);
        registry.unregister(Items.field_22002);
        registry.unregister(Items.field_22003);
        registry.unregister(Items.field_21997);
        registry.unregister(Items.field_21998);
        registry.unregister(Items.field_22006);
        registry.unregister(Items.field_22007);
        registry.unregister(Items.field_22004);
        registry.unregister(Items.field_22005);
        registry.unregister(Items.field_22008);
        registry.unregister(Items.field_22010);
        registry.unregister(Items.field_22009);
        registry.unregister(Items.field_22016);
        registry.unregister(Items.field_22017);
        registry.unregister(Items.field_22018);
        registry.unregister(Items.field_22019);
        registry.unregister(Items.HOGLIN_SPAWN_EGG);
        registry.unregister(Items.CRIMSON_SIGN);
        registry.unregister(Items.WARPED_SIGN);
        registry.unregister(Items.NETHERITE_SCRAP);
        registry.unregister(Items.NETHERITE_INGOT);
        registry.unregister(Items.NETHERITE_HELMET);
        registry.unregister(Items.NETHERITE_CHESTPLATE);
        registry.unregister(Items.NETHERITE_LEGGINGS);
        registry.unregister(Items.NETHERITE_BOOTS);
        registry.unregister(Items.NETHERITE_SWORD);
        registry.unregister(Items.NETHERITE_SHOVEL);
        registry.unregister(Items.NETHERITE_AXE);
        registry.unregister(Items.NETHERITE_PICKAXE);
        registry.unregister(Items.NETHERITE_HOE);
    }

    private void modifyEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.HOGLIN);
    }

    private static void modifyBiomeRegistry(ISimpleRegistry<Biome> registry) {
        rename(registry, Biomes.NETHER_WASTES, "nether");
        registry.unregister(Biomes.SOUL_SAND_VALLEY);
        registry.unregister(Biomes.CRIMSON_FOREST);
        registry.unregister(Biomes.WARPED_FOREST);
    }

    private void modifyParticleTypeRegistry(ISimpleRegistry<ParticleType<? extends ParticleEffect>> registry) {
        registry.unregister(ParticleTypes.ASH);
        registry.unregister(ParticleTypes.CRIMSON_SPORE);
        registry.unregister(ParticleTypes.SOUL_FIRE_FLAME);
        registry.unregister(ParticleTypes.WARPED_SPORE);
    }

    private void modifyBlockEntityRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        //registry.unregister(BlockEntityType.);
    }

    private void modifySoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_BREAK);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_STEP);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_PLACE);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_HIT);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_FALL);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE);
        registry.unregister(SoundEvents.BLOCK_BASALT_BREAK);
        registry.unregister(SoundEvents.BLOCK_BASALT_STEP);
        registry.unregister(SoundEvents.BLOCK_BASALT_PLACE);
        registry.unregister(SoundEvents.BLOCK_BASALT_HIT);
        registry.unregister(SoundEvents.BLOCK_BASALT_FALL);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_ROOTS_BREAK);
        registry.unregister(SoundEvents.BLOCK_ROOTS_STEP);
        registry.unregister(SoundEvents.BLOCK_ROOTS_PLACE);
        registry.unregister(SoundEvents.BLOCK_ROOTS_HIT);
        registry.unregister(SoundEvents.BLOCK_ROOTS_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_FALL);
        registry.unregister(SoundEvents.BLOCK_STEM_BREAK);
        registry.unregister(SoundEvents.BLOCK_STEM_STEP);
        registry.unregister(SoundEvents.BLOCK_STEM_PLACE);
        registry.unregister(SoundEvents.BLOCK_STEM_HIT);
        registry.unregister(SoundEvents.BLOCK_STEM_FALL);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_BREAK);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_STEP);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_PLACE);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_HIT);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_FALL);
        registry.unregister(SoundEvents.BLOCK_FUNGI_BREAK);
        registry.unregister(SoundEvents.BLOCK_FUNGI_STEP);
        registry.unregister(SoundEvents.BLOCK_FUNGI_PLACE);
        registry.unregister(SoundEvents.BLOCK_FUNGI_HIT);
        registry.unregister(SoundEvents.BLOCK_FUNGI_FALL);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_BREAK);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_STEP);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_PLACE);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_HIT);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_FALL);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_STEP);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_BREAK);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_STEP);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_PLACE);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_HIT);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_FALL);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_BREAK);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_STEP);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_PLACE);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_HIT);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_FALL);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_BREAK);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_STEP);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_PLACE);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_HIT);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_FALL);
    }
}
