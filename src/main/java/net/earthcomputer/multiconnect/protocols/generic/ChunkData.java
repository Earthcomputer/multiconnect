package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;

public final class ChunkData implements IBlockConnectionsBlockView {
    private final ChunkSection[] sections = new ChunkSection[16];

    public static ChunkData read(PacketByteBuf buf) {
        ChunkData data = new ChunkData();
        ChunkDataS2CPacket packet = ChunkDataTranslator.current().getPacket();
        int verticalStripBitmask = packet.getVerticalStripBitmask();

        for (int sectionY = 0; sectionY < 16; sectionY++) {
            if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                ChunkSection section = new ChunkSection(sectionY);
                section.fromPacket(buf);
                data.sections[sectionY] = section;
            }
        }

        return data;
    }

    public byte[] toByteArray() {
        int size = 0;
        for (ChunkSection section : sections) {
            if (section != null) {
                size += section.getPacketSize();
            }
        }
        byte[] buffer = new byte[size];
        ByteBuf rawBuf = Unpooled.wrappedBuffer(buffer);
        rawBuf.writerIndex(0);
        PacketByteBuf buf = new PacketByteBuf(rawBuf);

        for (ChunkSection section : sections) {
            if (section != null) {
                section.toPacket(buf);
            }
        }

        return buffer;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (y < 0 || y > 255) {
            return Blocks.AIR.getDefaultState();
        }
        x &= 15;
        z &= 15;
        ChunkSection section = sections[y >> 4];
        if (section == null) {
            return Blocks.AIR.getDefaultState();
        }
        y &= 15;
        return section.getBlockState(x, y, z);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state) {
        if (pos.getY() < 0 || pos.getY() > 255) {
            return false;
        }
        int x = pos.getX() & 15;
        int z = pos.getZ() & 15;
        ChunkSection section = sections[pos.getY() >> 4];
        if (section == null) {
            return false;
        }
        int y = pos.getY() & 15;
        return section.setBlockState(x, y, z, state, false) != state;
    }

    public ChunkSection[] getSections() {
        return sections;
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

}
