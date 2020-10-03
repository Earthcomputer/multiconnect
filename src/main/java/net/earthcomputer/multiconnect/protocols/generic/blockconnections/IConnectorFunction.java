package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface IConnectorFunction {
    boolean fix(IBlockConnectionsBlockView world, BlockPos pos);
}
