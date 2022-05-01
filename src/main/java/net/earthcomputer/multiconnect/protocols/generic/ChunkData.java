package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;

import java.util.BitSet;

public final class ChunkData implements IBlockConnectionsBlockView, IUserDataHolder {
    private final ChunkSection[] sections;
    private final int minY;
    private final int maxY;
    private final TypedMap userData;

    private ChunkData(ChunkSection[] sections, int minY, int maxY, TypedMap userData) {
        this.sections = sections;
        this.minY = minY;
        this.maxY = maxY;
        this.userData = userData;
    }

    @SuppressWarnings("unchecked")
    @ThreadSafe(withGameThread = false)
    public static ChunkData read(int minY, int maxY, TypedMap userData, PacketByteBuf buf) {
        ChunkData data = new ChunkData(new ChunkSection[(maxY + 1 - minY + 15) >> 4], minY, maxY, userData);
        // TODO: rewrite
//        ChunkDataS2CPacket packet = ChunkDataTranslator.current().getPacket();
//        BitSet verticalStripBitmask = packet.getVerticalStripBitmask();
//
//        // treat unknown state ids as air (ViaBackwards sometimes sends these)
//        ((IIdList<BlockState>) Block.STATE_IDS).multiconnect_setDefaultValue(Blocks.AIR.getDefaultState());
//        try {
//            for (int sectionY = 0; sectionY < data.sections.length; sectionY++) {
//                if (verticalStripBitmask.get(sectionY)) {
//                    ChunkSection section = new ChunkSection(sectionY);
//                    section.fromPacket(buf);
//                    data.sections[sectionY] = section;
//                }
//            }
//        } finally {
//            ((IIdList<BlockState>) Block.STATE_IDS).multiconnect_setDefaultValue(null);
//        }

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
        if (y < minY || y > maxY) {
            return Blocks.AIR.getDefaultState();
        }
        x &= 15;
        y += minY;
        z &= 15;
        ChunkSection section = sections[y >> 4];
        if (section == null) {
            return Blocks.AIR.getDefaultState();
        }
        y &= 15;
        return section.getBlockState(x, y, z);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        if (pos.getY() < minY || pos.getY() > maxY) {
            return;
        }
        int x = pos.getX() & 15;
        int y = pos.getY() + minY;
        int z = pos.getZ() & 15;
        ChunkSection section = sections[y >> 4];
        if (section == null) {
            return;
        }
        y &= 15;
        section.setBlockState(x, y, z, state, false);
    }

    public ChunkSection[] getSections() {
        return sections;
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return maxY;
    }

    @Override
    public TypedMap multiconnect_getUserData() {
        return userData;
    }

    @ThreadSafe(withGameThread = false)
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
