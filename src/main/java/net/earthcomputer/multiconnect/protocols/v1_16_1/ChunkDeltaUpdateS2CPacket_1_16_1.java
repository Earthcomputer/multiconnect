package net.earthcomputer.multiconnect.protocols.v1_16_1;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.earthcomputer.multiconnect.transformer.VarLong;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

public class ChunkDeltaUpdateS2CPacket_1_16_1 implements Packet<ClientPlayPacketListener> {
    private final ChunkPos pos;
    private final int updatedSectionBitmask;
    private final short[] indices;
    private final int[] stateIds;

    public ChunkDeltaUpdateS2CPacket_1_16_1(PacketByteBuf buf) {
        pos = new ChunkPos(buf.readInt(), buf.readInt());

        int numUpdates = buf.readVarInt();
        indices = new short[numUpdates];
        stateIds = new int[numUpdates];
        int updatedSectionBitmask = 0;
        for (int i = 0; i < numUpdates; i++) {
            short index = buf.readShort();
            int sectionY = (index & 255) >> 4;
            updatedSectionBitmask |= 1 << sectionY;
            indices[i] = index;
            stateIds[i] = buf.readVarInt();
        }
        this.updatedSectionBitmask = updatedSectionBitmask;
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        OffThreadException offThreadException = null;

        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            if ((updatedSectionBitmask & (1 << sectionY)) != 0) {
                int sectionY_f = sectionY;
                ChunkDeltaUpdateS2CPacket packet = Utils.createPacket(ChunkDeltaUpdateS2CPacket.class, ChunkDeltaUpdateS2CPacket::new, Protocols.V1_16_2, buf -> {
                    buf.pendingRead(Long.class, ((long)pos.x << 42) | ((long)pos.z << 20) | sectionY_f); // pos
                    buf.pendingRead(Boolean.class, false); // suppress light updates

                    ShortList filteredIndices = new ShortArrayList();
                    IntList filteredStateIds = new IntArrayList();
                    for (int i = 0; i < indices.length; i++) {
                        int index = indices[i];
                        int y = index & 255;
                        if (y >> 4 == sectionY_f) {
                            int x = (index >> 12) & 15;
                            int z = (index >> 8) & 15;
                            filteredIndices.add(ChunkSectionPos.packLocal(blockPos.set(x, y & 15, z)));
                            filteredStateIds.add(stateIds[i]);
                        }
                    }

                    buf.pendingRead(VarInt.class, new VarInt(filteredIndices.size()));
                    for (int i = 0; i < filteredIndices.size(); i++) {
                        buf.pendingRead(VarLong.class, new VarLong(((long)filteredStateIds.getInt(i) << 12) | filteredIndices.getShort(i)));
                    }

                    buf.applyPendingReads();
                });
                try {
                    listener.onChunkDeltaUpdate(packet);
                } catch (OffThreadException e) {
                    offThreadException = e;
                }
            }
        }

        if (offThreadException != null) {
            throw offThreadException;
        }
    }
}
