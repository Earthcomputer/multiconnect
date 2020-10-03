package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.minecraft.block.Block;

import java.util.Collection;

public interface IBlockConnector extends IConnectorFunction {
    Collection<Block> getAppliedBlocks();
    boolean needsNeighbors();
}
