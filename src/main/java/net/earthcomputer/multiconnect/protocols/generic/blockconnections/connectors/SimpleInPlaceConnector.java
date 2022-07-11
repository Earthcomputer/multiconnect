package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.minecraft.world.level.block.Block;

public class SimpleInPlaceConnector extends SimpleConnector {
    public SimpleInPlaceConnector(Block appliedBlock, IConnectorFunction connectorFunction) {
        super(appliedBlock, connectorFunction);
    }

    public SimpleInPlaceConnector(IConnectorFunction connectorFunction, Block... appliedBlocks) {
        super(connectorFunction, appliedBlocks);
    }

    @Override
    public boolean needsNeighbors() {
        return false;
    }
}
