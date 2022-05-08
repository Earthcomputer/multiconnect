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
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketChunkData;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1;
import net.earthcomputer.multiconnect.protocols.generic.DimensionTypeReference;
import net.earthcomputer.multiconnect.protocols.v1_17_1.Protocol_1_17_1;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

@MessageVariant(minVersion = Protocols.V1_18)
public class SPacketChunkData_Latest implements SPacketChunkData {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    @Introduce(compute = "computeInnerData")
    public InnerData innerData;
    @Introduce(defaultConstruct = true)
    public LightData lightData;

    public static InnerData computeInnerData(
            @Argument("verticalStripBitmask") BitSet verticalStripBitmask,
            @Argument("heightmaps") NbtCompound heightmaps,
            @Argument("data") ChunkData data,
            @Argument("biomes") IntList biomes,
            @Argument("blockEntities") List<NbtCompound> blockEntities,
            @FilledArgument(registry = Registries.BLOCK_ENTITY_TYPE) ToIntFunction<Identifier> blockEntityNameToId,
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
                toSection.palette = fromSection.palette;
                toSection.data = fromSection.data;
                computeBiomeData(sectionY, registryManager, biomes, toSection);
                destSections.add(toSection);
            } else {
                var toSection = new ChunkData_Latest.ChunkSection();
                toSection.palette = new ChunkData_Latest.Palette();
                toSection.palette.paletteSize = 4;
                toSection.palette.palette = new int[] {0};
                toSection.data = new long[256];
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
                destBe.type = blockEntityNameToId.applyAsInt(id);
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
            var palette = new ChunkData_Latest.BiomePalette.Singleton();
            palette.biomeId = biomePalette.getInt(0);
            toSection.biomePalette = palette;
            bitsPerBiome = 1;
        } else if (bitsPerBiome <= 2) {
            var palette = new ChunkData_Latest.BiomePalette.Multiple();
            palette.paletteSize = (byte) bitsPerBiome;
            palette.palette = biomePalette.toIntArray();
            toSection.biomePalette = palette;
        } else {
            var palette = new ChunkData_Latest.BiomePalette.Registry();
            palette.paletteSize = (byte) bitsPerBiome;
            toSection.biomePalette = palette;
            bitsPerBiome = MathHelper.ceilLog2(biomeRegistry.size());
        }

        int minIndex = sectionY << 6;

        int biomesPerLong = 64 / bitsPerBiome;
        long[] result = new long[(64 + biomesPerLong - 1) / biomesPerLong];
        toSection.biomeData = result;
        if (sectionY < 0) {
            return;
        }
        long currentLong = 0;
        for (int i = 0; i < 64; i++) {
            int valueToWrite = bitsPerBiome <= 2 ? invBiomePalette.get(biomes.getInt(minIndex + i)) : Protocol_1_17_1.mapBiomeId(biomes.getInt(minIndex + i), biomeRegistry);
            int posInLong = i % biomesPerLong;
            currentLong |= (long) valueToWrite << (posInLong * bitsPerBiome);
            if (posInLong + bitsPerBiome >= 64 || i == 63) {
                result[i / biomesPerLong] = currentLong;
                currentLong = 0;
            }
        }
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
                @Argument("type") int type,
                @FilledArgument(registry = Registries.BLOCK_ENTITY_TYPE) IntFunction<Identifier> blockEntityIdToName
        ) {
            if (nbt == null) {
                return;
            }
            Identifier name = blockEntityIdToName.apply(type);
            if (name != null) {
                nbt.putString("id",name.toString());
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
