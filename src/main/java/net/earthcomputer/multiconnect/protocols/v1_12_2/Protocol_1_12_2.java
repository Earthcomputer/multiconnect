package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_12_2.block.Blocks_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.*;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13_2.mixin.ZombieEntityAccessor;
import net.minecraft.block.*;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.*;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Consumer;

public class Protocol_1_12_2 extends Protocol_1_13 {
    private static final TrackedData<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_ID = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1 = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2 = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<String> OLD_CUSTOM_NAME = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.STRING);
    public static final TrackedData<Integer> OLD_MINECART_DISPLAY_TILE = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);
    public static final TrackedData<Integer> OLD_WOLF_COLLAR_COLOR = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.INTEGER);

    private static Identifier translateCustomStat(String id) {
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
            case "sneakTime" -> Stats.SNEAK_TIME;
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
    public <T> T readTrackedData(TrackedDataHandler<T> handler, PacketByteBuf buf) {
        if (handler == TrackedDataHandlerRegistry.OPTIONAL_BLOCK_STATE) {
            int stateId = buf.readVarInt();
            if (stateId == 0)
                return (T) Optional.empty();
            return (T) Optional.ofNullable(Block.STATE_IDS.get(Blocks_1_12_2.convertToStateRegistryId(stateId)));
        }
        return super.readTrackedData(handler, buf);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (!super.acceptEntityData(clazz, data))
            return false;

        if (clazz == AreaEffectCloudEntity.class && data == AreaEffectCloudEntityAccessor.getParticleId()) {
            DataTrackerManager.registerOldTrackedData(AreaEffectCloudEntity.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_ID,
                    Registry.PARTICLE_TYPE.getRawId(ParticleTypes.ENTITY_EFFECT),
                    (entity, val) -> {
                ParticleType<?> type = Registry.PARTICLE_TYPE.get(val);
                if (type == null)
                    type = ParticleTypes.ENTITY_EFFECT;
                setParticleType(entity, type);
            });
            DataTrackerManager.registerOldTrackedData(AreaEffectCloudEntity.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1,
                    0,
                    (entity, val) -> {
                ((IAreaEffectCloudEntity) entity).multiconnect_setParam1(val);
                setParticleType(entity, entity.getParticleType().getType());
            });
            DataTrackerManager.registerOldTrackedData(AreaEffectCloudEntity.class,
                    OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2,
                    0,
                    (entity, val) -> {
                ((IAreaEffectCloudEntity) entity).multiconnect_setParam2(val);
                setParticleType(entity, entity.getParticleType().getType());
            });
            return false;
        }

        if (clazz == Entity.class && data == EntityAccessor.getCustomName()) {
            DataTrackerManager.registerOldTrackedData(Entity.class, OLD_CUSTOM_NAME, "",
                    (entity, val) -> entity.setCustomName(val.isEmpty() ? null : Text.literal(val)));
            return false;
        }

        if (clazz == BoatEntity.class && data == BoatEntityAccessor.getBubbleWobbleTicks()) {
            return false;
        }

        if (clazz == ZombieEntity.class && data == ZombieEntityAccessor.getConvertingInWater()) {
            return false;
        }

        if (clazz == AbstractMinecartEntity.class) {
            TrackedData<Integer> displayTile = AbstractMinecartEntityAccessor.getCustomBlockId();
            if (data == displayTile) {
                DataTrackerManager.registerOldTrackedData(AbstractMinecartEntity.class, OLD_MINECART_DISPLAY_TILE, 0,
                        (entity, val) -> entity.getDataTracker().set(displayTile, Blocks_1_12_2.convertToStateRegistryId(val)));
                return false;
            }
        }

        if (clazz == WolfEntity.class) {
            TrackedData<Integer> collarColor = WolfEntityAccessor.getCollarColor();
            if (data == collarColor) {
                DataTrackerManager.registerOldTrackedData(WolfEntity.class, OLD_WOLF_COLLAR_COLOR, 1,
                        (entity, val) -> entity.getDataTracker().set(collarColor, 15 - val));
                return false;
            }
        }

        return true;
    }

    private static void setParticleType(AreaEffectCloudEntity entity, ParticleType<?> type) {
        IAreaEffectCloudEntity iaece = (IAreaEffectCloudEntity) entity;
        if (type.getParametersFactory() == ItemStackParticleEffect.PARAMETERS_FACTORY) {
            Item item = Registry.ITEM.get(iaece.multiconnect_getParam1());
            int meta = iaece.multiconnect_getParam2();
            // TODO: rewrite 1.12.2
//            ItemStack stack = Items_1_12_2.oldItemStackToNew(new ItemStack(item), meta);
//            entity.setParticleType(createParticle(type, buf -> buf.writeItemStack(stack)));
        } else if (type.getParametersFactory() == BlockStateParticleEffect.PARAMETERS_FACTORY) {
            entity.setParticleType(createParticle(type, buf -> buf.writeVarInt(iaece.multiconnect_getParam1())));
        } else if (type.getParametersFactory() == DustParticleEffect.PARAMETERS_FACTORY) {
            entity.setParticleType(createParticle(type, buf -> {
                buf.writeFloat(1);
                buf.writeFloat(0);
                buf.writeFloat(0);
                buf.writeFloat(1);
            }));
        } else {
            entity.setParticleType(createParticle(type, buf -> {}));
        }
    }

    private static <T extends ParticleEffect> T createParticle(ParticleType<T> type, Consumer<PacketByteBuf> function) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        function.accept(buf);
        return type.getParametersFactory().read(type, buf);
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockHardness(state, hardness);
    }

    @Override
    public BlockState getActualState(World world, BlockPos pos, BlockState state) {
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

    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
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
