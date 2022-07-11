package net.earthcomputer.multiconnect.protocols.v1_12;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_12.block.Blocks_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13.mixin.ZombieAccessor;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class Protocol_1_12_2 extends Protocol_1_13 {
    private static final EntityDataAccessor<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_ID = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1 = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2 = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> OLD_CUSTOM_NAME = SynchedDataManager.createOldEntityData(EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Integer> OLD_MINECART_DISPLAY_TILE = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> OLD_WOLF_COLLAR_COLOR = SynchedDataManager.createOldEntityData(EntityDataSerializers.INT);

    private static ResourceLocation translateCustomStat(String id) {
        return switch (id) {
            case "jump" -> Stats.JUMP;
            case "drop" -> Stats.DROP;
            case "deaths" -> Stats.DEATHS;
            case "mobKills" -> Stats.MOB_KILLS;
            case "pigOneCm" -> Stats.PIG_ONE_CM;
            case "flyOneCm" -> Stats.FLY_ONE_CM;
            case "leaveGame" -> Stats.LEAVE_GAME;
            case "diveOneCm" -> Stats.WALK_UNDER_WATER_ONE_CM;
            case "swimOneCm" -> Stats.SWIM_ONE_CM;
            case "fallOneCm" -> Stats.FALL_ONE_CM;
            case "walkOneCm" -> Stats.WALK_ONE_CM;
            case "boatOneCm" -> Stats.BOAT_ONE_CM;
            case "sneakTime" -> Stats.CROUCH_TIME;
            case "horseOneCm" -> Stats.HORSE_ONE_CM;
            case "sleepInBed" -> Stats.SLEEP_IN_BED;
            case "fishCaught" -> Stats.FISH_CAUGHT;
            case "climbOneCm" -> Stats.CLIMB_ONE_CM;
            case "aviateOneCm" -> Stats.AVIATE_ONE_CM;
            case "crouchOneCm" -> Stats.CROUCH_ONE_CM;
            case "sprintOneCm" -> Stats.SPRINT_ONE_CM;
            case "animalsBred" -> Stats.ANIMALS_BRED;
            case "chestOpened" -> Stats.OPEN_CHEST;
            case "damageTaken" -> Stats.DAMAGE_TAKEN;
            case "damageDealt" -> Stats.DAMAGE_DEALT;
            case "playerKills" -> Stats.PLAYER_KILLS;
            case "armorCleaned" -> Stats.CLEAN_ARMOR;
            case "flowerPotted" -> Stats.POT_FLOWER;
            case "recordPlayed" -> Stats.PLAY_RECORD;
            case "cauldronUsed" -> Stats.USE_CAULDRON;
            case "bannerCleaned" -> Stats.CLEAN_BANNER;
            case "itemEnchanted" -> Stats.ENCHANT_ITEM;
            case "playOneMinute" -> Stats.PLAY_TIME;
            case "minecartOneCm" -> Stats.MINECART_ONE_CM;
            case "timeSinceDeath" -> Stats.TIME_SINCE_DEATH;
            case "cauldronFilled" -> Stats.FILL_CAULDRON;
            case "noteblockTuned" -> Stats.TUNE_NOTEBLOCK;
            case "noteblockPlayed" -> Stats.PLAY_NOTEBLOCK;
            case "cakeSlicesEaten" -> Stats.EAT_CAKE_SLICE;
            case "hopperInspected" -> Stats.INSPECT_HOPPER;
            case "shulkerBoxOpened" -> Stats.OPEN_SHULKER_BOX;
            case "talkedToVillager" -> Stats.TALKED_TO_VILLAGER;
            case "enderchestOpened" -> Stats.OPEN_ENDERCHEST;
            case "dropperInspected" -> Stats.INSPECT_DROPPER;
            case "beaconInteraction" -> Stats.INTERACT_WITH_BEACON;
            case "furnaceInteraction" -> Stats.INTERACT_WITH_FURNACE;
            case "dispenserInspected" -> Stats.INSPECT_DISPENSER;
            case "tradedWithVillager" -> Stats.TRADED_WITH_VILLAGER;
            case "trappedChestTriggered" -> Stats.TRIGGER_TRAPPED_CHEST;
            case "brewingstandInteraction" -> Stats.INTERACT_WITH_BREWINGSTAND;
            case "craftingTableInteraction" -> Stats.INTERACT_WITH_CRAFTING_TABLE;
            case "junkFished" -> Protocol_1_11.JUNK_FISHED;
            case "treasureFished" -> Protocol_1_11.TREASURE_FISHED;
            default -> null;
        };
    }

    @Override
    public void setup() {
        TabCompletionManager.reset();
        super.setup();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T readEntityData(EntityDataSerializer<T> handler, FriendlyByteBuf buf) {
        if (handler == EntityDataSerializers.BLOCK_STATE) {
            int stateId = buf.readVarInt();
            if (stateId == 0)
                return (T) Optional.empty();
            return (T) Optional.ofNullable(Block.BLOCK_STATE_REGISTRY.byId(Blocks_1_12_2.convertToStateRegistryId(stateId)));
        }
        return super.readEntityData(handler, buf);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (!super.acceptEntityData(clazz, data))
            return false;

        if (clazz == AreaEffectCloud.class && data == AreaEffectCloudAccessor.getDataParticle()) {
            SynchedDataManager.registerOldEntityData(AreaEffectCloud.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_ID,
                    Registry.PARTICLE_TYPE.getId(ParticleTypes.ENTITY_EFFECT),
                    (entity, val) -> {
                ParticleType<?> type = Registry.PARTICLE_TYPE.byId(val);
                if (type == null)
                    type = ParticleTypes.ENTITY_EFFECT;
                setParticleType(entity, type);
            });
            SynchedDataManager.registerOldEntityData(AreaEffectCloud.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1,
                    0,
                    (entity, val) -> {
                ((IAreaEffectCloud) entity).multiconnect_setParam1(val);
                setParticleType(entity, entity.getParticle().getType());
            });
            SynchedDataManager.registerOldEntityData(AreaEffectCloud.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2,
                    0,
                    (entity, val) -> {
                ((IAreaEffectCloud) entity).multiconnect_setParam2(val);
                setParticleType(entity, entity.getParticle().getType());
            });
            return false;
        }

        if (clazz == Entity.class && data == EntityAccessor.getDataCustomName()) {
            SynchedDataManager.registerOldEntityData(Entity.class, OLD_CUSTOM_NAME, "",
                    (entity, val) -> entity.setCustomName(val.isEmpty() ? null : Component.literal(val)));
            return false;
        }

        if (clazz == Boat.class && data == BoatAccessor.getDataIdBubbleTime()) {
            return false;
        }

        if (clazz == Zombie.class && data == ZombieAccessor.getDataDrownedConversionId()) {
            return false;
        }

        if (clazz == AbstractMinecart.class) {
            EntityDataAccessor<Integer> displayTile = AbstractMinecartAccessor.getDataIdDisplayBlock();
            if (data == displayTile) {
                SynchedDataManager.registerOldEntityData(AbstractMinecart.class, OLD_MINECART_DISPLAY_TILE, 0,
                        (entity, val) -> entity.getEntityData().set(displayTile, Blocks_1_12_2.convertToStateRegistryId(val)));
                return false;
            }
        }

        if (clazz == Wolf.class) {
            EntityDataAccessor<Integer> collarColor = WolfAccessor.getDataCollarColor();
            if (data == collarColor) {
                SynchedDataManager.registerOldEntityData(Wolf.class, OLD_WOLF_COLLAR_COLOR, 1,
                        (entity, val) -> entity.getEntityData().set(collarColor, 15 - val));
                return false;
            }
        }

        return true;
    }

    private static void setParticleType(AreaEffectCloud entity, ParticleType<?> type) {
        IAreaEffectCloud iaece = (IAreaEffectCloud) entity;
        if (type.getDeserializer() == ItemParticleOption.DESERIALIZER) {
            Item item = Registry.ITEM.byId(iaece.multiconnect_getParam1());
            int meta = iaece.multiconnect_getParam2();
            // TODO: rewrite 1.12.2
//            ItemStack stack = Items_1_12_2.oldItemStackToNew(new ItemStack(item), meta);
//            entity.setParticleType(createParticle(type, buf -> buf.writeItemStack(stack)));
        } else if (type.getDeserializer() == BlockParticleOption.DESERIALIZER) {
            entity.setParticle(createParticle(type, buf -> buf.writeVarInt(iaece.multiconnect_getParam1())));
        } else if (type.getDeserializer() == DustParticleOptions.DESERIALIZER) {
            entity.setParticle(createParticle(type, buf -> {
                buf.writeFloat(1);
                buf.writeFloat(0);
                buf.writeFloat(0);
                buf.writeFloat(1);
            }));
        } else {
            entity.setParticle(createParticle(type, buf -> {}));
        }
    }

    private static <T extends ParticleOptions> T createParticle(ParticleType<T> type, Consumer<FriendlyByteBuf> function) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        function.accept(buf);
        return type.getDeserializer().fromNetwork(type, buf);
    }

    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockDestroySpeed(state, destroySpeed);
    }

    @Override
    public BlockState getActualState(Level world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof FlowerPotBlock) {
            if (world.getBlockEntity(pos) instanceof FlowerPotBlockEntity flowerPot) {
                BlockState flowerPotState = flowerPot.getFlowerPotState();
                if (flowerPotState != null) {
                    return flowerPotState;
                }
            }
        } else if (state.getBlock() instanceof AbstractSkullBlock) {
            if (world.getBlockEntity(pos) instanceof ISkullBlockEntity skull) {
                return skull.multiconnect_getActualState();
            }
        }
        return super.getActualState(world, pos, state);
    }

    public List<RecipeInfo<?>> getRecipes() {
        return Recipes_1_12_2.getRecipes();
    }

    public void registerCommands(CommandDispatcher<SharedSuggestionProvider> dispatcher, @Nullable Set<String> serverCommands) {
        Commands_1_12_2.register(dispatcher, serverCommands);
    }

    @Override
    public boolean shouldBlockChangeReplaceBlockEntity(Block oldBlock, Block newBlock) {
        if (!super.shouldBlockChangeReplaceBlockEntity(oldBlock, newBlock))
            return false;

        if (oldBlock instanceof AbstractSkullBlock && newBlock instanceof AbstractSkullBlock)
            return false;
        if (oldBlock instanceof AbstractBannerBlock && newBlock instanceof AbstractBannerBlock)
            return false;
        if (oldBlock instanceof FlowerPotBlock && newBlock instanceof FlowerPotBlock)
            return false;

        return true;
    }
}
