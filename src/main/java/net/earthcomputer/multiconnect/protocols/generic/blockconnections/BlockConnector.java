package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.IBlockConnector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EightWayDirection;

import java.util.EnumMap;
import java.util.Map;

@ThreadSafe
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
        if (connector == null) {
            return false;
        } else {
            BlockState prevState = world.getBlockState(pos);
            connector.fix(world, pos);
            return world.getBlockState(pos) != prevState;
        }
    }

    // TODO: rewrite
//    public void fixChunkData(ChunkData chunkData, EnumMap<EightWayDirection, IntSet> blocksNeedingUpdateOut) {
//        // early exit if no fixing ever needs to be done
//        if (connectors.isEmpty()) {
//            return;
//        }
//
//        int chunkX = ChunkDataTranslator.current().getPacket().getX();
//        int chunkZ = ChunkDataTranslator.current().getPacket().getZ();
//        ChunkSection[] sections = chunkData.getSections();
//        for (int sectionY = 0; sectionY < sections.length; sectionY++) {
//            if (sections[sectionY] != null) {
//                for (BlockPos pos : BlockPos.iterate(chunkX << 4, sectionY << 4, chunkZ << 4, (chunkX << 4) + 15, (sectionY << 4) + 15, (chunkZ << 4) + 15)) {
//                    IBlockConnector connector = connectors.get(chunkData.getBlockState(pos).getBlock());
//                    if (connector != null) {
//                        EightWayDirection dir = ChunkConnector.directionForPos(pos);
//                        if (dir == null || !connector.needsNeighbors()) {
//                            connector.fix(chunkData, pos);
//                        } else {
//                            blocksNeedingUpdateOut.computeIfAbsent(dir, k -> new IntOpenHashSet()).add(ChunkConnector.packLocalPos(chunkData.getMinY(), pos));
//                        }
//                    }
//                }
//            }
//        }
//    }
}
