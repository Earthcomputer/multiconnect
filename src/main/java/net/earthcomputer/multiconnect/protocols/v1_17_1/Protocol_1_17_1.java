package net.earthcomputer.multiconnect.protocols.v1_17_1;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.earthcomputer.multiconnect.protocols.generic.DefaultDynamicRegistries;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12_2.BlockEntities_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18;
import net.earthcomputer.multiconnect.protocols.v1_9_2.mixin.ChunkDataBlockEntityAccessor;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Protocol_1_17_1 extends Protocol_1_18 {
    private static final Logger LOGGER = LogManager.getLogger("multiconnect");
    public static final Key<BitSet> VERTICAL_STRIP_BITMASK = Key.create("verticalStripBitmask");
    private static final Key<int[]> BIOMES = Key.create("biomes");
    private static final int WIDTH_BITS = MathHelper.ceilLog2(16) - 2;
    public static final int MAX_BIOME_LENGTH = 1 << (WIDTH_BITS + WIDTH_BITS + BlockPos.SIZE_BITS_Y - 2);

    private static final Int2ObjectMap<RegistryKey<Biome>> OLD_BIOME_IDS = new Int2ObjectOpenHashMap<>();
    static {
        OLD_BIOME_IDS.put(0, BiomeKeys.OCEAN);
        OLD_BIOME_IDS.put(1, BiomeKeys.PLAINS);
        OLD_BIOME_IDS.put(2, BiomeKeys.DESERT);
        OLD_BIOME_IDS.put(3, BiomeKeys.WINDSWEPT_HILLS);
        OLD_BIOME_IDS.put(4, BiomeKeys.FOREST);
        OLD_BIOME_IDS.put(5, BiomeKeys.TAIGA);
        OLD_BIOME_IDS.put(6, BiomeKeys.SWAMP);
        OLD_BIOME_IDS.put(7, BiomeKeys.RIVER);
        OLD_BIOME_IDS.put(8, BiomeKeys.NETHER_WASTES);
        OLD_BIOME_IDS.put(9, BiomeKeys.THE_END);
        OLD_BIOME_IDS.put(10, BiomeKeys.FROZEN_OCEAN);
        OLD_BIOME_IDS.put(11, BiomeKeys.FROZEN_RIVER);
        OLD_BIOME_IDS.put(12, BiomeKeys.SNOWY_PLAINS);
        OLD_BIOME_IDS.put(13, Biomes_1_17_1.SNOWY_MOUNTAINS);
        OLD_BIOME_IDS.put(14, BiomeKeys.MUSHROOM_FIELDS);
        OLD_BIOME_IDS.put(15, Biomes_1_17_1.MUSHROOM_FIELD_SHORE);
        OLD_BIOME_IDS.put(16, BiomeKeys.BEACH);
        OLD_BIOME_IDS.put(17, Biomes_1_17_1.DESERT_HILLS);
        OLD_BIOME_IDS.put(18, Biomes_1_17_1.WOODED_HILLS);
        OLD_BIOME_IDS.put(19, Biomes_1_17_1.TAIGA_HILLS);
        OLD_BIOME_IDS.put(20, Biomes_1_17_1.MOUNTAIN_EDGE);
        OLD_BIOME_IDS.put(21, BiomeKeys.JUNGLE);
        OLD_BIOME_IDS.put(22, Biomes_1_17_1.JUNGLE_HILLS);
        OLD_BIOME_IDS.put(23, BiomeKeys.SPARSE_JUNGLE);
        OLD_BIOME_IDS.put(24, BiomeKeys.DEEP_OCEAN);
        OLD_BIOME_IDS.put(25, BiomeKeys.STONY_SHORE);
        OLD_BIOME_IDS.put(26, BiomeKeys.SNOWY_BEACH);
        OLD_BIOME_IDS.put(27, BiomeKeys.BIRCH_FOREST);
        OLD_BIOME_IDS.put(28, Biomes_1_17_1.BIRCH_FOREST_HILLS);
        OLD_BIOME_IDS.put(29, BiomeKeys.DARK_FOREST);
        OLD_BIOME_IDS.put(30, BiomeKeys.SNOWY_TAIGA);
        OLD_BIOME_IDS.put(31, Biomes_1_17_1.SNOWY_TAIGA_HILLS);
        OLD_BIOME_IDS.put(32, BiomeKeys.OLD_GROWTH_PINE_TAIGA);
        OLD_BIOME_IDS.put(33, Biomes_1_17_1.GIANT_TREE_TAIGA_HILLS);
        OLD_BIOME_IDS.put(34, BiomeKeys.WINDSWEPT_FOREST);
        OLD_BIOME_IDS.put(35, BiomeKeys.SAVANNA);
        OLD_BIOME_IDS.put(36, BiomeKeys.SAVANNA_PLATEAU);
        OLD_BIOME_IDS.put(37, BiomeKeys.BADLANDS);
        OLD_BIOME_IDS.put(38, BiomeKeys.WOODED_BADLANDS);
        OLD_BIOME_IDS.put(39, Biomes_1_17_1.BADLANDS_PLATEAU);
        OLD_BIOME_IDS.put(40, BiomeKeys.SMALL_END_ISLANDS);
        OLD_BIOME_IDS.put(41, BiomeKeys.END_MIDLANDS);
        OLD_BIOME_IDS.put(42, BiomeKeys.END_HIGHLANDS);
        OLD_BIOME_IDS.put(43, BiomeKeys.END_BARRENS);
        OLD_BIOME_IDS.put(44, BiomeKeys.WARM_OCEAN);
        OLD_BIOME_IDS.put(45, BiomeKeys.LUKEWARM_OCEAN);
        OLD_BIOME_IDS.put(46, BiomeKeys.COLD_OCEAN);
        OLD_BIOME_IDS.put(47, Biomes_1_17_1.DEEP_WARM_OCEAN);
        OLD_BIOME_IDS.put(48, BiomeKeys.DEEP_LUKEWARM_OCEAN);
        OLD_BIOME_IDS.put(49, BiomeKeys.DEEP_COLD_OCEAN);
        OLD_BIOME_IDS.put(50, BiomeKeys.DEEP_FROZEN_OCEAN);
        OLD_BIOME_IDS.put(129, BiomeKeys.SUNFLOWER_PLAINS);
        OLD_BIOME_IDS.put(130, Biomes_1_17_1.DESERT_LAKES);
        OLD_BIOME_IDS.put(131, BiomeKeys.WINDSWEPT_GRAVELLY_HILLS);
        OLD_BIOME_IDS.put(132, BiomeKeys.FLOWER_FOREST);
        OLD_BIOME_IDS.put(133, Biomes_1_17_1.TAIGA_MOUNTAINS);
        OLD_BIOME_IDS.put(134, Biomes_1_17_1.SWAMP_HILLS);
        OLD_BIOME_IDS.put(140, BiomeKeys.ICE_SPIKES);
        OLD_BIOME_IDS.put(149, Biomes_1_17_1.MODIFIED_JUNGLE);
        OLD_BIOME_IDS.put(151, Biomes_1_17_1.MODIFIED_JUNGLE_EDGE);
        OLD_BIOME_IDS.put(155, BiomeKeys.OLD_GROWTH_BIRCH_FOREST);
        OLD_BIOME_IDS.put(156, Biomes_1_17_1.TALL_BIRCH_HILLS);
        OLD_BIOME_IDS.put(157, Biomes_1_17_1.DARK_FOREST_HILLS);
        OLD_BIOME_IDS.put(158, Biomes_1_17_1.SNOWY_TAIGA_MOUNTAINS);
        OLD_BIOME_IDS.put(160, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
        OLD_BIOME_IDS.put(161, Biomes_1_17_1.GIANT_SPRUCE_TAIGA_HILLS);
        OLD_BIOME_IDS.put(162, Biomes_1_17_1.MODIFIED_GRAVELLY_MOUNTAINS);
        OLD_BIOME_IDS.put(163, BiomeKeys.WINDSWEPT_SAVANNA);
        OLD_BIOME_IDS.put(164, Biomes_1_17_1.SHATTERED_SAVANNA_PLATEAU);
        OLD_BIOME_IDS.put(165, BiomeKeys.ERODED_BADLANDS);
        OLD_BIOME_IDS.put(166, Biomes_1_17_1.MODIFIED_WOODED_BADLANDS_PLATEAU);
        OLD_BIOME_IDS.put(167, Biomes_1_17_1.MODIFIED_BADLANDS_PLATEAU);
        OLD_BIOME_IDS.put(168, BiomeKeys.BAMBOO_JUNGLE);
        OLD_BIOME_IDS.put(169, Biomes_1_17_1.BAMBOO_JUNGLE_HILLS);
        OLD_BIOME_IDS.put(170, BiomeKeys.SOUL_SAND_VALLEY);
        OLD_BIOME_IDS.put(171, BiomeKeys.CRIMSON_FOREST);
        OLD_BIOME_IDS.put(172, BiomeKeys.WARPED_FOREST);
        OLD_BIOME_IDS.put(173, BiomeKeys.BASALT_DELTAS);
        OLD_BIOME_IDS.put(174, BiomeKeys.DRIPSTONE_CAVES);
        OLD_BIOME_IDS.put(175, BiomeKeys.LUSH_CAVES);
    }

    private static int mapBiomeId(int oldId, Registry<Biome> biomeRegistry) {
        RegistryKey<Biome> newKey = OLD_BIOME_IDS.get(oldId);
        if (newKey == null) {
            return oldId;
        }
        RegistryKey<Biome> oldKey = DefaultDynamicRegistries.getInstance(Registry.BIOME_KEY).getOld(newKey);
        Biome biome = biomeRegistry.get(oldKey);
        if (biome == null) {
            return oldId;
        }
        return biomeRegistry.getRawId(biome);
    }

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // x
            buf.readInt(); // z
            buf.disablePassthroughMode();
            BitSet verticalStripBitmask = buf.readBitSet();
            buf.multiconnect_setUserData(VERTICAL_STRIP_BITMASK, verticalStripBitmask);
            buf.enablePassthroughMode();
            buf.readNbt(); // heightmaps
            buf.disablePassthroughMode();
            buf.multiconnect_setUserData(BIOMES, buf.readIntArray(MAX_BIOME_LENGTH));
            buf.enablePassthroughMode();
            int dataLength = buf.readVarInt();
            buf.readBytesSingleAlloc(dataLength); // data
            buf.disablePassthroughMode();
            List<NbtCompound> blockEntities = buf.readList(PacketByteBuf::readNbt);
            var newBlockEntities = new ArrayList<net.minecraft.network.packet.s2c.play.ChunkData.BlockEntityData>(blockEntities.size());
            for (NbtCompound blockEntity : blockEntities) {
                if (!blockEntity.contains("x", NbtElement.INT_TYPE)
                        || !blockEntity.contains("y", NbtElement.INT_TYPE)
                        || !blockEntity.contains("z", NbtElement.INT_TYPE)) {
                    continue;
                }
                if (!blockEntity.contains("id", NbtElement.STRING_TYPE)) {
                    continue;
                }
                int x = blockEntity.getInt("x");
                int y = blockEntity.getInt("y");
                int z = blockEntity.getInt("z");
                BlockEntityType<?> type;
                if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
                    type = Protocol_1_10.getBlockEntityById(blockEntity.getString("id"));
                } else {
                    Identifier id = Identifier.tryParse(blockEntity.getString("id"));
                    type = id == null ? null : Registry.BLOCK_ENTITY_TYPE.get(id);
                }
                if (type == null) {
                    continue;
                }
                blockEntity = Utils.datafix(TypeReferences.BLOCK_ENTITY, blockEntity);
                var newBlockEntity = ChunkDataBlockEntityAccessor.createChunkDataBlockEntity(
                        (ChunkSectionPos.getLocalCoord(x) << 4) | ChunkSectionPos.getLocalCoord(z),
                        y,
                        type,
                        blockEntity
                );
                newBlockEntities.add(newBlockEntity);
            }
            //noinspection unchecked
            buf.pendingReadCollection(
                    (Class<List<net.minecraft.network.packet.s2c.play.ChunkData.BlockEntityData>>) (Class<?>) List.class,
                    net.minecraft.network.packet.s2c.play.ChunkData.BlockEntityData.class,
                    newBlockEntities
            );

            buf.pendingRead(Boolean.class, true); // trust edges
            buf.pendingRead(BitSet.class, new BitSet()); // skylight mask
            buf.pendingRead(BitSet.class, new BitSet()); // block light mask
            buf.pendingRead(BitSet.class, new BitSet()); // filled skylight mask
            buf.pendingRead(BitSet.class, new BitSet()); // filled block light mask
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<byte[]>>) (Class<?>) List.class, byte[].class, new ArrayList<>()); // skylight data
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<byte[]>>) (Class<?>) List.class, byte[].class, new ArrayList<>()); // block light data

            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkData.class, buf -> {
            DimensionType dimension = ChunkDataTranslator.current().getDimension();
            int maxY = dimension.getMinimumY() + dimension.getHeight() - 1;
            int sectionCount = (maxY + 1 - dimension.getMinimumY() + 15) >> 4;
            BitSet verticalStripBitmask = ChunkDataTranslator.current().getUserData(VERTICAL_STRIP_BITMASK);
            int[] biomes = ChunkDataTranslator.current().getUserData(BIOMES);
            Int2IntMap invBiomePalette = new Int2IntOpenHashMap();
            IntList biomePalette = new IntArrayList();
            Registry<Biome> biomeRegistry = ChunkDataTranslator.current().getRegistryManager().get(Registry.BIOME_KEY);

            for (int sectionY = 0; sectionY < sectionCount; sectionY++) {
                if (verticalStripBitmask.get(sectionY)) {
                    buf.enablePassthroughMode();
                    buf.readShort(); // non-empty block count
                    ChunkData.skipPalettedContainer(buf, false, false);
                    buf.disablePassthroughMode();

                    biomePalette.clear();
                    invBiomePalette.clear();
                    int minIndex = sectionY << (WIDTH_BITS + WIDTH_BITS + WIDTH_BITS);
                    int maxIndex = minIndex + 64;
                    for (int i = minIndex; i < maxIndex && i < biomes.length; i++) {
                        int biome = biomes[i];
                        if (!invBiomePalette.containsKey(biome)) {
                            invBiomePalette.put(biome, invBiomePalette.size());
                            biomePalette.add(biome);
                        }
                    }
                    if (biomePalette.isEmpty()) {
                        continue;
                    }
                    int bitsPerBiome = MathHelper.ceilLog2(biomePalette.size());
                    buf.pendingRead(Byte.class, (byte) bitsPerBiome);
                    if (bitsPerBiome <= 2) {
                        if (bitsPerBiome == 0) {
                            bitsPerBiome = 1;
                            buf.pendingRead(VarInt.class, new VarInt(mapBiomeId(biomePalette.getInt(0), biomeRegistry)));
                        } else {
                            buf.pendingRead(VarInt.class, new VarInt(biomePalette.size()));
                            for (int i = 0; i < biomePalette.size(); i++) {
                                buf.pendingRead(VarInt.class, new VarInt(mapBiomeId(biomePalette.getInt(i), biomeRegistry)));
                            }
                        }
                    } else {
                        bitsPerBiome = MathHelper.ceilLog2(biomeRegistry.size());
                    }
                    int biomesPerLong = 64 / bitsPerBiome;
                    long[] result = new long[(64 + biomesPerLong - 1) / biomesPerLong];
                    long currentLong = 0;
                    for (int i = 0; i < 64; i++) {
                        int valueToWrite = bitsPerBiome <= 2 ? invBiomePalette.get(biomes[minIndex + i]) : mapBiomeId(biomes[minIndex + i], biomeRegistry);
                        int posInLong = i % biomesPerLong;
                        currentLong |= (long) valueToWrite << (posInLong * bitsPerBiome);
                        if (posInLong + bitsPerBiome >= 64 || i == 63) {
                            result[i / biomesPerLong] = currentLong;
                            currentLong = 0;
                        }
                    }
                    buf.pendingRead(long[].class, result);
                }
            }
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // player id
            buf.readBoolean(); // hardcore
            buf.readByte(); // gamemode
            buf.readByte(); // previous gamemode
            buf.readCollection(Sets::newHashSetWithExpectedSize, PacketByteBuf::readIdentifier); // worlds
            buf.decode(DynamicRegistryManager.Impl.CODEC); // registry manager
            buf.decode(DimensionType.REGISTRY_CODEC); // dimension type
            buf.readIdentifier(); // dimension id
            buf.readLong(); // seed
            buf.readVarInt(); // max players
            int chunkRadius = buf.readVarInt();
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(chunkRadius)); // simulation distance
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(BlockEntityUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readBlockPos(); // pos
            buf.disablePassthroughMode();
            int type = buf.readUnsignedByte();
            BlockEntityType<?> actualType = switch (type) {
                case 1 -> BlockEntityType.MOB_SPAWNER;
                case 2 -> BlockEntityType.COMMAND_BLOCK;
                case 3 -> BlockEntityType.BEACON;
                case 4 -> BlockEntityType.SKULL;
                case 5 -> {
                    if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
                        yield BlockEntities_1_12_2.FLOWER_POT;
                    } else {
                        yield BlockEntityType.CONDUIT;
                    }
                }
                case 6 -> BlockEntityType.BANNER;
                case 7 -> BlockEntityType.STRUCTURE_BLOCK;
                case 8 -> BlockEntityType.END_GATEWAY;
                case 9 -> BlockEntityType.SIGN;
                case 11 -> BlockEntityType.BED;
                case 12 -> BlockEntityType.JIGSAW;
                case 13 -> BlockEntityType.CAMPFIRE;
                case 14 -> BlockEntityType.BEEHIVE;
                default -> {
                    LOGGER.warn("Received unknown block entity type: " + type);
                    yield BlockEntityType.MOB_SPAWNER;
                }
            };
            buf.pendingRead(VarInt.class, new VarInt(Registry.BLOCK_ENTITY_TYPE.getRawId(actualType)));
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerOutboundTranslator(ClientSettingsC2SPacket.class, buf -> {
            buf.passthroughWrite(String.class); // language
            buf.passthroughWrite(Byte.class); // view distance
            buf.passthroughWrite(ChatVisibility.class); // chat visibility
            buf.passthroughWrite(Boolean.class); // chat colors
            buf.passthroughWrite(Byte.class); // player model bitmask
            buf.passthroughWrite(Arm.class); // main arm
            buf.passthroughWrite(Boolean.class); // filter text
            buf.skipWrite(Boolean.class); // allows listing
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, SimulationDistanceS2CPacket.class);
        return packets;
    }

    @Override
    @ThreadSafe(withGameThread = false)
    public void mutateDynamicRegistries(DynamicRegistryManager.Impl registries) {
        super.mutateDynamicRegistries(registries);
        mutateBiomeRegistry(DefaultDynamicRegistries.getInstance(Registry.BIOME_KEY));
    }

    private void mutateBiomeRegistry(DefaultDynamicRegistries<Biome> registry) {
        registry.add(Biomes_1_17_1.MOUNTAINS, BiomeKeys.WINDSWEPT_HILLS);
        registry.add(Biomes_1_17_1.SNOWY_TUNDRA, BiomeKeys.SNOWY_PLAINS);
        registry.add(Biomes_1_17_1.JUNGLE_EDGE, BiomeKeys.SPARSE_JUNGLE);
        registry.add(Biomes_1_17_1.STONE_SHORE, BiomeKeys.STONY_SHORE);
        registry.add(Biomes_1_17_1.GIANT_TREE_TAIGA, BiomeKeys.OLD_GROWTH_PINE_TAIGA);
        registry.add(Biomes_1_17_1.WOODED_MOUNTAINS, BiomeKeys.WINDSWEPT_FOREST);
        registry.add(Biomes_1_17_1.WOODED_BADLANDS_PLATEAU, BiomeKeys.WOODED_BADLANDS);
        registry.add(Biomes_1_17_1.GRAVELLY_MOUNTAINS, BiomeKeys.WINDSWEPT_GRAVELLY_HILLS);
        registry.add(Biomes_1_17_1.TALL_BIRCH_FOREST, BiomeKeys.OLD_GROWTH_BIRCH_FOREST);
        registry.add(Biomes_1_17_1.GIANT_SPRUCE_TAIGA, BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
        registry.add(Biomes_1_17_1.SHATTERED_SAVANNA, BiomeKeys.WINDSWEPT_SAVANNA);
    }

    @Override
    @ThreadSafe(withGameThread = false)
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_17_1, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_17_1, Registry.PARTICLE_TYPE, Particles_1_17_1::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_17_1, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.MUSIC_DISC_OTHERSIDE);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS);
        registry.unregister(SoundEvents.ITEM_BUNDLE_INSERT);
        registry.unregister(SoundEvents.ITEM_BUNDLE_REMOVE_ONE);
        registry.unregister(SoundEvents.BLOCK_GROWING_PLANT_CROP);
        registry.unregister(SoundEvents.MUSIC_DISC_OTHERSIDE);
        registry.unregister(SoundEvents.MUSIC_NETHER_CRIMSON_FOREST);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_DRIPSTONE_CAVES);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_GROVE);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_JAGGED_PEAKS);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_LUSH_CAVES);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_MEADOW);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_FROZEN_PEAKS);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_SNOWY_SLOPES);
        registry.unregister(SoundEvents.MUSIC_OVERWORLD_STONY_PEAKS);
        insertAfter(registry, SoundEvents.MUSIC_NETHER_SOUL_SAND_VALLEY, SoundEvents.MUSIC_NETHER_CRIMSON_FOREST, "music.nether.crimson_forest");
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.ANIMALS_SPAWNABLE_ON, Blocks.GRASS_BLOCK);
        tags.add(BlockTags.AXOLOTLS_SPAWNABLE_ON, Blocks.CLAY);
        tags.add(BlockTags.TERRACOTTA, Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA,
                Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA,
                Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA,
                Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA,
                Blocks.BLACK_TERRACOTTA);
        tags.addTag(BlockTags.AZALEA_GROWS_ON, BlockTags.DIRT);
        tags.addTag(BlockTags.AZALEA_GROWS_ON, BlockTags.SAND);
        tags.addTag(BlockTags.AZALEA_GROWS_ON, BlockTags.TERRACOTTA);
        tags.add(BlockTags.AZALEA_GROWS_ON, Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW);
        tags.addTag(BlockTags.AZALEA_ROOT_REPLACEABLE, BlockTags.LUSH_GROUND_REPLACEABLE);
        tags.addTag(BlockTags.AZALEA_ROOT_REPLACEABLE, BlockTags.TERRACOTTA);
        tags.add(BlockTags.AZALEA_ROOT_REPLACEABLE, Blocks.RED_SAND);
        tags.addTag(BlockTags.BIG_DRIPLEAF_PLACEABLE, BlockTags.SMALL_DRIPLEAF_PLACEABLE);
        tags.addTag(BlockTags.BIG_DRIPLEAF_PLACEABLE, BlockTags.DIRT);
        tags.add(BlockTags.BIG_DRIPLEAF_PLACEABLE, Blocks.FARMLAND);
        tags.add(BlockTags.FOXES_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.PODZOL,
                Blocks.COARSE_DIRT);
        tags.add(BlockTags.GOATS_SPAWNABLE_ON, Blocks.STONE, Blocks.SNOW, Blocks.POWDER_SNOW, Blocks.SNOW_BLOCK,
                Blocks.PACKED_ICE, Blocks.GRAVEL);
        tags.add(BlockTags.MOOSHROOMS_SPAWNABLE_ON, Blocks.MYCELIUM);
        tags.add(BlockTags.PARROTS_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.AIR);
        tags.addTag(BlockTags.PARROTS_SPAWNABLE_ON, BlockTags.LEAVES);
        tags.addTag(BlockTags.PARROTS_SPAWNABLE_ON, BlockTags.LOGS);
        tags.add(BlockTags.POLAR_BEARS_SPAWNABLE_ON_IN_FROZEN_OCEAN, Blocks.ICE);
        tags.add(BlockTags.RABBITS_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.SAND);
        tags.add(BlockTags.REPLACEABLE_PLANTS, Blocks.GRASS, Blocks.FERN, Blocks.DEAD_BUSH, Blocks.VINE,
                Blocks.GLOW_LICHEN, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH,
                Blocks.PEONY, Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.HANGING_ROOTS);
        tags.add(BlockTags.WOLVES_SPAWNABLE_ON, Blocks.GRASS_BLOCK, Blocks.SNOW, Blocks.SNOW_BLOCK);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.FEATURES_CANNOT_REPLACE);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.LEAVES);
        tags.addTag(BlockTags.LAVA_POOL_STONE_CANNOT_REPLACE, BlockTags.LOGS);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.DIRT, BlockTags.DIRT);
        copyBlocks(tags, blockTags, ItemTags.TERRACOTTA, BlockTags.TERRACOTTA);
        super.addExtraItemTags(tags, blockTags);
    }
}
