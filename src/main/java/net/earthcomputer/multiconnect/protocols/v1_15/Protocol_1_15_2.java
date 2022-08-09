package net.earthcomputer.multiconnect.protocols.v1_15;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleInPlaceConnector;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

public class Protocol_1_15_2 extends Protocol_1_16 {

    public static void registerConnectors() {
        BlockConnections.registerConnector(Protocols.V1_15_2, new SimpleInPlaceConnector(Blocks.REDSTONE_WIRE, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            boolean north = state.getValue(BlockStateProperties.NORTH_REDSTONE) != RedstoneSide.NONE;
            boolean south = state.getValue(BlockStateProperties.SOUTH_REDSTONE) != RedstoneSide.NONE;
            boolean west = state.getValue(BlockStateProperties.WEST_REDSTONE) != RedstoneSide.NONE;
            boolean east = state.getValue(BlockStateProperties.EAST_REDSTONE) != RedstoneSide.NONE;
            if (north && !south && !west && !east) state = state.setValue(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE);
            if (!north && south && !west && !east) state = state.setValue(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE);
            if (!north && !south && west && !east) state = state.setValue(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE);
            if (!north && !south && !west && east) state = state.setValue(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE);
            world.setBlockState(pos, state);
        }));
    }

    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        destroySpeed = super.getBlockDestroySpeed(state, destroySpeed);
        if (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON || state.getBlock() == Blocks.PISTON_HEAD) {
            destroySpeed = 0.5f;
        }
        return destroySpeed;
    }

    @Override
    public float getBlockExplosionResistance(Block block, float resistance) {
        resistance = super.getBlockExplosionResistance(block, resistance);
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD) {
            resistance = 0.5f;
        }
        return resistance;
    }
}
