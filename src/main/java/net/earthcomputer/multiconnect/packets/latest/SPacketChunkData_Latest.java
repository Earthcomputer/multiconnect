package net.earthcomputer.multiconnect.packets.latest;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkData;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_17_1.Protocol_1_17_1;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_18)
public class SPacketChunkData_Latest implements SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    @Introduce(compute = "computeInnerData")
    public InnerData innerData;
    @Introduce(compute = "computeLightData")
    public LightData lightData;

    public static InnerData computeInnerData(
            @Argument("verticalStripBitmask") BitSet verticalStripBitmask,
            @Argument("heightmaps") NbtCompound heightmaps,
            @Argument("data") ChunkData data,
            @Argument("biomes") IntList biomes,
            @Argument("blockEntities") List<NbtCompound> blockEntities,
            @DefaultConstruct InnerData dest,
            @GlobalData DynamicRegistryManager registryManager,
            @GlobalData DimensionTypeReference dimensionType
    ) {
        dest.heightmaps = heightmaps;

        // translate sections
        List<ChunkData.Section> fromSections = ((ChunkData_1_17_1) data).sections;
        List<ChunkData.Section> destSections = new ArrayList<>(fromSections.size());
        ((ChunkData_Latest) dest.data).sections = destSections;
        int numSections = dimensionType.value().value().getHeight() >> 4;
        int i = 0;
        for (int sectionY = 0; sectionY < numSections; sectionY++) {
            if (verticalStripBitmask.get(sectionY)) {
                var fromSection = (ChunkData_1_17_1.ChunkSection) fromSections.get(i++);
                var toSection = new ChunkData_Latest.ChunkSection();
                toSection.nonEmptyBlockCount = fromSection.nonEmptyBlockCount;

                // convert block states
                ChunkData_1_17_1.BlockStatePalettedContainer fromBlockStates = (ChunkData_1_17_1.BlockStatePalettedContainer) fromSection.blockStates;
                if (fromBlockStates instanceof ChunkData_1_17_1.BlockStatePalettedContainer.Multiple multiple) {
                    if (multiple.palette.length == 1) {
                        var states = new ChunkData_Latest.BlockStatePalettedContainer.Singleton();
                        states.paletteSize = 0;
                        states.blockStateId = multiple.palette[0];
                        states.dummyData = new long[0];
                        toSection.blockStates = states;
                    } else {
                        var states = new ChunkData_Latest.BlockStatePalettedContainer.Multiple();
                        states.paletteSize = multiple.paletteSize;
                        states.palette = multiple.palette;
                        states.data = multiple.data;
                        toSection.blockStates = states;
                    }
                } else if (fromBlockStates instanceof ChunkData_1_17_1.BlockStatePalettedContainer.RegistryContainer registry) {
                    var states = new ChunkData_Latest.BlockStatePalettedContainer.RegistryContainer();
                    states.paletteSize = registry.paletteSize;
                    states.data = registry.data;
                    toSection.blockStates = states;
                } else {
                    throw new IllegalStateException("Illegal subtype of BlockStatePalettedContainer");
                }

                computeBiomeData(sectionY, registryManager, biomes, toSection);
                destSections.add(toSection);
            } else {
                var toSection = new ChunkData_Latest.ChunkSection();
                var toStates = new ChunkData_Latest.BlockStatePalettedContainer.Singleton();
                toStates.dummyData = new long[0];
                toSection.blockStates = toStates;
                computeBiomeData(sectionY, registryManager, biomes, toSection);
                destSections.add(toSection);
            }
        }

        // translate block entities
        dest.blockEntities = new ArrayList<>(blockEntities.size());
        for (NbtCompound be : blockEntities) {
            BlockEntityData destBe = new BlockEntityData();
            destBe.localXz = (byte) ((ChunkSectionPos.getLocalCoord(be.getInt("x")) << 4) | ChunkSectionPos.getLocalCoord(be.getInt("z")));
            destBe.y = (short) be.getInt("y");
            Identifier id = Identifier.tryParse(be.getString("id"));
            if (id != null) {
                Integer rawId = PacketSystem.serverIdToRawId(net.minecraft.util.registry.Registry.BLOCK_ENTITY_TYPE, id);
                if (rawId != null) {
                    destBe.type = rawId;
                }
            }
            destBe.nbt = be;
            dest.blockEntities.add(destBe);
        }

        return dest;
    }

    private static void computeBiomeData(
            int sectionY,
            DynamicRegistryManager registryManager,
            IntList biomes,
            ChunkData_Latest.ChunkSection toSection
    ) {
        var biomeRegistry = registryManager.get(net.minecraft.util.registry.Registry.BIOME_KEY);
        Int2IntMap invBiomePalette = new Int2IntOpenHashMap();
        IntList biomePalette = new IntArrayList();
        for (int i = 0; i < biomes.size(); i++) {
            int biome = Protocol_1_17_1.mapBiomeId(biomes.getInt(i), biomeRegistry);
            invBiomePalette.computeIfAbsent(biome, k -> {
                biomePalette.add(biome);
                return invBiomePalette.size();
            });
        }
        if (biomePalette.isEmpty()) {
            biomePalette.add(0);
            invBiomePalette.put(0, 0);
        }

        int bitsPerBiome = MathHelper.ceilLog2(biomePalette.size());
        if (bitsPerBiome == 0) {
            var toBiomes = new ChunkData_Latest.BiomePalettedContainer.Singleton();
            toBiomes.dummyData = new long[0];
            toSection.biomes = toBiomes;
            toBiomes.biomeId = biomePalette.getInt(0);
            return;
        }

        int minIndex = sectionY << 6;
        int biomesPerLong = 64 / bitsPerBiome;
        long[] result = new long[(64 + biomesPerLong - 1) / biomesPerLong];

        if (bitsPerBiome <= 3) {
            var toBiomes = new ChunkData_Latest.BiomePalettedContainer.Multiple();
            toBiomes.paletteSize = (byte) bitsPerBiome;
            toBiomes.palette = biomePalette.toIntArray();
            toBiomes.data = result;
            toSection.biomes = toBiomes;
        } else {
            var toBiomes = new ChunkData_Latest.BiomePalettedContainer.RegistryContainer();
            toBiomes.paletteSize = (byte) bitsPerBiome;
            toBiomes.data = result;
            toSection.biomes = toBiomes;
            bitsPerBiome = MathHelper.ceilLog2(biomeRegistry.size());
        }

        if (sectionY < 0) {
            return;
        }
        long currentLong = 0;
        for (int i = 0; i < 64; i++) {
            int valueToWrite = bitsPerBiome <= 2
                    ? invBiomePalette.get(Protocol_1_17_1.mapBiomeId(biomes.getInt(minIndex + i), biomeRegistry))
                    : Protocol_1_17_1.mapBiomeId(biomes.getInt(minIndex + i), biomeRegistry);
            int posInLong = i % biomesPerLong;
            currentLong |= (long) valueToWrite << (posInLong * bitsPerBiome);
            if (posInLong + bitsPerBiome >= 64 || i == 63) {
                result[i / biomesPerLong] = currentLong;
                currentLong = 0;
            }
        }
    }

    public static LightData computeLightData(
            @Argument("verticalStripBitmask") BitSet verticalStripBitmask,
            @FilledArgument TypedMap userData,
            @DefaultConstruct LightData lightData
    ) {
        byte[][] blockLight = userData.get(Protocol_1_13_2.BLOCK_LIGHT_KEY);
        if (blockLight != null) {
            BitSet blockLightMask = (BitSet) verticalStripBitmask.clone();
            Utils.leftShift(blockLightMask, 1);

            lightData.blockLightMask = blockLightMask;
            for (byte[] section : blockLight) {
                if (section != null) {
                    lightData.blockLightData.add(section);
                }
            }

            byte[][] skyLight = userData.get(Protocol_1_13_2.SKY_LIGHT_KEY);
            for (int i = 0; i < blockLight.length; i++) {
                if (blockLightMask.get(i)) {
                    if (skyLight != null && i < skyLight.length && skyLight[i] != null) {
                        lightData.skyLightData.add(skyLight[i]);
                    } else {
                        lightData.skyLightData.add(new byte[2048]);
                    }
                }
            }
        }
        return lightData;
    }

    @MessageVariant
    public static class InnerData {
        public NbtCompound heightmaps;
        @Length(raw = true)
        public ChunkData data;
        public List<BlockEntityData> blockEntities;
    }

    @MessageVariant
    public static class BlockEntityData {
        public byte localXz;
        public short y;
        @Registry(Registries.BLOCK_ENTITY_TYPE)
        public int type;
        @Datafix(value = DatafixTypes.BLOCK_ENTITY, preprocess = "preprocessBlockEntity")
        public NbtCompound nbt;

        public static void preprocessBlockEntity(
                NbtCompound nbt,
                @Argument("type") int type
        ) {
            if (nbt == null) {
                return;
            }
            Identifier name = PacketSystem.serverRawIdToId(net.minecraft.util.registry.Registry.BLOCK_ENTITY_TYPE, type);
            if (name != null) {
                nbt.putString("id", name.toString());
            }
        }
    }

    @MessageVariant
    public static class LightData {
        @DefaultConstruct(booleanValue = true)
        public boolean trustEdges;
        public BitSet skyLightMask;
        public BitSet blockLightMask;
        public BitSet filledSkyLightMask;
        public BitSet filledBlockLightMask;
        public List<byte[]> skyLightData;
        public List<byte[]> blockLightData;
    }
}
