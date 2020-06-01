package net.earthcomputer.multiconnect.protocols.v1_10;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.v1_10.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
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
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.command.CommandSource;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Set;

public class Protocol_1_10 extends Protocol_1_11 {

    private static final TrackedData<Byte> OLD_GUARDIAN_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> OLD_ZOMBIE_TYPE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> OLD_ZOMBIE_CONVERTING = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Byte> OLD_HORSE_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Integer> OLD_HORSE_TYPE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_HORSE_VARIANT = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_HORSE_ARMOR = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> OLD_SKELETON_TYPE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    public static void registerTranslators() {

    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_10, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_10, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_10, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_10, Registry.BLOCK_ENTITY_TYPE, this::mutateBlockEntityTypeRegistry);
        mutator.mutate(Protocols.V1_10, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
        mutator.mutate(Protocols.V1_10, Registry.PARTICLE_TYPE, this::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_10, Registry.ENCHANTMENT, this::mutateEnchantmentRegistry);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.purge(Blocks.OBSERVER);
        registry.purge(Blocks.WHITE_SHULKER_BOX);
        registry.purge(Blocks.ORANGE_SHULKER_BOX);
        registry.purge(Blocks.MAGENTA_SHULKER_BOX);
        registry.purge(Blocks.LIGHT_BLUE_SHULKER_BOX);
        registry.purge(Blocks.YELLOW_SHULKER_BOX);
        registry.purge(Blocks.LIME_SHULKER_BOX);
        registry.purge(Blocks.PINK_SHULKER_BOX);
        registry.purge(Blocks.GRAY_SHULKER_BOX);
        registry.purge(Blocks.LIGHT_GRAY_SHULKER_BOX);
        registry.purge(Blocks.CYAN_SHULKER_BOX);
        registry.purge(Blocks.PURPLE_SHULKER_BOX);
        registry.purge(Blocks.BLUE_SHULKER_BOX);
        registry.purge(Blocks.BROWN_SHULKER_BOX);
        registry.purge(Blocks.GREEN_SHULKER_BOX);
        registry.purge(Blocks.RED_SHULKER_BOX);
        registry.purge(Blocks.BLACK_SHULKER_BOX);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.purge(Items.TOTEM_OF_UNDYING);
        registry.purge(Items.SHULKER_SHELL);
    }

