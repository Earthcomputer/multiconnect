package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.earthcomputer.multiconnect.transformer.VarLong;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;

import java.util.List;
import java.util.stream.Stream;

public class Protocol_1_16_4 extends Protocol_1_17 {
    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int x = buf.readInt();
            int z = buf.readInt();
            buf.disablePassthroughMode();
            boolean fullChunk = buf.readBoolean();
            PendingFullChunkData.setPendingFullChunk(new ChunkPos(x, z), fullChunk);
            buf.enablePassthroughMode();
            buf.readVarInt(); // vertical strip bitmask
            buf.readCompoundTag(); // heightmaps
            if (fullChunk) {
                buf.readIntArray(BiomeArray.DEFAULT_LENGTH);
            } else {
                // TODO: get the actual biome array from somewhere
                buf.pendingRead(int[].class, new int[BiomeArray.DEFAULT_LENGTH]);
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
            buf.pendingRead(VarLong.class, new VarLong(buf.readVarInt())); // sky light mask
            buf.pendingRead(VarLong.class, new VarLong(buf.readVarInt())); // block light mask
            buf.pendingRead(VarLong.class, new VarLong(buf.readVarInt())); // filled sky light mask
            buf.pendingRead(VarLong.class, new VarLong(buf.readVarInt())); // filled block light mask
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
        return packets;
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_16_4, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.PARTICLE_TYPE, this::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
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
    }

    private void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        registry.unregister(ParticleTypes.SMALL_FLAME);
        registry.unregister(ParticleTypes.SNOWFLAKE);
        registry.unregister(ParticleTypes.DRIPPING_DRIPSTONE_LAVA);
        registry.unregister(ParticleTypes.FALLING_DRIPSTONE_LAVA);
        registry.unregister(ParticleTypes.DRIPPING_DRIPSTONE_WATER);
        registry.unregister(ParticleTypes.FALLING_DRIPSTONE_WATER);
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
        if (state.getBlock() instanceof AbstractRailBlock && state.get(AbstractRailBlock.field_27096)) {
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
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        tags.add(ItemTags.IGNORED_BY_PIGLIN_BABIES, Items.LEATHER);
        tags.add(ItemTags.PIGLIN_FOOD, Items.PORKCHOP, Items.COOKED_PORKCHOP);
        tags.add(ItemTags.CANDLES);
        tags.add(ItemTags.FREEZE_IMMUNE_WEARABLES, Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS, EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH);
        super.addExtraEntityTags(tags);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getFrozenTicks()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
