package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.minecraft.core.BlockPos;

@FunctionalInterface
public interface IConnectorFunction {
    @ThreadSafe
    void fix(IBlockConnectionsBlockView world, BlockPos pos);
}
