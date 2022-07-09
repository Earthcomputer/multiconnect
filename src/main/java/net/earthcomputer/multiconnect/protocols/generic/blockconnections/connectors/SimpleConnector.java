package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import com.google.common.collect.ImmutableList;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class SimpleConnector implements IBlockConnector {
    private final List<Block> appliedBlocks;
    private final IConnectorFunction connectorFunction;

    public SimpleConnector(Block appliedBlock, IConnectorFunction connectorFunction) {
        this.appliedBlocks = Collections.singletonList(appliedBlock);
        this.connectorFunction = connectorFunction;
    }

    public SimpleConnector(IConnectorFunction connectorFunction, Block... appliedBlocks) {
        this.appliedBlocks = ImmutableList.copyOf(appliedBlocks);
        this.connectorFunction = connectorFunction;
    }

    @Override
    public Collection<Block> getAppliedBlocks() {
        return appliedBlocks;
    }

    @Override
    public void fix(IBlockConnectionsBlockView world, BlockPos pos) {
        connectorFunction.fix(world, pos);
    }
}
