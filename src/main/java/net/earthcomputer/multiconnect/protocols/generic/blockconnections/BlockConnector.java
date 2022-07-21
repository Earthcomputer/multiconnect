package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.IBlockConnector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction8;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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

    public void fixChunkData(BlockConnectionsNetworkView connectionsView, EnumMap<Direction8, IntSet> blocksNeedingUpdateOut) {
        // early exit if no fixing ever needs to be done
        if (connectors.isEmpty()) {
            return;
        }

        int minY = connectionsView.getMinY();
        int numSections = (connectionsView.getMaxY() - minY + 1) >> 4;
        for (int sectionIndex = 0; sectionIndex < numSections; sectionIndex++) {
            if (connectionsView.doesSectionExist(sectionIndex)) {
                for (BlockPos pos : BlockPos.betweenClosed(0, (sectionIndex << 4) + minY, 0, 15, (sectionIndex << 4) + 15 + minY, 15)) {
                    IBlockConnector connector = connectors.get(connectionsView.getBlockState(pos).getBlock());
                    if (connector != null) {
                        Direction8 dir = ChunkConnector.directionForPos(pos);
                        if (dir == null || !connector.needsNeighbors()) {
                            connector.fix(connectionsView, pos);
                        } else {
                            blocksNeedingUpdateOut.computeIfAbsent(dir, k -> new IntOpenHashSet()).add(ChunkConnector.packLocalPos(connectionsView.getMinY(), pos));
                        }
                    }
                }
            }
        }
    }
}
