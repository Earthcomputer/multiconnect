package net.earthcomputer.multiconnect.protocols.v1_12;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.IBlockConnectionsBlockView;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.HorizontalNeighborConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleInPlaceConnector;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleNeighborConnector;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.FireBlockAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.RedStoneWireBlockAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.mixin.WallBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.Shapes;

public class BlockConnectors_1_12_2 {

    public static void register() {
        // snow for grass block, podzol, mycelium
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector((world, pos) -> {
            Block blockAbove = world.getBlockState(pos.above()).getBlock();
            boolean snowy = blockAbove == Blocks.SNOW_BLOCK || blockAbove == Blocks.SNOW;
            world.setBlockState(pos, world.getBlockState(pos).setValue(BlockStateProperties.SNOWY, snowy));
        }, Blocks.GRASS_BLOCK, Blocks.PODZOL, Blocks.MYCELIUM));

        // door divided state
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            if (state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                // copy from upper half
                BlockState upper = world.getBlockState(pos.above());
                if (upper.getBlock() == state.getBlock()) {
                    state = state.setValue(BlockStateProperties.DOOR_HINGE, upper.getValue(BlockStateProperties.DOOR_HINGE)).setValue(BlockStateProperties.POWERED, upper.getValue(BlockStateProperties.POWERED));
                }
            } else {
                // copy from lower half
                BlockState lower = world.getBlockState(pos.below());
                if (lower.getBlock() == state.getBlock()) {
                    state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, lower.getValue(BlockStateProperties.HORIZONTAL_FACING)).setValue(BlockStateProperties.OPEN, lower.getValue(BlockStateProperties.OPEN));
                }
            }
            world.setBlockState(pos, state);
        }, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.IRON_DOOR));

        // copy double plant type from lower
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector((world, pos) -> {
            if (world.getBlockState(pos).getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER) {
                return;
            }
            Block lower = world.getBlockState(pos.below()).getBlock();
            if (lower instanceof DoublePlantBlock) {
                world.setBlockState(pos, lower.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
            }
        }, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.ROSE_BUSH, Blocks.PEONY));

        // chest connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction chestFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            ChestType currentType = state.getValue(BlockStateProperties.CHEST_TYPE);
            boolean changed = false;
            for (Direction dir : Direction.values()) {
                if (dir.getAxis().isHorizontal()) {
                    BlockState neighborState = world.getBlockState(pos.relative(dir));
                    ChestType correctDoubleType = dir == chestFacing.getClockWise() ? ChestType.LEFT : ChestType.RIGHT;
                    if (dir.getAxis() != chestFacing.getAxis()) {
                        if (state.getBlock() == neighborState.getBlock()) {
                            if (currentType == ChestType.SINGLE && chestFacing == neighborState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                                state = state.setValue(BlockStateProperties.CHEST_TYPE, correctDoubleType);
                                changed = true;
                            }
                        } else if (currentType == correctDoubleType) {
                            state = state.setValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE);
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
            BlockState below = world.getBlockState(pos.below());
            NoteBlockInstrument instrument = NoteBlockInstrument.byState(below);
            BlockState newState = Blocks.NOTE_BLOCK.defaultBlockState().setValue(BlockStateProperties.NOTEBLOCK_INSTRUMENT, instrument);
            // TODO: rewrite 1.12.2
//            if (ConnectionInfo.protocol.acceptBlockState(newState)) {
                world.setBlockState(pos, newState);
//            }
        }));

        // bed occupied
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.WHITE_BED, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            BedPart part = state.getValue(BlockStateProperties.BED_PART);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction dir = part == BedPart.FOOT ? facing : facing.getOpposite();
            BlockPos otherPos = pos.relative(dir);
            BlockState otherState = world.getBlockState(otherPos);
            if (otherState.getBlock() instanceof BedBlock && otherState.getValue(BlockStateProperties.BED_PART) != part) {
                world.setBlockState(pos, state.setValue(BlockStateProperties.OCCUPIED, otherState.getValue(BlockStateProperties.OCCUPIED)));
            }
        }));

        // fire connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.FIRE, (world, pos) -> {
            FireBlockAccessor fireBlock = (FireBlockAccessor) Blocks.FIRE;
            BlockState below = world.getBlockState(pos.below());
            if (fireBlock.callCanBurn(below)) {
                return;
            }
            if (IBlockConnectionsBlockView.withNullLevel(below.getBlock(), false, () -> below.isFaceSturdy(null, null, Direction.UP))) {
                return;
            }

            BlockState state = Blocks.FIRE.defaultBlockState();
            state = state.setValue(BlockStateProperties.UP, fireBlock.callCanBurn(world.getBlockState(pos.above())));
            state = state.setValue(BlockStateProperties.NORTH, fireBlock.callCanBurn(world.getBlockState(pos.north())));
            state = state.setValue(BlockStateProperties.SOUTH, fireBlock.callCanBurn(world.getBlockState(pos.south())));
            state = state.setValue(BlockStateProperties.WEST, fireBlock.callCanBurn(world.getBlockState(pos.west())));
            state = state.setValue(BlockStateProperties.EAST, fireBlock.callCanBurn(world.getBlockState(pos.east())));
            world.setBlockState(pos, state);
        }));

        // stair shape
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);

            // outer shape
            BlockState back = world.getBlockState(pos.relative(facing));
            if (StairBlock.isStairs(back) && state.getValue(BlockStateProperties.HALF) == back.getValue(BlockStateProperties.HALF)) {
                Direction backFacing = back.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (backFacing.getAxis() != state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis()) {
                    BlockState side = world.getBlockState(pos.relative(backFacing, -1));
                    if (!StairBlock.isStairs(side) || side.getValue(BlockStateProperties.HORIZONTAL_FACING) != state.getValue(BlockStateProperties.HORIZONTAL_FACING) || side.getValue(BlockStateProperties.HALF) != state.getValue(BlockStateProperties.HALF)) {
                        if (backFacing == facing.getCounterClockWise()) {
                            world.setBlockState(pos, state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.OUTER_LEFT));
                        } else {
                            world.setBlockState(pos, state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.OUTER_RIGHT));
                        }
                        return;
                    }
                }
            }

            // inner shape
            BlockState front = world.getBlockState(pos.relative(facing.getOpposite()));
            if (StairBlock.isStairs(front) && state.getValue(BlockStateProperties.HALF) == front.getValue(BlockStateProperties.HALF)) {
                Direction frontFacing = front.getValue(BlockStateProperties.HORIZONTAL_FACING);
                if (frontFacing.getAxis() != state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis()) {
                    BlockState side = world.getBlockState(pos.relative(frontFacing));
                    if (!StairBlock.isStairs(side) || side.getValue(BlockStateProperties.HORIZONTAL_FACING) != state.getValue(BlockStateProperties.HORIZONTAL_FACING) || side.getValue(BlockStateProperties.HALF) != state.getValue(BlockStateProperties.HALF)) {
                        if (frontFacing == facing.getCounterClockWise()) {
                            world.setBlockState(pos, state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.INNER_LEFT));
                        } else {
                            world.setBlockState(pos, state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.INNER_RIGHT));
                        }
                        return;
                    }
                }
            }

            world.setBlockState(pos, state.setValue(BlockStateProperties.STAIRS_SHAPE, StairsShape.STRAIGHT));
        }, Blocks.OAK_STAIRS, Blocks.COBBLESTONE_STAIRS, Blocks.BRICK_STAIRS, Blocks.STONE_BRICK_STAIRS,
                Blocks.NETHER_BRICK_STAIRS, Blocks.SANDSTONE_STAIRS, Blocks.SPRUCE_STAIRS, Blocks.BIRCH_STAIRS,
                Blocks.JUNGLE_STAIRS, Blocks.QUARTZ_STAIRS, Blocks.ACACIA_STAIRS, Blocks.DARK_OAK_STAIRS,
                Blocks.RED_SANDSTONE_STAIRS, Blocks.PURPUR_STAIRS));

        // redstone wire connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.REDSTONE_WIRE, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                Property<RedstoneSide> property = RedStoneWireBlock.PROPERTY_BY_DIRECTION.get(dir);
                BlockPos offsetPos = pos.relative(dir);
                BlockState offsetState = world.getBlockState(offsetPos);
                if (RedStoneWireBlockAccessor.callShouldConnectTo(offsetState, dir) || (
                        !IBlockConnectionsBlockView.withNullLevel(offsetState.getBlock(), false, () -> offsetState.isRedstoneConductor(null, null))
                                && RedStoneWireBlockAccessor.callShouldConnectTo(world.getBlockState(offsetPos.below()), null)
                )) {
                    state = state.setValue(property, RedstoneSide.SIDE);
                } else {
                    BlockState above = world.getBlockState(pos.above());
                    if (IBlockConnectionsBlockView.withNullLevel(above.getBlock(), false, () -> above.isRedstoneConductor(null, null))) {
                        state = state.setValue(property, RedstoneSide.NONE);
                    } else {
                        boolean canRunOnTop = offsetState.getBlock() == Blocks.GLOWSTONE
                                || offsetState.getBlock() == Blocks.HOPPER
                                || IBlockConnectionsBlockView.withNullLevel(offsetState.getBlock(), false, () -> offsetState.isFaceSturdy(null, null, Direction.UP));
                        if (canRunOnTop && RedStoneWireBlockAccessor.callShouldConnectTo(world.getBlockState(offsetPos.above()), null)) {
                            if (IBlockConnectionsBlockView.withNullLevel(offsetState.getBlock(), false, () -> offsetState.isFaceSturdy(null, null, dir.getOpposite()))) {
                                state = state.setValue(property, RedstoneSide.UP);
                            } else {
                                state = state.setValue(property, RedstoneSide.SIDE);
                            }
                        } else {
                            state = state.setValue(property, RedstoneSide.NONE);
                        }
                    }
                }
            }

            world.setBlockState(pos, state);
        }));

        // fence connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new HorizontalNeighborConnector(
                (thisState, otherState, dir) -> ((FenceBlock) thisState.getBlock()).connectsTo(otherState, IBlockConnectionsBlockView.withNullLevel(otherState.getBlock(), false, () -> otherState.isFaceSturdy(null, null, dir.getOpposite())), dir),
                Blocks.OAK_FENCE, Blocks.NETHER_BRICK_FENCE, Blocks.SPRUCE_FENCE, Blocks.BIRCH_FENCE, Blocks.JUNGLE_FENCE, Blocks.DARK_OAK_FENCE, Blocks.ACACIA_FENCE
        ));

        // repeater locking
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.REPEATER, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction repeaterFacing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction rightFacing = repeaterFacing.getClockWise();
            BlockState right = world.getBlockState(pos.relative(rightFacing));
            Direction leftFacing = repeaterFacing.getCounterClockWise();
            BlockState left = world.getBlockState(pos.relative(leftFacing));

            boolean locked = false;
            if (right.getBlock() instanceof DiodeBlock && right.getValue(BlockStateProperties.HORIZONTAL_FACING) == leftFacing && right.getValue(BlockStateProperties.POWERED)) {
                locked = true;
            } else if (left.getBlock() instanceof DiodeBlock && left.getValue(BlockStateProperties.HORIZONTAL_FACING) == rightFacing && left.getValue(BlockStateProperties.POWERED)) {
                locked = true;
            }

            world.setBlockState(pos, state.setValue(BlockStateProperties.LOCKED, locked));
        }));

        // glass pane and iron bars connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(
                (world, pos) -> {
                    BlockState newState = world.getBlockState(pos);
                    IronBarsBlock paneBlock = (IronBarsBlock) newState.getBlock();

                    BlockState northState = world.getBlockState(pos.north());
                    Boolean north = paneBlock.attachsTo(northState, IBlockConnectionsBlockView.withNullLevel(northState.getBlock(), false, () -> northState.isFaceSturdy(null, null, Direction.SOUTH)));
                    newState = newState.setValue(BlockStateProperties.NORTH, north);
                    BlockState southState = world.getBlockState(pos.south());
                    Boolean south = paneBlock.attachsTo(southState, IBlockConnectionsBlockView.withNullLevel(southState.getBlock(), false, () -> southState.isFaceSturdy(null, null, Direction.NORTH)));
                    newState = newState.setValue(BlockStateProperties.SOUTH, south);
                    BlockState westState = world.getBlockState(pos.west());
                    Boolean west = paneBlock.attachsTo(westState, IBlockConnectionsBlockView.withNullLevel(westState.getBlock(), false, () -> westState.isFaceSturdy(null, null, Direction.EAST)));
                    newState = newState.setValue(BlockStateProperties.WEST, west);
                    BlockState eastState = world.getBlockState(pos.east());
                    Boolean east = paneBlock.attachsTo(eastState, IBlockConnectionsBlockView.withNullLevel(eastState.getBlock(), false, () -> eastState.isFaceSturdy(null, null, Direction.WEST)));
                    newState = newState.setValue(BlockStateProperties.EAST, east);

                    if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
                        if (!north && !south && !west && !east) {
                            newState = newState.setValue(BlockStateProperties.NORTH, true).setValue(BlockStateProperties.SOUTH, true).setValue(BlockStateProperties.WEST, true).setValue(BlockStateProperties.EAST, true);
                        }
                    }

                    world.setBlockState(pos, newState);
                },
                Blocks.IRON_BARS, Blocks.GLASS_PANE, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE,
                Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE,
                Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE,
                Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE,
                Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE));

        // vine connections
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleInPlaceConnector(Blocks.VINE, (world, pos) -> {
            BlockState above = world.getBlockState(pos.above());
            boolean up = Block.isFaceFull(IBlockConnectionsBlockView.withNullLevel(above.getBlock(), Shapes.block(), () -> above.getCollisionShape(null, null)), Direction.UP);
            world.setBlockState(pos, world.getBlockState(pos).setValue(BlockStateProperties.UP, up));
        }));

        // fence gate wall connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            Direction sideDir = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getClockWise();
            boolean inWall = world.getBlockState(pos.relative(sideDir)).is(BlockTags.WALLS) || world.getBlockState(pos.relative(sideDir, -1)).is(BlockTags.WALLS);
            world.setBlockState(pos, state.setValue(BlockStateProperties.IN_WALL, inWall));
        }, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE));

        // tripwire connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new HorizontalNeighborConnector(
                Blocks.TRIPWIRE,
                (thisState, otherState, dir) -> ((TripWireBlock) thisState.getBlock()).shouldConnectTo(otherState, dir)
        ));

        // wall connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector((world, pos) -> {
            BlockState state = world.getBlockState(pos);
            WallBlockAccessor wallBlock = (WallBlockAccessor) state.getBlock();
            BlockState north = world.getBlockState(pos.north());
            BlockState south = world.getBlockState(pos.south());
            BlockState west = world.getBlockState(pos.west());
            BlockState east = world.getBlockState(pos.east());
            boolean connectNorth = wallBlock.callConnectsTo(north, IBlockConnectionsBlockView.withNullLevel(north.getBlock(), false, () -> north.isFaceSturdy(null, null, Direction.SOUTH)), Direction.SOUTH);
            boolean connectSouth = wallBlock.callConnectsTo(south, IBlockConnectionsBlockView.withNullLevel(south.getBlock(), false, () -> south.isFaceSturdy(null, null, Direction.NORTH)), Direction.NORTH);
            boolean connectWest = wallBlock.callConnectsTo(west, IBlockConnectionsBlockView.withNullLevel(west.getBlock(), false, () -> west.isFaceSturdy(null, null, Direction.EAST)), Direction.EAST);
            boolean connectEast = wallBlock.callConnectsTo(east, IBlockConnectionsBlockView.withNullLevel(east.getBlock(), false, () -> east.isFaceSturdy(null, null, Direction.WEST)), Direction.WEST);
            state = state.setValue(BlockStateProperties.NORTH_WALL, connectNorth ? WallSide.LOW : WallSide.NONE);
            state = state.setValue(BlockStateProperties.SOUTH_WALL, connectSouth ? WallSide.LOW : WallSide.NONE);
            state = state.setValue(BlockStateProperties.WEST_WALL, connectWest ? WallSide.LOW : WallSide.NONE);
            state = state.setValue(BlockStateProperties.EAST_WALL, connectEast ? WallSide.LOW : WallSide.NONE);
            boolean straightWall = (connectNorth && connectSouth && !connectEast && !connectWest) || (connectEast && connectWest && !connectNorth && !connectSouth);
            boolean connectUp = !straightWall || !world.getBlockState(pos.above()).isAir();
            state = state.setValue(BlockStateProperties.UP, connectUp);
            world.setBlockState(pos, state);
        }, Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL));

        // chorus plant connection
        BlockConnections.registerConnector(Protocols.V1_12_2, new SimpleNeighborConnector(Blocks.CHORUS_PLANT, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockState offsetState = world.getBlockState(pos.relative(dir));
                boolean canConnect = offsetState.getBlock() == Blocks.CHORUS_PLANT || offsetState.getBlock() == Blocks.CHORUS_FLOWER || (dir == Direction.DOWN && offsetState.getBlock() == Blocks.END_STONE);
                state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(dir), canConnect);
            }
            world.setBlockState(pos, state);
        }));
    }
}
