package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.minecraft.block.Block;

import java.util.Collection;

public interface IBlockConnector extends IConnectorFunction {
    @ThreadSafe
    Collection<Block> getAppliedBlocks();
    @ThreadSafe
    boolean needsNeighbors();
}
