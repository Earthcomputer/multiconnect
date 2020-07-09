package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.PalettedContainer;

import java.util.List;

public abstract class ChunkData {

    protected int chunkX;
    protected int chunkZ;
    protected CompoundTag heightmaps;
    protected int[] biomeArray;
    protected Section[] sections;
    protected List<CompoundTag> blockEntities;
    protected boolean isFullChunk;
    protected boolean forgetOldData;

    public abstract void read(PacketByteBuf buf);

    public void writePendingReads(TransformerByteBuf buf) {
        buf.pendingRead(Integer.class, chunkX);
        buf.pendingRead(Integer.class, chunkZ);
        buf.pendingRead(Boolean.class, isFullChunk);
        buf.pendingRead(Boolean.class, forgetOldData);

        int verticalStripBitmask = 0;
        for (int sectionY = 0; sectionY < 16; sectionY++) {
            if (sections[sectionY] != null) {
                verticalStripBitmask |= 1 << sectionY;
            }
        }
        buf.pendingRead(VarInt.class, new VarInt(verticalStripBitmask));

        buf.pendingRead(CompoundTag.class, heightmaps);
        if (isFullChunk) {
            buf.pendingRead(int[].class, biomeArray);
        }

        int dataSize = 0;
        for (Section section : sections) {
            if (section != null) {
                dataSize += section.getDataSize();
            }
        }
        buf.pendingRead(VarInt.class, new VarInt(dataSize));
        byte[] data = new byte[dataSize];
        ByteBuf rawDataBuf = Unpooled.wrappedBuffer(data);
        rawDataBuf.writerIndex(0);
        PacketByteBuf dataBuf = new PacketByteBuf(rawDataBuf);
        for (Section section : sections) {
            if (section != null) {
                section.write(dataBuf);
            }
        }
        buf.pendingRead(byte[].class, data);

        buf.pendingRead(VarInt.class, new VarInt(blockEntities.size()));
        for (CompoundTag be : blockEntities) {
            buf.pendingRead(CompoundTag.class, be);
        }
    }

    public static int skipPalette(PacketByteBuf buf) {
        int paletteSize = buf.readByte();
        if (paletteSize <= 8) {
            // array and bimap palette data look the same enough to use the same code here
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++)
                buf.readVarInt(); // state id
        }
        return paletteSize;
    }

    public static final class Section {
        public int nonEmptyBlockCount;
        public PalettedContainer<BlockState> blocks;

        private int getDataSize() {
            return 2 + blocks.getPacketSize();
        }

        private void write(PacketByteBuf buf) {
            buf.writeShort(nonEmptyBlockCount);
            blocks.toPacket(buf);
        }
    }

}
