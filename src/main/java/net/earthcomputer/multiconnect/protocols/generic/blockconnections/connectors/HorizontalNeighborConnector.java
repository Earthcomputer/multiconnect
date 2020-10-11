package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class HorizontalNeighborConnector extends SimpleNeighborConnector {
    public HorizontalNeighborConnector(Block appliedBlock, IConnectionPredicate predicate) {
        super(appliedBlock, createConnectorFunction(predicate));
    }

    public HorizontalNeighborConnector(IConnectionPredicate predicate, Block... appliedBlocks) {
        super(createConnectorFunction(predicate), appliedBlocks);
    }

    private static IConnectorFunction createConnectorFunction(IConnectionPredicate predicate) {
        return (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            BlockState newState = state;

            newState = newState.with(Properties.NORTH, predicate.canConnect(state, world.getBlockState(pos.north()), Direction.NORTH));
            newState = newState.with(Properties.SOUTH, predicate.canConnect(state, world.getBlockState(pos.south()), Direction.SOUTH));
            newState = newState.with(Properties.WEST, predicate.canConnect(state, world.getBlockState(pos.west()), Direction.WEST));
            newState = newState.with(Properties.EAST, predicate.canConnect(state, world.getBlockState(pos.east()), Direction.EAST));

            return world.setBlockState(pos, newState);
        };
    }

    @FunctionalInterface
    public interface IConnectionPredicate {
        boolean canConnect(BlockState thisState, BlockState otherState, Direction dir);
    }
}
