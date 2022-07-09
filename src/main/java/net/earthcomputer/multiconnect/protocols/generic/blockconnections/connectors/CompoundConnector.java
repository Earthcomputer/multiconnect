package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import java.util.List;
import net.minecraft.world.level.block.Block;

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
