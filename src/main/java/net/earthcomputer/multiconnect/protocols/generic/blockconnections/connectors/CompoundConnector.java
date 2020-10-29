package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.minecraft.block.Block;

import java.util.List;

public class CompoundConnector extends SimpleConnector {
    private final boolean needsNeighbors;

    public CompoundConnector(List<IBlockConnector> connectors) {
        super((world, pos) -> {
            for (IBlockConnector connector : connectors) {
                connector.fix(world, pos);
            }
        }, connectors.stream().flatMap(connector -> connector.getAppliedBlocks().stream()).distinct().toArray(Block[]::new));
        this.needsNeighbors = connectors.stream().anyMatch(IBlockConnector::needsNeighbors);
    }

    @Override
    public boolean needsNeighbors() {
        return needsNeighbors;
    }
}
