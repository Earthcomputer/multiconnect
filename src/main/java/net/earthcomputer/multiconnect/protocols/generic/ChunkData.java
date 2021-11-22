package net.earthcomputer.multiconnect.protocols.generic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.earthcomputer.multiconnect.protocols.v1_17_1.Protocol_1_17_1;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

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
        BitSet verticalStripBitmask = userData.get(Protocol_1_17_1.VERTICAL_STRIP_BITMASK);
        Registry<Biome> biomeRegistry = ChunkDataTranslator.current().getRegistryManager().get(Registry.BIOME_KEY);

        // treat unknown state ids as air (ViaBackwards sometimes sends these)
        ((IIdList<BlockState>) Block.STATE_IDS).multiconnect_setDefaultValue(Blocks.AIR.getDefaultState());
        try {
            for (int sectionY = 0; sectionY < data.sections.length; sectionY++) {
                if (verticalStripBitmask == null || verticalStripBitmask.get(sectionY)) {
                    ChunkSection section = new ChunkSection(sectionY, biomeRegistry);
                    section.fromPacket(buf);
                    data.sections[sectionY] = section;
                }
            }
        } finally {
            ((IIdList<BlockState>) Block.STATE_IDS).multiconnect_setDefaultValue(null);
        }

        return data;
    }

    public byte[] toByteArray() {
        Registry<Biome> biomeRegistry = ChunkDataTranslator.current().getRegistryManager().get(Registry.BIOME_KEY);

        ChunkSection[] sections = this.sections.clone();
        Set<Biome> biomes = new HashSet<>();
        for (int ourSectionY = 0; ourSectionY < sections.length; ourSectionY++) {
            if (sections[ourSectionY] == null) {
                ChunkSection section = new ChunkSection(ourSectionY, biomeRegistry);
                int max = Math.max(ourSectionY, sections.length - 1 - ourSectionY);
                for (int copyFromSectionY = 1; copyFromSectionY < max; copyFromSectionY = copyFromSectionY > 0 ? -copyFromSectionY : -copyFromSectionY + 1) {
                    if (ourSectionY + copyFromSectionY >= 0 && ourSectionY + copyFromSectionY < sections.length && this.sections[ourSectionY + copyFromSectionY] != null) {
                        // copy biomes from that section
                        biomes.clear();
                        int copyFromY = copyFromSectionY < ourSectionY ? 3 : 0;
                        for (int z = 0; z < 4; z++) {
                            for (int x = 0; x < 4; x++) {
                                for (int y = 0; y < 4; y++) {
                                    Biome biome = this.sections[ourSectionY + copyFromSectionY].getBiomeContainer().get(x, copyFromY, z);
                                    if (biomes.add(biome)) {
                                        // check if biomes.size() was *previously* a power of 2 (or 0)
                                        if (((biomes.size() - 1) & (biomes.size() - 2)) == 0) {
                                            section.getBiomeContainer().onResize(MathHelper.ceilLog2(biomes.size()), biome);
                                        }
                                    }
                                    section.getBiomeContainer().set(x, y, z, biome);
                                }
                            }
                        }
                        break;
                    }
                }
                sections[ourSectionY] = section;
            }
        }

        int size = 0;
        for (ChunkSection section : sections) {
            size += section.getPacketSize();
        }
        byte[] buffer = new byte[size];
        ByteBuf rawBuf = Unpooled.wrappedBuffer(buffer);
        rawBuf.writerIndex(0);
        PacketByteBuf buf = new PacketByteBuf(rawBuf);

        for (ChunkSection section : sections) {
            section.toPacket(buf);
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
    public static void skipPalettedContainer(PacketByteBuf buf, boolean biomes, boolean allowSingletonPalette) {
        int paletteSize = skipPalette(buf, allowSingletonPalette);
        int elementBits;
        if (paletteSize == 0 && allowSingletonPalette) {
            elementBits = 1;
        } else {
            // TODO: this is a fundamental flaw with how multiconnect works atm, holy shit get that rewrite done
            if (biomes) {
                elementBits = paletteSize <= 2 ? paletteSize : MathHelper.ceilLog2(ChunkDataTranslator.current().getRegistryManager().get(Registry.BIOME_KEY).size());
            } else {
                elementBits = paletteSize <= 8 ? Math.max(4, paletteSize) : MathHelper.ceilLog2(Block.STATE_IDS.size());
            }
        }
        int elementsPerLong = 64 / elementBits;
        int arraySize = ((biomes ? 64 : 4096) + elementsPerLong - 1) / elementsPerLong;
        buf.readLongArray(new long[arraySize]); // data
    }

    @ThreadSafe(withGameThread = false)
    public static int skipPalette(PacketByteBuf buf, boolean allowSingletonPalette) {
        int paletteSize = buf.readByte();
        if (paletteSize <= 8) {
            if (paletteSize == 0 && allowSingletonPalette) {
                buf.readVarInt(); // singleton id
            } else {
                // array and bimap palette data look the same enough to use the same code here
                int size = buf.readVarInt();
                for (int i = 0; i < size; i++)
                    buf.readVarInt(); // state id
            }
        }
        return paletteSize;
    }

}
