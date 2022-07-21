package net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

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

            newState = newState.setValue(BlockStateProperties.NORTH, predicate.canConnect(state, world.getBlockState(pos.north()), Direction.NORTH));
            newState = newState.setValue(BlockStateProperties.SOUTH, predicate.canConnect(state, world.getBlockState(pos.south()), Direction.SOUTH));
            newState = newState.setValue(BlockStateProperties.WEST, predicate.canConnect(state, world.getBlockState(pos.west()), Direction.WEST));
            newState = newState.setValue(BlockStateProperties.EAST, predicate.canConnect(state, world.getBlockState(pos.east()), Direction.EAST));

            world.setBlockState(pos, newState);
        };
    }

    @FunctionalInterface
    public interface IConnectionPredicate {
        boolean canConnect(BlockState thisState, BlockState otherState, Direction dir);
    }
}
