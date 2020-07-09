package net.earthcomputer.multiconnect.protocols.v1_16_1;

import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.biome.source.BiomeArray;

import java.util.ArrayList;

public class ChunkData_1_16_1 extends ChunkData {
    @Override
    public void read(PacketByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        isFullChunk = buf.readBoolean();
        forgetOldData = buf.readBoolean();
        int verticalStripBitmask = buf.readVarInt();
        heightmaps = buf.readCompoundTag();

        if (isFullChunk) {
            biomeArray = new int[BiomeArray.DEFAULT_LENGTH];
            for (int i = 0; i < biomeArray.length; i++) {
                biomeArray[i] = buf.readInt();
            }
        }

        PacketByteBuf dataBuf = readData(buf);
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                Section section = new Section();
                section.nonEmptyBlockCount = dataBuf.readShort();
                section.blocks = readNewBlockArray(dataBuf);
            }
        }

        int blockEntityCount = buf.readVarInt();
        blockEntities = new ArrayList<>(blockEntityCount);
        for (int i = 0; i < blockEntityCount; i++) {
            blockEntities.add(buf.readCompoundTag());
        }
    }
}
