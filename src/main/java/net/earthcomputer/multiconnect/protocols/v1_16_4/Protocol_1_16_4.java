package net.earthcomputer.multiconnect.protocols.v1_16_4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.ShulkerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.event.GameEvent;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Protocol_1_16_4 extends Protocol_1_17 {
    public static final int BIOME_ARRAY_LENGTH = 1024;

    private static final TrackedData<Optional<BlockPos>> OLD_SHULKER_ATTACHED_POSITION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // entity id
            buf.readBoolean(); // hardcode
            buf.readByte(); // game mode
            buf.readByte(); // previous game mode
            int numDimensions = buf.readVarInt();
            for (int i = 0; i < numDimensions; i++) {
                buf.readIdentifier(); // dimension id
            }
            buf.disablePassthroughMode();
            Codec<Set<Identifier>> dimensionSetCodec = Codec.list(Utils.singletonKeyCodec("name", Identifier.CODEC))
                    .xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
            Utils.translateDynamicRegistries(
                    buf,
                    Utils.singletonKeyCodec("minecraft:dimension_type", Utils.singletonKeyCodec("value", dimensionSetCodec)),
                    ImmutableSet.of(new Identifier("overworld"), new Identifier("the_nether"), new Identifier("the_end"), new Identifier("overworld_caves"))::equals
            );
            Utils.translateDimensionType(buf);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int x = buf.readInt();
            int z = buf.readInt();
            buf.disablePassthroughMode();
            boolean fullChunk = buf.readBoolean();
            PendingFullChunkData.setPendingFullChunk(new ChunkPos(x, z), fullChunk);
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {buf.readVarInt()})); // vertical strip bitmask
            buf.enablePassthroughMode();
            buf.readCompoundTag(); // heightmaps
            if (fullChunk) {
                buf.readIntArray(BiomeArray.DEFAULT_LENGTH);
            } else {
                // TODO: get the actual biome array from somewhere
                buf.pendingRead(int[].class, new int[BIOME_ARRAY_LENGTH]);
            }
            buf.disablePassthroughMode();
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(LightUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // x
            buf.readVarInt(); // z
            buf.readBoolean(); // trust edges
            buf.disablePassthroughMode();
            int skyLightMask = buf.readVarInt();
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {skyLightMask})); // sky light mask
            int blockLightMask = buf.readVarInt();
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {blockLightMask})); // block light mask
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {buf.readVarInt()})); // filled sky light mask
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {buf.readVarInt()})); // filled block light mask
            int numSkyUpdates = Integer.bitCount(skyLightMask);
            buf.pendingRead(VarInt.class, new VarInt(numSkyUpdates));
            buf.enablePassthroughMode();
            for (int i = 0; i < numSkyUpdates; i++) {
                buf.readByteArray(2048); // sky light update
            }
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(Integer.bitCount(blockLightMask))); // num block light updates
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(SynchronizeTagsS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(5)); // number of tags
            buf.pendingRead(Identifier.class, new Identifier("block"));
            TagGroup.class_5748.method_33160(buf); // block tags
            buf.pendingRead(Identifier.class, new Identifier("item"));
            TagGroup.class_5748.method_33160(buf); // item tags
            buf.pendingRead(Identifier.class, new Identifier("fluid"));
            TagGroup.class_5748.method_33160(buf); // fluid tags
            buf.pendingRead(Identifier.class, new Identifier("entity_type"));
            TagGroup.class_5748.method_33160(buf); // entity type tags
            buf.disablePassthroughMode();
            buf.pendingRead(Identifier.class, new Identifier("game_event"));
            buf.pendingRead(VarInt.class, new VarInt(0)); // step count
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ResourcePackSendS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(32767); // url
            buf.readString(40); // hash
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, false); // required
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        insertAfter(packets, MapUpdateS2CPacket.class, PacketInfo.of(MapUpdateS2CPacket_1_16_4.class, MapUpdateS2CPacket_1_16_4::new));
        remove(packets, MapUpdateS2CPacket.class);
        remove(packets, VibrationS2CPacket.class);
        return packets;
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_16_4, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.ENTITY_TYPE, this::mutateEntityRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.BLOCK_ENTITY_TYPE, this::mutateBlockEntityRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.PARTICLE_TYPE, this::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
    }

    @Override
    public void mutateDynamicRegistries(RegistryMutator mutator, DynamicRegistryManager.Impl registries) {
        super.mutateDynamicRegistries(mutator, registries);
        addRegistry(registries, Registry.DIMENSION_TYPE_KEY);
        addRegistry(registries, Registry.BIOME_KEY);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.WATER_CAULDRON);
        registry.unregister(Blocks.LAVA_CAULDRON);
        registry.unregister(Blocks.POWDER_SNOW_CAULDRON);
        rename(registry, Blocks.DIRT_PATH, "grass_path");
        registry.unregister(Blocks.CANDLE);
        registry.unregister(Blocks.WHITE_CANDLE);
        registry.unregister(Blocks.ORANGE_CANDLE);
        registry.unregister(Blocks.MAGENTA_CANDLE);
        registry.unregister(Blocks.LIGHT_BLUE_CANDLE);
        registry.unregister(Blocks.YELLOW_CANDLE);
        registry.unregister(Blocks.LIME_CANDLE);
        registry.unregister(Blocks.PINK_CANDLE);
        registry.unregister(Blocks.GRAY_CANDLE);
        registry.unregister(Blocks.LIGHT_GRAY_CANDLE);
        registry.unregister(Blocks.CYAN_CANDLE);
        registry.unregister(Blocks.PURPLE_CANDLE);
        registry.unregister(Blocks.BLUE_CANDLE);
        registry.unregister(Blocks.BROWN_CANDLE);
        registry.unregister(Blocks.GREEN_CANDLE);
        registry.unregister(Blocks.RED_CANDLE);
        registry.unregister(Blocks.BLACK_CANDLE);
        registry.unregister(Blocks.CANDLE_CAKE);
        registry.unregister(Blocks.WHITE_CANDLE_CAKE);
        registry.unregister(Blocks.ORANGE_CANDLE_CAKE);
        registry.unregister(Blocks.MAGENTA_CANDLE_CAKE);
        registry.unregister(Blocks.LIGHT_BLUE_CANDLE_CAKE);
        registry.unregister(Blocks.YELLOW_CANDLE_CAKE);
        registry.unregister(Blocks.LIME_CANDLE_CAKE);
        registry.unregister(Blocks.PINK_CANDLE_CAKE);
        registry.unregister(Blocks.GRAY_CANDLE_CAKE);
        registry.unregister(Blocks.LIGHT_GRAY_CANDLE_CAKE);
        registry.unregister(Blocks.CYAN_CANDLE_CAKE);
        registry.unregister(Blocks.PURPLE_CANDLE_CAKE);
        registry.unregister(Blocks.BLUE_CANDLE_CAKE);
        registry.unregister(Blocks.BROWN_CANDLE_CAKE);
        registry.unregister(Blocks.GREEN_CANDLE_CAKE);
        registry.unregister(Blocks.RED_CANDLE_CAKE);
        registry.unregister(Blocks.BLACK_CANDLE_CAKE);
        registry.unregister(Blocks.AMETHYST_BLOCK);
        registry.unregister(Blocks.BUDDING_AMETHYST);
        registry.unregister(Blocks.AMETHYST_CLUSTER);
        registry.unregister(Blocks.LARGE_AMETHYST_BUD);
        registry.unregister(Blocks.MEDIUM_AMETHYST_BUD);
        registry.unregister(Blocks.SMALL_AMETHYST_BUD);
        registry.unregister(Blocks.TUFF);
        registry.unregister(Blocks.CALCITE);
        registry.unregister(Blocks.TINTED_GLASS);
        registry.unregister(Blocks.POWDER_SNOW);
        registry.unregister(Blocks.SCULK_SENSOR);
        registry.unregister(Blocks.WEATHERED_COPPER_BLOCK);
        registry.unregister(Blocks.SEMI_WEATHERED_COPPER_BLOCK);
        registry.unregister(Blocks.LIGHTLY_WEATHERED_COPPER_BLOCK);
        registry.unregister(Blocks.COPPER_BLOCK);
        registry.unregister(Blocks.COPPER_ORE);
        registry.unregister(Blocks.WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.SEMI_WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.LIGHTLY_WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.CUT_COPPER);
        registry.unregister(Blocks.WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.SEMI_WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.LIGHTLY_WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.SEMI_WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.LIGHTLY_WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.CUT_COPPER_SLAB);
        registry.unregister(Blocks.WAXED_COPPER);
        registry.unregister(Blocks.WAXED_SEMI_WEATHERED_COPPER);
        registry.unregister(Blocks.WAXED_LIGHTLY_WEATHERED_COPPER);
        registry.unregister(Blocks.WAXED_CUT_COPPER);
        registry.unregister(Blocks.WAXED_SEMI_WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.WAXED_LIGHTLY_WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.WAXED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WAXED_SEMI_WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WAXED_LIGHTLY_WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WAXED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.WAXED_SEMI_WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.WAXED_LIGHTLY_WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.LIGHTNING_ROD);
        registry.unregister(Blocks.POINTED_DRIPSTONE);
        registry.unregister(Blocks.DRIPSTONE_BLOCK);
        registry.unregister(Blocks.GLOW_LICHEN);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        rename(registry, Items.DIRT_PATH, "grass_path");
        registry.unregister(Items.JACK_O_LANTERN);
        insertAfter(registry, Items.GLOWSTONE, Items.JACK_O_LANTERN, "jack_o_lantern");
        registry.unregister(Items.COPPER_INGOT);
        registry.unregister(Items.BUNDLE);
        registry.unregister(Items.AMETHYST_SHARD);
        registry.unregister(Items.SPYGLASS);
        registry.unregister(Items.POWDER_SNOW_BUCKET);
        registry.unregister(Items.AXOLOTL_BUCKET);
        registry.unregister(Items.GLOW_ITEM_FRAME);
        registry.unregister(Items.GLOW_INK_SAC);
    }

    private void mutateEntityRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.AXOLOTL);
        registry.unregister(EntityType.GLOW_ITEM_FRAME);
        registry.unregister(EntityType.GLOW_SQUID);
    }

    private void mutateBlockEntityRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.SCULK_SENSOR);
    }

    private void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        registry.unregister(ParticleTypes.SMALL_FLAME);
        registry.unregister(ParticleTypes.SNOWFLAKE);
        registry.unregister(ParticleTypes.DRIPPING_DRIPSTONE_LAVA);
        registry.unregister(ParticleTypes.FALLING_DRIPSTONE_LAVA);
        registry.unregister(ParticleTypes.DRIPPING_DRIPSTONE_WATER);
        registry.unregister(ParticleTypes.FALLING_DRIPSTONE_WATER);
        registry.unregister(ParticleTypes.DUST_COLOR_TRANSITION);
        registry.unregister(ParticleTypes.VIBRATION);
        registry.unregister(ParticleTypes.GLOW_SQUID_INK);
        registry.unregister(ParticleTypes.GLOW);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_PLACE);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_STEP);
        registry.unregister(SoundEvents.BLOCK_CAKE_ADD_CANDLE);
        registry.unregister(SoundEvents.BLOCK_CALCITE_BREAK);
        registry.unregister(SoundEvents.BLOCK_CALCITE_FALL);
        registry.unregister(SoundEvents.BLOCK_CALCITE_HIT);
        registry.unregister(SoundEvents.BLOCK_CALCITE_PLACE);
        registry.unregister(SoundEvents.BLOCK_CALCITE_STEP);
        registry.unregister(SoundEvents.BLOCK_CANDLE_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_CANDLE_BREAK);
        registry.unregister(SoundEvents.BLOCK_CANDLE_EXTINGUISH);
        registry.unregister(SoundEvents.BLOCK_CANDLE_FALL);
        registry.unregister(SoundEvents.BLOCK_CANDLE_HIT);
        registry.unregister(SoundEvents.BLOCK_CANDLE_PLACE);
        registry.unregister(SoundEvents.BLOCK_CANDLE_STEP);
        registry.unregister(SoundEvents.BLOCK_COPPER_BREAK);
        registry.unregister(SoundEvents.BLOCK_COPPER_FALL);
        registry.unregister(SoundEvents.BLOCK_COPPER_HIT);
        registry.unregister(SoundEvents.BLOCK_COPPER_PLACE);
        registry.unregister(SoundEvents.BLOCK_COPPER_STEP);
        registry.unregister(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_BREAK);
        registry.unregister(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_PLACE);
        registry.unregister(SoundEvents.BLOCK_MEDIUM_AMETHYST_BUD_BREAK);
        registry.unregister(SoundEvents.BLOCK_MEDIUM_AMETHYST_BUD_PLACE);
        registry.unregister(SoundEvents.ENTITY_MINECART_INSIDE_UNDERWATER);
        registry.unregister(SoundEvents.BLOCK_SMALL_AMETHYST_BUD_BREAK);
        registry.unregister(SoundEvents.BLOCK_SMALL_AMETHYST_BUD_PLACE);
        registry.unregister(SoundEvents.ITEM_SPYGLASS_USE);
        registry.unregister(SoundEvents.ITEM_SPYGLASS_STOP_USING);
        registry.unregister(SoundEvents.BLOCK_TUFF_BREAK);
        registry.unregister(SoundEvents.BLOCK_TUFF_FALL);
        registry.unregister(SoundEvents.BLOCK_TUFF_HIT);
        registry.unregister(SoundEvents.BLOCK_TUFF_PLACE);
        registry.unregister(SoundEvents.BLOCK_TUFF_STEP);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
        registry.unregister(SoundEvents.ENTITY_PLAYER_HURT_FREEZE);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_BREAK);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_FALL);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_HIT);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_PLACE);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_STEP);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_BREAK);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_STEP);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_PLACE);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_HIT);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_FALL);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_LAND);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING_STOP);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_BREAK);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_FALL);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_HIT);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_PLACE);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_STEP);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_ATTACK);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_DEATH);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_HURT);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_IDLE_AIR);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_IDLE_WATER);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_SPLASH);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_SWIM);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_AXOLOTL);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_AXOLOTL);
        registry.unregister(SoundEvents.ITEM_DYE_USE);
        registry.unregister(SoundEvents.ITEM_GLOW_INK_SAC_USE);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_DEATH);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_HURT);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_SQUIRT);
        registry.unregister(SoundEvents.ITEM_INK_SAC_USE);
    }

    @Override
    protected Stream<BlockState> getStatesForBlock(Block block) {
        if (block == Blocks.CAULDRON) {
            return Stream.of(Blocks.CAULDRON.getDefaultState(),
                    Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 1),
                    Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 2),
                    Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        }
        return super.getStatesForBlock(block);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() instanceof AbstractRailBlock && state.get(AbstractRailBlock.WATERLOGGED)) {
            return false;
        }
        if (state.getBlock() instanceof AbstractSignBlock && state.get(SignBlock.field_28426)) {
            return false;
        }
        return super.acceptBlockState(state);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.CANDLES);
        tags.add(BlockTags.CANDLE_CAKES);
        tags.add(BlockTags.CAULDRONS, Blocks.CAULDRON, Blocks.WATER_CAULDRON);
        tags.add(BlockTags.CRYSTAL_SOUND_BLOCKS);
        tags.add(BlockTags.INSIDE_STEP_SOUND_BLOCKS, Blocks.SNOW);
        tags.addTag(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS, BlockTags.BASE_STONE_OVERWORLD);
        tags.add(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS, Blocks.DIRT);
        tags.addTag(BlockTags.OCCLUDES_VIBRATION_SIGNALS, BlockTags.WOOL);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        tags.add(ItemTags.IGNORED_BY_PIGLIN_BABIES, Items.LEATHER);
        tags.add(ItemTags.PIGLIN_FOOD, Items.PORKCHOP, Items.COOKED_PORKCHOP);
        tags.add(ItemTags.CANDLES);
        tags.add(ItemTags.FREEZE_IMMUNE_WEARABLES, Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET);
        tags.add(ItemTags.AXOLOTL_TEMPT_ITEMS, Items.TROPICAL_FISH, Items.TROPICAL_FISH_BUCKET);
        tags.addTag(ItemTags.OCCLUDES_VIBRATION_SIGNALS, ItemTags.WOOL);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS, EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH);
        tags.add(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID);
        tags.add(EntityTypeTags.AXOLOTL_TEMPTED_HOSTILES, EntityType.DROWNED, EntityType.GUARDIAN);
        super.addExtraEntityTags(tags);
    }

    @Override
    public void addExtraGameEventTags(TagRegistry<GameEvent> tags) {
        tags.add(GameEventTags.VIBRATIONS);
        tags.add(GameEventTags.IGNORE_VIBRATIONS_STEPPING_CAREFULLY);
        super.addExtraGameEventTags(tags);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getFrozenTicks()) {
            return false;
        }
        if (clazz == ShulkerEntity.class && data == ShulkerEntityAccessor.getPeekAmount()) {
            DataTrackerManager.registerOldTrackedData(ShulkerEntity.class, OLD_SHULKER_ATTACHED_POSITION, Optional.empty(), (entity, pos) -> {});
        }
        return super.acceptEntityData(clazz, data);
    }
}
