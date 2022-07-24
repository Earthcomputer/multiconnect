package net.earthcomputer.multiconnect.packets.v1_16_5;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Datafix;
import net.earthcomputer.multiconnect.ap.DatafixTypes;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.Length;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.ReturnType;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.SPacketLevelChunkWithLight;
import net.earthcomputer.multiconnect.packets.SPacketSectionBlocksUpdate;
import net.earthcomputer.multiconnect.packets.v1_17_1.ChunkData_1_17_1;
import net.earthcomputer.multiconnect.packets.v1_17_1.SPacketLevelChunkWithLight_1_17_1;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.SimpleBitStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_16_2, maxVersion = Protocols.V1_16_5)
public class SPacketLevelChunkWithLight_1_16_5 implements SPacketLevelChunkWithLight {
    @Type(Types.INT)
    public int x;
    @Type(Types.INT)
    public int z;
    public boolean fullChunk;
    public int verticalStripBitmask;
    public CompoundTag heightmaps;
    @OnlyIf("hasFullChunk")
    public IntList biomes;
    @Length(raw = true)
    public ChunkData data;
    @Datafix(DatafixTypes.BLOCK_ENTITY)
    public List<CompoundTag> blockEntities;

    public static boolean hasFullChunk(@Argument("fullChunk") boolean fullChunk) {
        return fullChunk;
    }

    @ReturnType(SPacketLevelChunkWithLight_1_17_1.class)
    @ReturnType(SPacketSectionBlocksUpdate.class)
    @Handler
    public static List<Object> handle(
        @Argument(value = "this", translate = true) SPacketLevelChunkWithLight_1_17_1 translatedThis,
        @Argument("fullChunk") boolean fullChunk,
        @Argument("verticalStripBitmask") int verticalStripBitmask
    ) {
        List<Object> packets;
        if (fullChunk) {
            packets = new ArrayList<>(1);
            packets.add(translatedThis);
        } else {
            packets = new ArrayList<>(Integer.bitCount(verticalStripBitmask & 0xffff));
            ChunkData_1_17_1 data = (ChunkData_1_17_1) translatedThis.data;
            int sectionIndex = 0;

            for (int sectionY = 0; sectionY < 16; sectionY++) {
                if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                    var packet = new SPacketSectionBlocksUpdate();
                    packet.sectionPos = ((long) translatedThis.x << 42)
                        | ((long) (translatedThis.z & ((1 << 22) - 1)) << 20)
                        | sectionY;

                    var section = (ChunkData_1_17_1.ChunkSection) data.sections.get(sectionIndex++);
                    var blockStates = (ChunkData_1_17_1.BlockStatePalettedContainer) section.blockStates;

                    int paletteSize;
                    int[] palette;
                    long[] blockData;
                    if (blockStates instanceof ChunkData_1_17_1.BlockStatePalettedContainer.Multiple multiple) {
                        palette = multiple.palette;
                        paletteSize = Math.max(4, multiple.paletteSize);
                        blockData = multiple.data;
                    } else {
                        palette = null;
                        paletteSize = PacketSystem.getServerBlockStateRegistryBits();
                        blockData = ((ChunkData_1_17_1.BlockStatePalettedContainer.RegistryContainer) blockStates).data;
                    }

                    int expectedSize = Utils.getExpectedPackedIntegerArraySize(paletteSize, 4096);
                    if (blockData.length != expectedSize) {
                        blockData = Arrays.copyOf(blockData, expectedSize);
                    }

                    SimpleBitStorage bitStorage = new SimpleBitStorage(paletteSize, 4096, blockData);
                    LongList blocks = new LongArrayList(4096);
                    for (int i = 0; i < 4096; i++) {
                        int blockId = bitStorage.get(i);
                        if (palette != null) {
                            blockId = palette[blockId];
                        }
                        int x = i & 15;
                        int y = (i >> 8) & 15;
                        int z = (i >> 4) & 15;
                        int sectionRelativePos = (x << 8) | (z << 4) | y;
                        blocks.add((long) blockId << 12 | sectionRelativePos);
                    }
                    packet.blocks = blocks;

                    packet.noLightUpdates = true;
                    packets.add(packet);
                }
            }
        }

        return packets;
    }
}
