package net.earthcomputer.multiconnect.protocols.v1_16_1;

import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.ChunkDeltaUpdateS2CAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.OffThreadException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.ArrayList;
import java.util.List;

public class ChunkDeltaUpdateS2CPacket_1_16_1 implements Packet<ClientPlayPacketListener> {
    private ChunkPos pos;
    private int updatedSectionBitmask;
    private short[] indices;
    private BlockState[] states;

    @Override
    public void read(PacketByteBuf buf) {
        pos = new ChunkPos(buf.readInt(), buf.readInt());

        int numUpdates = buf.readVarInt();
        indices = new short[numUpdates];
        states = new BlockState[numUpdates];
        updatedSectionBitmask = 0;
        for (int i = 0; i < numUpdates; i++) {
            short index = buf.readShort();
            updatedSectionBitmask |= (index & 255) >> 4;
            indices[i] = index;
            states[i] = Block.STATE_IDS.get(buf.readVarInt());
        }
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
                ChunkDeltaUpdateS2CPacket packet = new ChunkDeltaUpdateS2CPacket();
                //noinspection ConstantConditions
                ChunkDeltaUpdateS2CAccessor accessor = (ChunkDeltaUpdateS2CAccessor) packet;
                accessor.setPos(ChunkSectionPos.from(pos, sectionY));
                ShortList filteredIndices = new ShortArrayList();
                List<BlockState> filteredBlockStates = new ArrayList<>();
                for (int i = 0; i < indices.length; i++) {
                    int index = indices[i];
                    int y = index & 255;
                    if (y >> 4 == sectionY) {
                        int x = (index >> 12) & 15;
                        int z = (index >> 8) & 15;
                        filteredIndices.add(ChunkSectionPos.getPackedLocalPos(blockPos.set(x, y & 15, z)));
                        filteredBlockStates.add(states[i]);
                    }
                }
                accessor.setIndices(filteredIndices.toShortArray());
                accessor.setStates(filteredBlockStates.toArray(new BlockState[0]));
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