    private void mutateEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        //registry.purge(EntityType.ELDER_GUARDIAN);
        //registry.purge(EntityType.WITHER_SKELETON);
        //registry.purge(EntityType.STRAY);
        //registry.purge(EntityType.HUSK);
        //registry.purge(EntityType.ZOMBIE_VILLAGER);
        //registry.purge(EntityType.SKELETON_HORSE);
        //registry.purge(EntityType.ZOMBIE_HORSE);
        //registry.purge(EntityType.DONKEY);
        //registry.purge(EntityType.MULE);
        registry.purge(EntityType.EVOKER_FANGS);
        registry.purge(EntityType.EVOKER);
        registry.purge(EntityType.VEX);
        registry.purge(EntityType.VINDICATOR);
        registry.purge(EntityType.LLAMA);
        registry.purge(EntityType.LLAMA_SPIT);
    }

    private void mutateBlockEntityTypeRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.purge(BlockEntityType.SHULKER_BOX);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.BLOCK_SHULKER_BOX_CLOSE);
        registry.unregister(SoundEvents.BLOCK_SHULKER_BOX_OPEN);
        registry.unregister(SoundEvents.ENTITY_ELDER_GUARDIAN_FLOP);
        registry.unregister(SoundEvents.ENTITY_EVOKER_FANGS_ATTACK);
        registry.unregister(SoundEvents.ENTITY_EVOKER_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_EVOKER_CAST_SPELL);
        registry.unregister(SoundEvents.ENTITY_EVOKER_DEATH);
        registry.unregister(SoundEvents.ENTITY_EVOKER_HURT);
        registry.unregister(SoundEvents.ENTITY_EVOKER_PREPARE_ATTACK);
        registry.unregister(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON);
        registry.unregister(SoundEvents.ENTITY_EVOKER_PREPARE_WOLOLO);
        registry.unregister(SoundEvents.ENTITY_LLAMA_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_LLAMA_ANGRY);
        registry.unregister(SoundEvents.ENTITY_LLAMA_CHEST);
        registry.unregister(SoundEvents.ENTITY_LLAMA_DEATH);
        registry.unregister(SoundEvents.ENTITY_LLAMA_EAT);
        registry.unregister(SoundEvents.ENTITY_LLAMA_HURT);
        registry.unregister(SoundEvents.ENTITY_LLAMA_SPIT);
        registry.unregister(SoundEvents.ENTITY_LLAMA_STEP);
        registry.unregister(SoundEvents.ENTITY_LLAMA_SWAG);
        registry.unregister(SoundEvents.ENTITY_MULE_CHEST);
        registry.unregister(SoundEvents.ENTITY_VEX_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_VEX_CHARGE);
        registry.unregister(SoundEvents.ENTITY_VEX_DEATH);
        registry.unregister(SoundEvents.ENTITY_VEX_HURT);
        registry.unregister(SoundEvents.ENTITY_VINDICATOR_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_VINDICATOR_DEATH);
        registry.unregister(SoundEvents.ENTITY_VINDICATOR_HURT);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_ELYTRA);
        registry.unregister(SoundEvents.ITEM_BOTTLE_EMPTY);
        registry.unregister(SoundEvents.ITEM_TOTEM_USE);
    }

    private void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        registry.purge(ParticleTypes.SPIT);
        registry.purge(ParticleTypes.TOTEM_OF_UNDYING);
    }

    private void mutateEnchantmentRegistry(ISimpleRegistry<Enchantment> registry) {
        registry.purge(Enchantments.BINDING_CURSE);
        registry.purge(Enchantments.VANISHING_CURSE);
    }

    @Override
    public List<RecipeInfo<?>> getCraftingRecipes() {
        List<RecipeInfo<?>> recipes = super.getCraftingRecipes();
        recipes.removeIf(recipe -> recipe.getOutput().getItem() instanceof BlockItem && ((BlockItem) recipe.getOutput().getItem()).getBlock() instanceof ShulkerBoxBlock);
        recipes.removeIf(recipe -> recipe.getOutput().getItem() == Items.OBSERVER);
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("locate").remove();
        BrigadierRemover.of(dispatcher).get("title").get("player").get("actionbar").remove();
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
                EntityType<?> newType;
                switch (val) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        newType = EntityType.ZOMBIE_VILLAGER;
                        break;
                    case 6:
                        newType = EntityType.HUSK;
                        break;
                    case 0:
                    default:
                        newType = EntityType.ZOMBIE;
                }
                if (newType != entity.getType()) {
                    entity = (ZombieEntity) changeEntityType(entity, newType);
                }
                entity.getDataTracker().set(Protocol_1_13_2.OLD_ZOMBIE_VILLAGER_PROFESSION, val - 1);
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
                entity.getDataTracker().set(HorseBaseEntityAccessor.getHorseFlags(), (val & 7) | ((val & ~15) >>> 1));
                if (entity instanceof AbstractDonkeyEntity) {
                    ((AbstractDonkeyEntity) entity).setHasChest((val & 8) != 0);
                }
            });
            DataTrackerManager.registerOldTrackedData(HorseBaseEntity.class, OLD_HORSE_TYPE, 0, (entity, val) -> {
                EntityType<?> newType;
                switch (val) {
                    case 1:
                        newType = EntityType.DONKEY;
                        break;
                    case 2:
                        newType = EntityType.MULE;
                        break;
                    case 3:
                        newType = EntityType.ZOMBIE_HORSE;
                        break;
                    case 4:
                        newType = EntityType.SKELETON_HORSE;
                        break;
                    case 0:
                    default:
                        newType = EntityType.HORSE;
                }
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
        if (clazz == AbstractSkeletonEntity.class && data == Protocol_1_13_2.OLD_SKELETON_ATTACKING) {
            DataTrackerManager.registerOldTrackedData(AbstractSkeletonEntity.class, OLD_SKELETON_TYPE, 0, (entity, val) -> {
                EntityType<?> newType;
                switch (val) {
                    case 1:
                        newType = EntityType.WITHER_SKELETON;
                        break;
                    case 2:
                        newType = EntityType.STRAY;
                        break;
                    case 0:
                    default:
                        newType = EntityType.SKELETON;
                }
                if (newType != entity.getType()) {
                    changeEntityType(entity, newType);
                }
            });
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
        // TODO
        return entity;
    }
}
