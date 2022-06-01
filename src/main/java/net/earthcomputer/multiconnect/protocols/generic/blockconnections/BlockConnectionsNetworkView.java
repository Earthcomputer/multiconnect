package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.earthcomputer.multiconnect.packets.latest.ChunkData_Latest;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.collection.PackedIntegerArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Assumes that block state IDs have been translated by this point
 */
public class BlockConnectionsNetworkView implements IBlockConnectionsBlockView {
    private final int minY;
    private final ChunkData_Latest.ChunkSection[] sections;
    private final PackedIntegerArray[] packedArrays;

    public BlockConnectionsNetworkView(int minY, ChunkData_Latest.ChunkSection[] sections) {
        this.minY = minY;
        this.sections = sections;
        this.packedArrays = new PackedIntegerArray[sections.length];
        for (int i = 0; i < sections.length; i++) {
            if (sections[i] != null) {
                var blockStates = sections[i].blockStates;
                if (blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Multiple multiple) {
                    packedArrays[i] = new PackedIntegerArray(multiple.paletteSize, 4096, multiple.data);
                } else if (blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.RegistryContainer registryContainer) {
                    packedArrays[i] = new PackedIntegerArray(registryContainer.paletteSize, 4096, registryContainer.data);
                }
            }
        }
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int sectionIndex = (pos.getY() - minY) >> 4;
        if (sectionIndex < 0 || sectionIndex >= sections.length) {
            return Blocks.AIR.getDefaultState();
        }
        ChunkData_Latest.ChunkSection section = sections[sectionIndex];
        if (section == null) {
            return Blocks.AIR.getDefaultState();
        }
        int stateId;
        if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Singleton singleton) {
            stateId = singleton.blockStateId;
        } else if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Multiple multiple) {
            stateId = packedArrays[sectionIndex].get((((pos.getY() - minY) & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15));
            stateId = multiple.palette[stateId];
        } else {
            stateId = packedArrays[sectionIndex].get((((pos.getY() - minY) & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15));
        }
        return Block.getStateFromRawId(stateId);
    }

    @Override
    public void setBlockState(BlockPos pos, BlockState state) {
        int sectionIndex = (pos.getY() - minY) >> 4;
        if (sectionIndex < 0 || sectionIndex >= sections.length) {
            return;
        }
        ChunkData_Latest.ChunkSection section = sections[sectionIndex];
        if (section == null) {
            return;
        }
        int stateId = Block.getRawIdFromState(state);
        int index = (((pos.getY() - minY) & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
        if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Singleton singleton) {
            if (stateId == singleton.blockStateId) {
                return;
            }
            PackedIntegerArray packedArray = new PackedIntegerArray(4, 4096);
            packedArray.set(index, 1);
            packedArrays[sectionIndex] = packedArray;
            var newBlockStates = new ChunkData_Latest.BlockStatePalettedContainer.Multiple();
            newBlockStates.paletteSize = 4;
            newBlockStates.palette = new int[] { singleton.blockStateId, stateId };
            newBlockStates.data = packedArray.getData();
            section.blockStates = newBlockStates;
        } else if (section.blockStates instanceof ChunkData_Latest.BlockStatePalettedContainer.Multiple multiple) {
            PackedIntegerArray packedArray = packedArrays[sectionIndex];
            int paletteIndex = ArrayUtils.indexOf(multiple.palette, stateId);
            if (paletteIndex == -1) {
                if (multiple.palette.length == 256) {
                    int elementBits = MathHelper.ceilLog2(Block.STATE_IDS.size());
                    PackedIntegerArray newPackedArray = new PackedIntegerArray(elementBits, 4096);
                    for (int i = 0; i < 4096; i++) {
                        newPackedArray.set(i, multiple.palette[packedArray.get(i)]);
                    }
                    newPackedArray.set(index, stateId);
                    packedArrays[sectionIndex] = newPackedArray;
                    var newBlockStates = new ChunkData_Latest.BlockStatePalettedContainer.RegistryContainer();
                    newBlockStates.paletteSize = (byte) elementBits;
                    newBlockStates.data = newPackedArray.getData();
                    section.blockStates = newBlockStates;
                } else {
                    paletteIndex = multiple.palette.length;
                    multiple.palette = ArrayUtils.add(multiple.palette, stateId);
                    if (paletteIndex >= 16 && MathHelper.isPowerOfTwo(paletteIndex)) {
                        PackedIntegerArray newPackedArray = new PackedIntegerArray(packedArray.getElementBits() + 1, 4096);
                        for (int i = 0; i < 4096; i++) {
                            newPackedArray.set(i, packedArray.get(i));
                        }
                        multiple.paletteSize++;
                        multiple.data = newPackedArray.getData();
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
        return minY + 16 * sections.length - 1;
    }

    public boolean doesSectionExist(int sectionIndex) {
        return sectionIndex >= 0 && sectionIndex < sections.length && sections[sectionIndex] != null;
    }
}
