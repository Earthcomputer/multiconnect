package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.HorizontalNeighborConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleInPlaceConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleNeighborConnector;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.FireBlockAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.RedstoneWireBlockAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.WallBlockAccessor;
import net.minecraft.block.*;
import net.minecraft.block.enums.*;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShapes;

public class BlockConnectors_1_12_2 {

    public static void register() {
        // snow for grass block, podzol, mycelium
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector((world, pos) -> {
            Block blockAbove = world.getBlockState(pos.up()).getBlock();
            boolean snowy = blockAbove == Blocks.SNOW_BLOCK || blockAbove == Blocks.SNOW;
            world.setBlockState(pos, world.getBlockState(pos).with(Properties.SNOWY, snowy));
        }, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM));

        // door divided state
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            if (state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                // copy from upper half
                BlockState upper = world.getBlockState(pos.up());
                if (upper.getBlock() == state.getBlock()) {
                    state = state.with(Properties.DOOR_HINGE, upper.get(Properties.DOOR_HINGE)).with(Properties.POWERED, upper.get(Properties.POWERED));
                }
            } else {
                // copy from lower half
                BlockState lower = world.getBlockState(pos.down());
                if (lower.getBlock() == state.getBlock()) {
                    state = state.with(Properties.HORIZONTAL_FACING, lower.get(Properties.HORIZONTAL_FACING)).with(Properties.OPEN, lower.get(Properties.OPEN));
                }
            }
            world.setBlockState(pos, state);
        }, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.IRON_DOOR));

        // copy double plant type from lower
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector((world, pos) -> {
            if (world.getBlockState(pos).get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                return;
            }
            Block lower = world.getBlockState(pos.down()).getBlock();
            if (lower instanceof TallPlantBlock) {
                world.setBlockState(pos, lower.getDefaultState().with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
            }
        }, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.ROSE_BUSH, Blocks.PEONY));

        // chest connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction chestFacing = state.get(Properties.HORIZONTAL_FACING);
            ChestType currentType = state.get(Properties.CHEST_TYPE);
            boolean changed = false;
            for (Direction dir : Direction.values()) {
                if (dir.getAxis().isHorizontal()) {
                    BlockState neighborState = world.getBlockState(pos.method_35851(dir));
                    ChestType correctDoubleType = dir == chestFacing.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT;
                    if (dir.getAxis() != chestFacing.getAxis()) {
                        if (state.getBlock() == neighborState.getBlock()) {
                            if (currentType == ChestType.SINGLE && chestFacing == neighborState.get(Properties.HORIZONTAL_FACING)) {
                                state = state.with(Properties.CHEST_TYPE, correctDoubleType);
                                changed = true;
                            }
                        } else if (currentType == correctDoubleType) {
                            state = state.with(Properties.CHEST_TYPE, ChestType.SINGLE);
                            changed = true;
                        }
                    }
                }
            }
            if (changed) {
                world.setBlockState(pos, state);
            }
        }, Blocks.CHEST, Blocks.TRAPPED_CHEST));

        // note block instruments
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector(Blocks.NOTE_BLOCK, (world, pos) -> {
            BlockState below = world.getBlockState(pos.down());
            Instrument instrument = Instrument.fromBlockState(below);
            world.setBlockState(pos, Blocks.NOTE_BLOCK.getDefaultState().with(Properties.INSTRUMENT, instrument));
        }));

        // bed occupied
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.WHITE_BED, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            BedPart part = state.get(Properties.BED_PART);
            Direction facing = state.get(Properties.HORIZONTAL_FACING);
            Direction dir = part == BedPart.FOOT ? facing : facing.getOpposite();
            BlockPos otherPos = pos.method_35851(dir);
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.getBlock() instanceof BedBlock && otherState.get(Properties.BED_PART) != part) {
                world.setBlockState(pos, state.with(Properties.OCCUPIED, otherState.get(Properties.OCCUPIED)));
            }
        }));

        // fire connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.FIRE, (world, pos) -> {
            FireBlockAccessor fireBlock = (FireBlockAccessor) Blocks.FIRE;
            BlockState below = world.getBlockState(pos.down());
            if (fireBlock.callIsFlammable(below)) {
                return;
            }
            if (IBlockConnectionsBlockView.withNullWorld(below.getBlock(), false, () -> below.isSideSolidFullSquare(null, null, Direction.UP))) {
                return;
            }

            BlockState state = Blocks.FIRE.getDefaultState();
            state = state.with(Properties.UP, fireBlock.callIsFlammable(world.getBlockState(pos.up())));
            state = state.with(Properties.NORTH, fireBlock.callIsFlammable(world.getBlockState(pos.method_35861())));
            state = state.with(Properties.SOUTH, fireBlock.callIsFlammable(world.getBlockState(pos.method_35859())));
            state = state.with(Properties.WEST, fireBlock.callIsFlammable(world.getBlockState(pos.method_35857())));
            state = state.with(Properties.EAST, fireBlock.callIsFlammable(world.getBlockState(pos.method_35855())));
            world.setBlockState(pos, state);
        }));

        // stair shape
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction facing = state.get(Properties.HORIZONTAL_FACING);

            // outer shape
            BlockState back = world.getBlockState(pos.method_35851(facing));
            if (StairsBlock.isStairs(back) && state.get(Properties.BLOCK_HALF) == back.get(Properties.BLOCK_HALF)) {
                Direction backFacing = back.get(Properties.HORIZONTAL_FACING);
                if (backFacing.getAxis() != state.get(Properties.HORIZONTAL_FACING).getAxis()) {
                    BlockState side = world.getBlockState(pos.offset(backFacing, -1));
                    if (!StairsBlock.isStairs(side) || side.get(Properties.HORIZONTAL_FACING) != state.get(Properties.HORIZONTAL_FACING) || side.get(Properties.BLOCK_HALF) != state.get(Properties.BLOCK_HALF)) {
                        if (backFacing == facing.rotateYCounterclockwise()) {
                            world.setBlockState(pos, state.with(Properties.STAIR_SHAPE, StairShape.OUTER_LEFT));
                        } else {
                            world.setBlockState(pos, state.with(Properties.STAIR_SHAPE, StairShape.OUTER_RIGHT));
                        }
                        return;
                    }
                }
            }

            // inner shape
            BlockState front = world.getBlockState(pos.method_35851(facing.getOpposite()));
            if (StairsBlock.isStairs(front) && state.get(Properties.BLOCK_HALF) == front.get(Properties.BLOCK_HALF)) {
                Direction frontFacing = front.get(Properties.HORIZONTAL_FACING);
                if (frontFacing.getAxis() != state.get(Properties.HORIZONTAL_FACING).getAxis()) {
                    BlockState side = world.getBlockState(pos.method_35851(frontFacing));
                    if (!StairsBlock.isStairs(side) || side.get(Properties.HORIZONTAL_FACING) != state.get(Properties.HORIZONTAL_FACING) || side.get(Properties.BLOCK_HALF) != state.get(Properties.BLOCK_HALF)) {
                        if (frontFacing == facing.rotateYCounterclockwise()) {
                            world.setBlockState(pos, state.with(Properties.STAIR_SHAPE, StairShape.INNER_LEFT));
                        } else {
                            world.setBlockState(pos, state.with(Properties.STAIR_SHAPE, StairShape.INNER_RIGHT));
                        }
                        return;
                    }
                }
            }

            world.setBlockState(pos, state.with(Properties.STAIR_SHAPE, StairShape.STRAIGHT));
        }, Blocks.OAK_STAIRS, Blocks.COBBLESTONE_STAIRS, Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS,
                Blocks.NETHER_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS,
                Blocks.JUNGLE_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.ACACIA_STAIRS, Blocks.DARK_OAK_STAIRS,
                Blocks.RED_SANDSTONE_STAIRS, Blocks.PURPUR_STAIRS));

        // redstone wire connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.REDSTONE_WIRE, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            for (Direction dir : Direction.Type.HORIZONTAL) {
                Property<WireConnection> property = RedstoneWireBlock.DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(dir);
                BlockPos offsetPos = pos.method_35851(dir);
                BlockState offsetState = world.getBlockState(offsetPos);
                if (RedstoneWireBlockAccessor.callConnectsTo(offsetState, dir) || (
                        !IBlockConnectionsBlockView.withNullWorld(offsetState.getBlock(), false, () -> offsetState.isSolidBlock(null, null))
                                && RedstoneWireBlockAccessor.callConnectsTo(world.getBlockState(offsetPos.down()), null)
                )) {
                    state = state.with(property, WireConnection.SIDE);
                } else {
                    BlockState above = world.getBlockState(pos.up());
                    if (IBlockConnectionsBlockView.withNullWorld(above.getBlock(), false, () -> above.isSolidBlock(null, null))) {
                        state = state.with(property, WireConnection.NONE);
                    } else {
                        boolean canRunOnTop = offsetState.getBlock() == Blocks.GLOWSTONE
                                || offsetState.getBlock() == Blocks.HOPPER
                                || IBlockConnectionsBlockView.withNullWorld(offsetState.getBlock(), false, () -> offsetState.isSideSolidFullSquare(null, null, Direction.UP));
                        if (canRunOnTop && RedstoneWireBlockAccessor.callConnectsTo(world.getBlockState(offsetPos.up()), null)) {
                            if (IBlockConnectionsBlockView.withNullWorld(offsetState.getBlock(), false, () -> offsetState.isSideSolidFullSquare(null, null, dir.getOpposite()))) {
                                state = state.with(property, WireConnection.UP);
                            } else {
                                state = state.with(property, WireConnection.SIDE);
                            }
                        } else {
                            state = state.with(property, WireConnection.NONE);
                        }
                    }
                }
            }

            world.setBlockState(pos, state);
        }));

        // fence connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new HorizontalNeighborConnector(
                (thisState, otherState, dir) -> ((FenceBlock) thisState.getBlock()).canConnect(otherState, IBlockConnectionsBlockView.withNullWorld(otherState.getBlock(), false, () -> otherState.isSideSolidFullSquare(null, null, dir.getOpposite())), dir),
                Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE, Blocks.DARK_OAK_FENCE, Blocks.ACACIA_FENCE
        ));

        // repeater locking
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.REPEATER, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction repeaterFacing = state.get(Properties.HORIZONTAL_FACING);
            Direction rightFacing = repeaterFacing.rotateYClockwise();
            BlockState right = world.getBlockState(pos.method_35851(rightFacing));
            Direction leftFacing = repeaterFacing.rotateYCounterclockwise();
            BlockState left = world.getBlockState(pos.method_35851(leftFacing));

            boolean locked = false;
            if (right.getBlock() instanceof AbstractRedstoneGateBlock && right.get(Properties.HORIZONTAL_FACING) == leftFacing && right.get(Properties.POWERED)) {
                locked = true;
            } else if (left.getBlock() instanceof AbstractRedstoneGateBlock && left.get(Properties.HORIZONTAL_FACING) == rightFacing && left.get(Properties.POWERED)) {
                locked = true;
            }

            world.setBlockState(pos, state.with(Properties.LOCKED, locked));
        }));

        // glass pane and iron bars connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new HorizontalNeighborConnector(
                (thisState, otherState, dir) -> ((PaneBlock) thisState.getBlock()).connectsTo(otherState, IBlockConnectionsBlockView.withNullWorld(otherState.getBlock(), false, () -> otherState.isSideSolidFullSquare(null, null, dir.getOpposite()))),
                Blocks.IRON_BARS, Blocks.GLASS_PANE, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE,
                Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
                Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE,
                Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE,
                Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE));

        // vine connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector(Blocks.VINE, (world, pos) -> {
            BlockState above = world.getBlockState(pos.up());
            boolean up = Block.isFaceFullSquare(IBlockConnectionsBlockView.withNullWorld(above.getBlock(), VoxelShapes.fullCube(), () -> above.getCollisionShape(null, null)), Direction.UP);
            world.setBlockState(pos, world.getBlockState(pos).with(Properties.UP, up));
        }));

        // fence gate wall connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction sideDir = state.get(Properties.HORIZONTAL_FACING).rotateYClockwise();
            boolean inWall = world.getBlockState(pos.method_35851(sideDir)).isIn(BlockTags.WALLS) || world.getBlockState(pos.offset(sideDir, -1)).isIn(BlockTags.WALLS);
            world.setBlockState(pos, state.with(Properties.IN_WALL, inWall));
        }, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE));

        // tripwire connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new HorizontalNeighborConnector(
                Blocks.TRIPWIRE,
                (thisState, otherState, dir) -> ((TripwireBlock) thisState.getBlock()).shouldConnectTo(otherState, dir)
        ));

        // wall connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            WallBlockAccessor wallBlock = (WallBlockAccessor) state.getBlock();
            BlockState north = world.getBlockState(pos.method_35861());
            BlockState south = world.getBlockState(pos.method_35859());
            BlockState west = world.getBlockState(pos.method_35857());
            BlockState east = world.getBlockState(pos.method_35855());
            boolean connectNorth = wallBlock.callShouldConnectTo(north, IBlockConnectionsBlockView.withNullWorld(north.getBlock(), false, () -> north.isSideSolidFullSquare(null, null, Direction.SOUTH)), Direction.SOUTH);
            boolean connectSouth = wallBlock.callShouldConnectTo(south, IBlockConnectionsBlockView.withNullWorld(south.getBlock(), false, () -> south.isSideSolidFullSquare(null, null, Direction.NORTH)), Direction.NORTH);
            boolean connectWest = wallBlock.callShouldConnectTo(west, IBlockConnectionsBlockView.withNullWorld(west.getBlock(), false, () -> west.isSideSolidFullSquare(null, null, Direction.EAST)), Direction.EAST);
            boolean connectEast = wallBlock.callShouldConnectTo(east, IBlockConnectionsBlockView.withNullWorld(east.getBlock(), false, () -> east.isSideSolidFullSquare(null, null, Direction.WEST)), Direction.WEST);
            state = state.with(Properties.NORTH_WALL_SHAPE, connectNorth ? WallShape.LOW : WallShape.NONE);
            state = state.with(Properties.SOUTH_WALL_SHAPE, connectSouth ? WallShape.LOW : WallShape.NONE);
            state = state.with(Properties.WEST_WALL_SHAPE, connectWest ? WallShape.LOW : WallShape.NONE);
            state = state.with(Properties.EAST_WALL_SHAPE, connectEast ? WallShape.LOW : WallShape.NONE);
            boolean straightWall = (connectNorth && connectSouth && !connectEast && !connectWest) || (connectEast && connectWest && !connectNorth && !connectSouth);
            boolean connectUp = !straightWall || !world.getBlockState(pos.up()).isAir();
            state = state.with(Properties.UP, connectUp);
            world.setBlockState(pos, state);
        }, Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL));

        // chorus plant connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.CHORUS_PLANT, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockState offsetState = world.getBlockState(pos.method_35851(dir));
                boolean canConnect = offsetState.getBlock() == Blocks.CHORUS_PLANT || offsetState.getBlock() == Blocks.CHORUS_FLOWER || (dir == Direction.DOWN && offsetState.getBlock() == Blocks.END_STONE);
                state = state.with(ConnectingBlock.FACING_PROPERTIES.get(dir), canConnect);
            }
            world.setBlockState(pos, state);
        }));
    }
}
