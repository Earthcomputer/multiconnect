package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
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
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.*;
import net.minecraft.stat.Stats;
import net.minecraft.tag.*;
import net.minecraft.text.LiteralText;
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
    public void setup(boolean resourceReload) {
        TabCompletionManager.reset();
        super.setup(resourceReload);
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
                    (entity, val) -> entity.setCustomName(val.isEmpty() ? null : new LiteralText(val)));
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
            ItemStack stack = Items_1_12_2.oldItemStackToNew(new ItemStack(item), meta);
            entity.setParticleType(createParticle(type, buf -> buf.writeItemStack(stack)));
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

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.WOOL,
            Blocks.WHITE_WOOL,
            Blocks.ORANGE_WOOL,
            Blocks.MAGENTA_WOOL,
            Blocks.LIGHT_BLUE_WOOL,
            Blocks.YELLOW_WOOL,
            Blocks.LIME_WOOL,
            Blocks.PINK_WOOL,
            Blocks.GRAY_WOOL,
            Blocks.LIGHT_GRAY_WOOL,
            Blocks.CYAN_WOOL,
            Blocks.PURPLE_WOOL,
            Blocks.BLUE_WOOL,
            Blocks.BROWN_WOOL,
            Blocks.GREEN_WOOL,
            Blocks.RED_WOOL,
            Blocks.BLACK_WOOL);
        tags.add(BlockTags.PLANKS,
            Blocks.OAK_PLANKS,
            Blocks.SPRUCE_PLANKS,
            Blocks.BIRCH_PLANKS,
            Blocks.JUNGLE_PLANKS,
            Blocks.ACACIA_PLANKS,
            Blocks.DARK_OAK_PLANKS);
        tags.add(BlockTags.STONE_BRICKS,
            Blocks.STONE_BRICKS,
            Blocks.MOSSY_STONE_BRICKS,
            Blocks.CRACKED_STONE_BRICKS,
            Blocks.CHISELED_STONE_BRICKS);
        tags.add(BlockTags.WOODEN_BUTTONS, Blocks.OAK_BUTTON);
        tags.addTag(BlockTags.BUTTONS, BlockTags.WOODEN_BUTTONS);
        tags.add(BlockTags.BUTTONS, Blocks.STONE_BUTTON);
        tags.add(BlockTags.CARPETS,
            Blocks.WHITE_CARPET,
            Blocks.ORANGE_CARPET,
            Blocks.MAGENTA_CARPET,
            Blocks.LIGHT_BLUE_CARPET,
            Blocks.YELLOW_CARPET,
            Blocks.LIME_CARPET,
            Blocks.PINK_CARPET,
            Blocks.GRAY_CARPET,
            Blocks.LIGHT_GRAY_CARPET,
            Blocks.CYAN_CARPET,
            Blocks.PURPLE_CARPET,
            Blocks.BLUE_CARPET,
            Blocks.BROWN_CARPET,
            Blocks.GREEN_CARPET,
            Blocks.RED_CARPET,
            Blocks.BLACK_CARPET);
        tags.add(BlockTags.WOODEN_DOORS,
            Blocks.OAK_DOOR,
            Blocks.SPRUCE_DOOR,
            Blocks.BIRCH_DOOR,
            Blocks.JUNGLE_DOOR,
            Blocks.ACACIA_DOOR,
            Blocks.DARK_OAK_DOOR);
        tags.add(BlockTags.WOODEN_STAIRS,
            Blocks.OAK_STAIRS,
            Blocks.SPRUCE_STAIRS,
            Blocks.BIRCH_STAIRS,
            Blocks.JUNGLE_STAIRS,
            Blocks.ACACIA_STAIRS,
            Blocks.DARK_OAK_STAIRS);
        tags.add(BlockTags.WOODEN_SLABS,
            Blocks.OAK_SLAB,
            Blocks.SPRUCE_SLAB,
            Blocks.BIRCH_SLAB,
            Blocks.JUNGLE_SLAB,
            Blocks.ACACIA_SLAB,
            Blocks.DARK_OAK_SLAB);
        tags.addTag(BlockTags.DOORS, BlockTags.WOODEN_DOORS);
        tags.add(BlockTags.DOORS, Blocks.IRON_DOOR);
        tags.add(BlockTags.SAPLINGS,
            Blocks.OAK_SAPLING,
            Blocks.SPRUCE_SAPLING,
            Blocks.BIRCH_SAPLING,
            Blocks.JUNGLE_SAPLING,
            Blocks.ACACIA_SAPLING,
            Blocks.DARK_OAK_SAPLING);
        tags.add(BlockTags.DARK_OAK_LOGS,
            Blocks.DARK_OAK_LOG,
            Blocks.DARK_OAK_WOOD);
        tags.add(BlockTags.OAK_LOGS,
            Blocks.OAK_LOG,
            Blocks.OAK_WOOD);
        tags.add(BlockTags.ACACIA_LOGS,
            Blocks.ACACIA_LOG,
            Blocks.ACACIA_WOOD);
        tags.add(BlockTags.BIRCH_LOGS,
            Blocks.BIRCH_LOG,
            Blocks.BIRCH_WOOD);
        tags.add(BlockTags.JUNGLE_LOGS,
            Blocks.JUNGLE_LOG,
            Blocks.JUNGLE_WOOD);
        tags.add(BlockTags.SPRUCE_LOGS,
            Blocks.SPRUCE_LOG,
            Blocks.SPRUCE_WOOD);
        tags.addTag(BlockTags.LOGS, BlockTags.DARK_OAK_LOGS);
        tags.addTag(BlockTags.LOGS, BlockTags.OAK_LOGS);
        tags.addTag(BlockTags.LOGS, BlockTags.ACACIA_LOGS);
        tags.addTag(BlockTags.LOGS, BlockTags.BIRCH_LOGS);
        tags.addTag(BlockTags.LOGS, BlockTags.JUNGLE_LOGS);
        tags.addTag(BlockTags.LOGS, BlockTags.SPRUCE_LOGS);
        tags.add(BlockTags.ANVIL,
            Blocks.ANVIL,
            Blocks.CHIPPED_ANVIL,
            Blocks.DAMAGED_ANVIL);
        tags.add(BlockTags.ENDERMAN_HOLDABLE,
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.PODZOL,
            Blocks.SAND,
            Blocks.RED_SAND,
            Blocks.GRAVEL,
            Blocks.BROWN_MUSHROOM,
            Blocks.RED_MUSHROOM,
            Blocks.TNT,
            Blocks.CACTUS,
            Blocks.CLAY,
            Blocks.CARVED_PUMPKIN,
            Blocks.MELON,
            Blocks.MYCELIUM,
            Blocks.NETHERRACK);
        tags.add(BlockTags.FLOWER_POTS,
            Blocks.FLOWER_POT,
            Blocks.POTTED_POPPY,
            Blocks.POTTED_BLUE_ORCHID,
            Blocks.POTTED_ALLIUM,
            Blocks.POTTED_AZURE_BLUET,
            Blocks.POTTED_RED_TULIP,
            Blocks.POTTED_ORANGE_TULIP,
            Blocks.POTTED_WHITE_TULIP,
            Blocks.POTTED_PINK_TULIP,
            Blocks.POTTED_OXEYE_DAISY,
            Blocks.POTTED_DANDELION,
            Blocks.POTTED_OAK_SAPLING,
            Blocks.POTTED_SPRUCE_SAPLING,
            Blocks.POTTED_BIRCH_SAPLING,
            Blocks.POTTED_JUNGLE_SAPLING,
            Blocks.POTTED_ACACIA_SAPLING,
            Blocks.POTTED_DARK_OAK_SAPLING,
            Blocks.POTTED_RED_MUSHROOM,
            Blocks.POTTED_BROWN_MUSHROOM,
            Blocks.POTTED_DEAD_BUSH,
            Blocks.POTTED_FERN,
            Blocks.POTTED_CACTUS);
        tags.add(BlockTags.BANNERS,
            Blocks.WHITE_BANNER,
            Blocks.ORANGE_BANNER,
            Blocks.MAGENTA_BANNER,
            Blocks.LIGHT_BLUE_BANNER,
            Blocks.YELLOW_BANNER,
            Blocks.LIME_BANNER,
            Blocks.PINK_BANNER,
            Blocks.GRAY_BANNER,
            Blocks.LIGHT_GRAY_BANNER,
            Blocks.CYAN_BANNER,
            Blocks.PURPLE_BANNER,
            Blocks.BLUE_BANNER,
            Blocks.BROWN_BANNER,
            Blocks.GREEN_BANNER,
            Blocks.RED_BANNER,
            Blocks.BLACK_BANNER,
            Blocks.WHITE_WALL_BANNER,
            Blocks.ORANGE_WALL_BANNER,
            Blocks.MAGENTA_WALL_BANNER,
            Blocks.LIGHT_BLUE_WALL_BANNER,
            Blocks.YELLOW_WALL_BANNER,
            Blocks.LIME_WALL_BANNER,
            Blocks.PINK_WALL_BANNER,
            Blocks.GRAY_WALL_BANNER,
            Blocks.LIGHT_GRAY_WALL_BANNER,
            Blocks.CYAN_WALL_BANNER,
            Blocks.PURPLE_WALL_BANNER,
            Blocks.BLUE_WALL_BANNER,
            Blocks.BROWN_WALL_BANNER,
            Blocks.GREEN_WALL_BANNER,
            Blocks.RED_WALL_BANNER,
            Blocks.BLACK_WALL_BANNER);
        tags.add(BlockTags.WOODEN_PRESSURE_PLATES, Blocks.OAK_PRESSURE_PLATE);
        tags.add(BlockTags.STAIRS,
            Blocks.OAK_STAIRS,
            Blocks.COBBLESTONE_STAIRS,
            Blocks.SPRUCE_STAIRS,
            Blocks.SANDSTONE_STAIRS,
            Blocks.ACACIA_STAIRS,
            Blocks.JUNGLE_STAIRS,
            Blocks.BIRCH_STAIRS,
            Blocks.DARK_OAK_STAIRS,
            Blocks.NETHER_BRICK_STAIRS,
            Blocks.STONE_BRICK_STAIRS,
            Blocks.BRICK_STAIRS,
            Blocks.PURPUR_STAIRS,
            Blocks.QUARTZ_STAIRS,
            Blocks.RED_SANDSTONE_STAIRS);
        tags.add(BlockTags.SLABS,
            Blocks.SMOOTH_STONE_SLAB,
            Blocks.STONE_BRICK_SLAB,
            Blocks.SANDSTONE_SLAB,
            Blocks.ACACIA_SLAB,
            Blocks.BIRCH_SLAB,
            Blocks.DARK_OAK_SLAB,
            Blocks.JUNGLE_SLAB,
            Blocks.OAK_SLAB,
            Blocks.SPRUCE_SLAB,
            Blocks.PURPUR_SLAB,
            Blocks.QUARTZ_SLAB,
            Blocks.RED_SANDSTONE_SLAB,
            Blocks.BRICK_SLAB,
            Blocks.COBBLESTONE_SLAB,
            Blocks.NETHER_BRICK_SLAB,
            Blocks.PETRIFIED_OAK_SLAB);
        tags.add(BlockTags.SAND,
            Blocks.SAND,
            Blocks.RED_SAND);
        tags.add(BlockTags.RAILS,
            Blocks.RAIL,
            Blocks.POWERED_RAIL,
            Blocks.DETECTOR_RAIL,
            Blocks.ACTIVATOR_RAIL);
        tags.add(BlockTags.ICE,
            Blocks.ICE,
            Blocks.PACKED_ICE,
            Blocks.BLUE_ICE,
            Blocks.FROSTED_ICE);
        tags.add(BlockTags.VALID_SPAWN,
            Blocks.GRASS_BLOCK,
            Blocks.PODZOL);
        tags.add(BlockTags.LEAVES,
            Blocks.JUNGLE_LEAVES,
            Blocks.OAK_LEAVES,
            Blocks.SPRUCE_LEAVES,
            Blocks.DARK_OAK_LEAVES,
            Blocks.ACACIA_LEAVES,
            Blocks.BIRCH_LEAVES);
        tags.add(BlockTags.IMPERMEABLE,
            Blocks.GLASS,
            Blocks.WHITE_STAINED_GLASS,
            Blocks.ORANGE_STAINED_GLASS,
            Blocks.MAGENTA_STAINED_GLASS,
            Blocks.LIGHT_BLUE_STAINED_GLASS,
            Blocks.YELLOW_STAINED_GLASS,
            Blocks.LIME_STAINED_GLASS,
            Blocks.PINK_STAINED_GLASS,
            Blocks.GRAY_STAINED_GLASS,
            Blocks.LIGHT_GRAY_STAINED_GLASS,
            Blocks.CYAN_STAINED_GLASS,
            Blocks.PURPLE_STAINED_GLASS,
            Blocks.BLUE_STAINED_GLASS,
            Blocks.BROWN_STAINED_GLASS,
            Blocks.GREEN_STAINED_GLASS,
            Blocks.RED_STAINED_GLASS,
            Blocks.BLACK_STAINED_GLASS);
        tags.add(BlockTags.WOODEN_TRAPDOORS, Blocks.OAK_TRAPDOOR);
        tags.addTag(BlockTags.TRAPDOORS, BlockTags.WOODEN_TRAPDOORS);
        tags.add(BlockTags.TRAPDOORS, Blocks.IRON_TRAPDOOR);
        tags.add(BlockTags.CORAL_BLOCKS);
        tags.add(BlockTags.CORALS);
        tags.add(BlockTags.WALL_CORALS);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.WOOL, BlockTags.WOOL);
        copyBlocks(tags, blockTags, ItemTags.PLANKS, BlockTags.PLANKS);
        copyBlocks(tags, blockTags, ItemTags.STONE_BRICKS, BlockTags.STONE_BRICKS);
        copyBlocks(tags, blockTags, ItemTags.WOODEN_BUTTONS, BlockTags.WOODEN_BUTTONS);
        copyBlocks(tags, blockTags, ItemTags.BUTTONS, BlockTags.BUTTONS);
        copyBlocks(tags, blockTags, ItemTags.CARPETS, BlockTags.CARPETS);
        copyBlocks(tags, blockTags, ItemTags.WOODEN_DOORS, BlockTags.WOODEN_DOORS);
        copyBlocks(tags, blockTags, ItemTags.WOODEN_STAIRS, BlockTags.WOODEN_STAIRS);
        copyBlocks(tags, blockTags, ItemTags.WOODEN_SLABS, BlockTags.WOODEN_SLABS);
        copyBlocks(tags, blockTags, ItemTags.WOODEN_PRESSURE_PLATES, BlockTags.WOODEN_PRESSURE_PLATES);
        copyBlocks(tags, blockTags, ItemTags.DOORS, BlockTags.DOORS);
        copyBlocks(tags, blockTags, ItemTags.SAPLINGS, BlockTags.SAPLINGS);
        copyBlocks(tags, blockTags, ItemTags.OAK_LOGS, BlockTags.OAK_LOGS);
        copyBlocks(tags, blockTags, ItemTags.DARK_OAK_LOGS, BlockTags.DARK_OAK_LOGS);
        copyBlocks(tags, blockTags, ItemTags.BIRCH_LOGS, BlockTags.BIRCH_LOGS);
        copyBlocks(tags, blockTags, ItemTags.ACACIA_LOGS, BlockTags.ACACIA_LOGS);
        copyBlocks(tags, blockTags, ItemTags.SPRUCE_LOGS, BlockTags.SPRUCE_LOGS);
        copyBlocks(tags, blockTags, ItemTags.JUNGLE_LOGS, BlockTags.JUNGLE_LOGS);
        copyBlocks(tags, blockTags, ItemTags.LOGS, BlockTags.LOGS);
        copyBlocks(tags, blockTags, ItemTags.SAND, BlockTags.SAND);
        copyBlocks(tags, blockTags, ItemTags.SLABS, BlockTags.SLABS);
        copyBlocks(tags, blockTags, ItemTags.STAIRS, BlockTags.STAIRS);
        copyBlocks(tags, blockTags, ItemTags.ANVIL, BlockTags.ANVIL);
        copyBlocks(tags, blockTags, ItemTags.RAILS, BlockTags.RAILS);
        copyBlocks(tags, blockTags, ItemTags.LEAVES, BlockTags.LEAVES);
        copyBlocks(tags, blockTags, ItemTags.WOODEN_TRAPDOORS, BlockTags.WOODEN_TRAPDOORS);
        copyBlocks(tags, blockTags, ItemTags.TRAPDOORS, BlockTags.TRAPDOORS);
        tags.add(ItemTags.BANNERS,
            Items.WHITE_BANNER,
            Items.ORANGE_BANNER,
            Items.MAGENTA_BANNER,
            Items.LIGHT_BLUE_BANNER,
            Items.YELLOW_BANNER,
            Items.LIME_BANNER,
            Items.PINK_BANNER,
            Items.GRAY_BANNER,
            Items.LIGHT_GRAY_BANNER,
            Items.CYAN_BANNER,
            Items.PURPLE_BANNER,
            Items.BLUE_BANNER,
            Items.BROWN_BANNER,
            Items.GREEN_BANNER,
            Items.RED_BANNER,
            Items.BLACK_BANNER);
        tags.add(ItemTags.BOATS,
            Items.OAK_BOAT,
            Items.SPRUCE_BOAT,
            Items.BIRCH_BOAT,
            Items.JUNGLE_BOAT,
            Items.ACACIA_BOAT,
            Items.DARK_OAK_BOAT);
        tags.add(ItemTags.FISHES,
            Items.COD,
            Items.COOKED_COD,
            Items.SALMON,
            Items.COOKED_SALMON,
            Items.PUFFERFISH,
            Items.TROPICAL_FISH);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraFluidTags(TagRegistry<Fluid> tags) {
        tags.add(FluidTags.WATER, Fluids.WATER, Fluids.FLOWING_WATER);
        tags.add(FluidTags.LAVA, Fluids.LAVA, Fluids.FLOWING_LAVA);
        super.addExtraFluidTags(tags);
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
