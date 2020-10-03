package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.earthcomputer.multiconnect.protocols.generic.ChunkDataTranslator;
import net.minecraft.block.Block;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkSection;

import java.util.EnumMap;
import java.util.Map;

public class BlockConnector {
    private final Map<Block, IBlockConnector> connectors;

    BlockConnector(Map<Block, IBlockConnector> connectors) {
        this.connectors = connectors;
    }

    public boolean shouldProcess(Block block) {
        return connectors.containsKey(block);
    }

    public boolean needsNeighbors(Block block) {
        IBlockConnector connector = connectors.get(block);
        return connector != null && connector.needsNeighbors();
    }

    public boolean fix(IBlockConnectionsBlockView world, BlockPos pos, Block block) {
        IBlockConnector connector = connectors.get(block);
        return connector != null && connector.fix(world, pos);
    }

    public void fixChunkData(ChunkData chunkData, EnumMap<EightWayDirection, ShortSet> blocksNeedingUpdateOut) {
        int chunkX = ChunkDataTranslator.current().getPacket().getX();
        int chunkZ = ChunkDataTranslator.current().getPacket().getZ();
        ChunkSection[] sections = chunkData.getSections();
        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
            if (sections[sectionY] != null) {
                for (BlockPos pos : BlockPos.iterate(chunkX << 4, sectionY << 4, chunkZ << 4, (chunkX << 4) + 15, (sectionY << 4) + 15, (chunkZ << 4) + 15)) {
                    IBlockConnector connector = connectors.get(chunkData.getBlockState(pos).getBlock());
                    if (connector != null) {
                        EightWayDirection dir = ChunkConnector.directionForPos(pos);
                        if (dir == null || !connector.needsNeighbors()) {
                            connector.fix(chunkData, pos);
                        } else {
                            blocksNeedingUpdateOut.computeIfAbsent(dir, k -> new ShortOpenHashSet()).add(ChunkConnector.packLocalPos(pos));
                        }
                    }
                }
            }
        }
    }
}
