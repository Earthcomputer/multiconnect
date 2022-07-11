package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.earthcomputer.multiconnect.packets.ChunkData;
import net.earthcomputer.multiconnect.packets.ChunkData.BlockStatePalettedContainer;
import net.earthcomputer.multiconnect.packets.latest.ChunkData_Latest;
import net.earthcomputer.multiconnect.packets.latest.ChunkData_Latest.BlockStatePalettedContainer.Multiple;
import net.earthcomputer.multiconnect.packets.latest.ChunkData_Latest.BlockStatePalettedContainer.RegistryContainer;
import net.earthcomputer.multiconnect.packets.latest.ChunkData_Latest.ChunkSection;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * Assumes that block state IDs have been translated by this point
 */
public class BlockConnectionsNetworkView implements IBlockConnectionsBlockView {
    private final int minY;
    private final List<ChunkData.Section> sections;
    private final SimpleBitStorage[] packedArrays;

    public BlockConnectionsNetworkView(int minY, List<ChunkData.Section> sections) {
        this.minY = minY;
        this.sections = sections;
        this.packedArrays = new SimpleBitStorage[sections.size()];
        for (int i = 0; i < sections.size(); i++) {
            var section = (ChunkData_Latest.ChunkSection) sections.get(i);
            var blockStates = section.blockStates;
            if (blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Multiple multiple) {
                packedArrays[i] = new SimpleBitStorage(multiple.paletteSize, 4096, multiple.data);
            } else if (blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.RegistryContainer registryContainer) {
                packedArrays[i] = new SimpleBitStorage(registryContainer.paletteSize, 4096, registryContainer.data);
            }
        }
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int sectionIndex = (pos.getY() - minY) >> 4;
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            return Blocks.AIR.defaultBlockState();
        }
        var section = (ChunkData_Latest.ChunkSection) sections.get(sectionIndex);
        int stateId;
        if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Singleton singleton) {
            stateId = singleton.blockStateId;
        } else if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Multiple multiple) {
            stateId = packedArrays[sectionIndex].get((((pos.getY() - minY) & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15));
            stateId = multiple.palette[stateId];
        } else {
            stateId = packedArrays[sectionIndex].get((((pos.getY() - minY) & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15));
        }
        return Block.stateById(stateId);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        int sectionIndex = (pos.getY() - minY) >> 4;
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            return;
        }
        var section = (ChunkData_Latest.ChunkSection) sections.get(sectionIndex);
        int stateId = Block.getId(state);
        int index = (((pos.getY() - minY) & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
        if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Singleton singleton) {
            if (stateId == singleton.blockStateId) {
                return;
            }
            SimpleBitStorage packedArray = new SimpleBitStorage(4, 4096);
            packedArray.set(index, 1);
            packedArrays[sectionIndex] = packedArray;
            var newBlockStates = new ChunkData_Latest.BlockStatePalettedContainer.Multiple();
            newBlockStates.paletteSize = 4;
            newBlockStates.palette = new int[] { singleton.blockStateId, stateId };
            newBlockStates.data = packedArray.getRaw();
            section.blockStates = newBlockStates;
        } else if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Multiple multiple) {
            SimpleBitStorage packedArray = packedArrays[sectionIndex];
            int paletteIndex = ArrayUtils.indexOf(multiple.palette, stateId);
            if (paletteIndex == -1) {
                if (multiple.palette.length == 256) {
                    int elementBits = Mth.ceillog2(Block.BLOCK_STATE_REGISTRY.size());
                    SimpleBitStorage newPackedArray = new SimpleBitStorage(elementBits, 4096);
                    for (int i = 0; i < 4096; i++) {
                        newPackedArray.set(i, multiple.palette[packedArray.get(i)]);
                    }
                    newPackedArray.set(index, stateId);
                    packedArrays[sectionIndex] = newPackedArray;
                    var newBlockStates = new ChunkData_Latest.BlockStatePalettedContainer.RegistryContainer();
                    newBlockStates.paletteSize = (byte) elementBits;
                    newBlockStates.data = newPackedArray.getRaw();
                    section.blockStates = newBlockStates;
                } else {
                    paletteIndex = multiple.palette.length;
                    multiple.palette = ArrayUtils.add(multiple.palette, stateId);
                    if (paletteIndex >= 16 && Mth.isPowerOfTwo(paletteIndex)) {
                        SimpleBitStorage newPackedArray = new SimpleBitStorage(packedArray.getBits() + 1, 4096);
                        for (int i = 0; i < 4096; i++) {
                            newPackedArray.set(i, packedArray.get(i));
                        }
                        multiple.paletteSize++;
                        multiple.data = newPackedArray.getRaw();
                        packedArrays[sectionIndex] = newPackedArray;
                        packedArray = newPackedArray;
                    }
                    packedArray.set(index, paletteIndex);
                }
            } else {
                packedArray.set(index, paletteIndex);
            }
        } else {
            packedArrays[sectionIndex].set(index, stateId);
        }
    }

    @Override
    public int getMinY() {
        return minY;
    }

    @Override
    public int getMaxY() {
        return minY + 16 * sections.size() - 1;
    }

    public boolean doesSectionExist(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            return false;
        }
        var section = (ChunkData_Latest.ChunkSection) sections.get(sectionIndex);
        return !(section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Singleton singleton)
                || singleton.blockStateId != 0;
    }
}
