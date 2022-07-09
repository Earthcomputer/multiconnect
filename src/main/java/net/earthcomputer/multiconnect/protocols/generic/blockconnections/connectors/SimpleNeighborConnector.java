package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.minecraft.world.level.block.Block;

public class SimpleNeighborConnector extends SimpleConnector {
    public SimpleNeighborConnector(Block appliedBlock, IConnectorFunction connectorFunction) {
        super(appliedBlock, connectorFunction);
    }

    public SimpleNeighborConnector(IConnectorFunction connectorFunction, Block... appliedBlocks) {
        super(connectorFunction, appliedBlocks);
    }

    @Override
    public boolean needsNeighbors() {
        return true;
    }
}
