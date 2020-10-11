package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface IConnectorFunction {
    boolean fix(IBlockConnectionsBlockView world, BlockPos pos);
}
