package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class ChunkData {

    protected int chunkX;
    protected int chunkZ;
    protected CompoundTag heightmaps;
    protected int[] biomeArray;
    protected Section[] sections = new Section[16];
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
                section.trim();
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

    protected PacketByteBuf readData(PacketByteBuf buf) {
        int dataSize = buf.readVarInt();
        byte[] data = new byte[dataSize];
        buf.readBytes(data);
        return new PacketByteBuf(Unpooled.wrappedBuffer(data));
    }

    protected BlockArray readNewBlockArray(PacketByteBuf buf) {
        BlockArray blocks = new BlockArray();

        int paletteSize = buf.readByte();
        int valuesPerLong = 64 / paletteSize;
        int mask = (1 << valuesPerLong) - 1;

        if (paletteSize < 9) {
            int paletteCount = buf.readVarInt();
            BlockState[] palette = new BlockState[paletteCount];
            for (int i = 0; i < paletteCount; i++) {
                palette[i] = Block.STATE_IDS.get(buf.readVarInt());
            }

            buf.readVarInt(); // long array length
            for (int index = 0; index < 4096; index += valuesPerLong) {
                long val = buf.readLong();
                for (int i = 0; i < valuesPerLong && index + i < 4096; i++) {
                    int id = (int) (val & mask);
                    BlockState state = palette[id];
                    blocks.set(index & 15, index >> 8, (index >> 4) & 15, state);
                    val >>= valuesPerLong;
                }
            }
        } else {
            buf.readVarInt(); // long array length
            for (int index = 0; index < 4096; index += valuesPerLong) {
                long val = buf.readLong();
                for (int i = 0; i < valuesPerLong && index + i < 4096; i++) {
                    int id = (int) (val & mask);
                    BlockState state = Block.STATE_IDS.get(id);
                    blocks.set(index & 15, index >> 8, (index >> 4) & 15, state);
                    val >>= valuesPerLong;
                }
            }
        }

        return blocks;
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
        public BlockArray blocks;

        private void trim() {
            blocks.trim();
        }

        private int getDataSize() {
            return 2 + blocks.getDataSize();
        }

        private void write(PacketByteBuf buf) {
            buf.writeShort(nonEmptyBlockCount);
            blocks.write(buf);
        }
    }

    public static final class BlockArray {
        private BlockState[] blocks = new BlockState[4096];
        private IdentityHashMap<BlockState, ShortOpenHashSet> positions = new IdentityHashMap<>();

        private BlockState currentIteratingState;
        private ShortArrayList pendingRemovals = null;

        public BlockState get(int x, int y, int z) {
            assert isInBounds(x, y, z) : outOfBoundsError(x, y, z);
            return blocks[(y << 8) | (z << 4) | x];
        }

        public void set(int x, int y, int z, BlockState state) {
            assert isInBounds(x, y, z) : outOfBoundsError(x, y, z);
            int index = (y << 8) | (z << 4) | x;
            BlockState oldBlock = blocks[index];
            if (oldBlock == state) {
                return;
            }
            if (oldBlock != null) {
                if (pendingRemovals != null && oldBlock == currentIteratingState) {
                    pendingRemovals.add((short)index);
                } else {
                    positions.get(oldBlock).remove((short)index);
                }
            }
            blocks[index] = state;
            positions.computeIfAbsent(state, k -> new ShortOpenHashSet()).add((short)index);
        }

        /**
         * Does not support nested calls, block positions are mutable and should be converted to immutable if saved.
         * Cannot add more of this block state while iterating, can remove them though.
         */
        public void forEachPosition(BlockState state, Consumer<BlockPos> function) {
            assert pendingRemovals == null;
            ShortOpenHashSet set = positions.get(state);
            if (set != null) {
                pendingRemovals = new ShortArrayList();
                currentIteratingState = state;
                BlockPos.Mutable pos = new BlockPos.Mutable();
                ShortIterator itr = set.iterator();
                while (itr.hasNext()) {
                    short index = itr.nextShort();
                    function.accept(pos.set(index & 15, index >> 8, (index >> 4) & 15));
                }
                if (!pendingRemovals.isEmpty()) {
                    ShortIterator removalsItr = pendingRemovals.iterator();
                    pendingRemovals = null;
                    while (removalsItr.hasNext()) {
                        short index = removalsItr.nextShort();
                        set.remove(index);
                    }
                } else {
                    pendingRemovals = null;
                }
            }
        }

        private void trim() {
            positions.values().removeIf(ShortOpenHashSet::isEmpty);
        }

        private int getDataSize() {
            int paletteSize = getPaletteSize();
            int paletteSizeBytes = 0;
            if (paletteSize < 9) {
                // not id list palette
                paletteSizeBytes = PacketByteBuf.getVarIntSizeBytes(positions.size());
                for (BlockState state : positions.keySet()) {
                    paletteSizeBytes += PacketByteBuf.getVarIntSizeBytes(Block.STATE_IDS.getRawId(state));
                }
            }
            int valuesPerLong = 64 / paletteSize;
            int storageSizeBytes = getLongArrayLength(valuesPerLong) * 8;
            return 1 + paletteSizeBytes + PacketByteBuf.getVarIntSizeBytes(storageSizeBytes) + storageSizeBytes;
        }

        private void write(PacketByteBuf buf) {
            int paletteSize = getPaletteSize();
            int valuesPerLong = 64 / paletteSize;

            buf.writeByte(paletteSize);

            if (paletteSize < 9) {
                // not id list palette
                buf.writeVarInt(positions.size());
                Object2IntOpenCustomHashMap<BlockState> palette = new Object2IntOpenCustomHashMap<>(positions.size(), Util.identityHashStrategy());
                for (BlockState state : positions.keySet()) {
                    buf.writeVarInt(Block.STATE_IDS.getRawId(state));
                    palette.put(state, palette.size());
                }

                buf.writeVarInt(getLongArrayLength(valuesPerLong));
                for (int index = 0; index < 4096; index += valuesPerLong) {
                    long val = 0;
                    for (int i = 0; i < valuesPerLong && index + i < 4096; i++) {
                        int id = palette.getInt(blocks[index + i]);
                        val |= (long) id << (i * paletteSize);
                    }
                    buf.writeLong(val);
                }
            } else {
                buf.writeVarInt(getLongArrayLength(valuesPerLong));
                for (int index = 0; index < 4096; index += valuesPerLong) {
                    long val = 0;
                    for (int i = 0; i < valuesPerLong && index + i < 4096; i++) {
                        int id = Block.STATE_IDS.getRawId(blocks[index + i]);
                        val |= (long) id << (i * paletteSize);
                    }
                    buf.writeLong(val);
                }
            }
        }

        private static int getLongArrayLength(int valuesPerLong) {
            return (4096 + valuesPerLong - 1) / valuesPerLong;
        }

        private int getPaletteSize() {
            return Math.max(4, MathHelper.log2DeBruijn(positions.size()));
        }

        private static boolean isInBounds(int x, int y, int z) {
            return x >= 0 && x <= 15 && y >= 0 && y <= 15 && z >= 0 && z <= 15;
        }

        private static String outOfBoundsError(int x, int y, int z) {
            return "Out of bounds: (" + x + ", " + y + ", " + z + ")";
        }
    }

}
