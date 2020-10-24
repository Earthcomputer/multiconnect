package net.earthcomputer.multiconnect.protocols.v1_8;

import net.earthcomputer.multiconnect.protocols.generic.IChunkDataS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_8.mixin.ChunkDataS2CAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.biome.source.BiomeArray;

import java.util.ArrayList;

public class BulkChunkDataS2CPacket_1_8 implements Packet<ClientPlayPacketListener> {
    private Entry[] entries;

    @Override
    public void read(PacketByteBuf buf) {
        boolean hasSkyLight = buf.readBoolean();
        int numChunks = buf.readVarInt();
        entries = new Entry[numChunks];
        for (int i = 0; i < numChunks; i++) {
            Entry entry = entries[i] = new Entry();
            entry.x = buf.readInt();
            entry.z = buf.readInt();
            entry.verticalStripBitmask = buf.readShort();
            entry.data = new byte[calcDataSize(Integer.bitCount(entry.verticalStripBitmask), hasSkyLight)];
        }
        for (int i = 0; i < numChunks; i++) {
            buf.readBytes(entries[i].data);
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        for (Entry entry : entries) {
            ChunkDataS2CPacket packet = new ChunkDataS2CPacket();
            //noinspection ConstantConditions
            ChunkDataS2CAccessor accessor = (ChunkDataS2CAccessor) packet;
            //noinspection ConstantConditions
            IChunkDataS2CPacket iPacket = (IChunkDataS2CPacket) packet;

            accessor.setChunkX(entry.x);
            accessor.setChunkZ(entry.z);
            accessor.setIsFullChunk(true);
            accessor.setVerticalStripBitmask(entry.verticalStripBitmask);
            accessor.setBiomeArray(new int[BiomeArray.DEFAULT_LENGTH]);
            accessor.setHeightmaps(new CompoundTag());
            accessor.setBlockEntities(new ArrayList<>(0));
            iPacket.setData(entry.data);

            try {
                listener.onChunkData(packet);
            } catch (OffThreadException ignore) {
            }
        }
    }

    private static int calcDataSize(int numSubchunks, boolean hasSkyLight) {
        int blocksSize = numSubchunks * 2 * 16 * 16 * 16;
        int blockLightSize = numSubchunks * 16 * 16 * 16 / 2;
        int skyLightSize = hasSkyLight ? numSubchunks * 16 * 16 * 16 / 2 : 0;
        int biomeSize = 256;
        return blocksSize + blockLightSize + skyLightSize + biomeSize;
    }

    private static class Entry {
        public int x;
        public int z;
        public int verticalStripBitmask;
        public byte[] data;
    }
}
