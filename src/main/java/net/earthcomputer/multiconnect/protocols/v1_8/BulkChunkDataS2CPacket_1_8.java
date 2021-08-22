package net.earthcomputer.multiconnect.protocols.v1_8;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.ChunkPos;

public class BulkChunkDataS2CPacket_1_8 implements Packet<ClientPlayPacketListener> {
    private final Entry[] entries;

    public BulkChunkDataS2CPacket_1_8(PacketByteBuf buf) {
        boolean hasSkyLight = buf.readBoolean();
        int numChunks = buf.readVarInt();
        entries = new Entry[numChunks];
        for (int i = 0; i < numChunks; i++) {
            Entry entry = entries[i] = new Entry();
            entry.x = buf.readInt();
            entry.z = buf.readInt();
            entry.verticalStripBitmask = buf.readUnsignedShort();
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
            ChunkDataTranslator.asyncExecute(new ChunkPos(entry.x, entry.z), () -> {
                ChunkDataS2CPacket packet = Utils.createPacket(ChunkDataS2CPacket.class, ChunkDataS2CPacket::new, Protocols.V1_9, buf -> {
                    buf.pendingRead(Integer.class, entry.x);
                    buf.pendingRead(Integer.class, entry.z);
                    buf.pendingRead(Boolean.class, true); // full chunk
                    buf.pendingRead(VarInt.class, new VarInt(entry.verticalStripBitmask));
                    buf.pendingRead(VarInt.class, new VarInt(entry.data.length));
                    buf.pendingRead(byte[].class, entry.data);
                    buf.applyPendingReads();
                });

                try {
                    listener.onChunkData(packet);
                } catch (OffThreadException ignore) {
                }
            });
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
