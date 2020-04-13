package net.earthcomputer.multiconnect.protocols.v1_12_2;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.ChunkPalettedStorageFixAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.UpgradeDataAccessor;
import net.minecraft.block.*;
import net.minecraft.nbt.*;
import net.minecraft.state.properties.BedPart;
import net.minecraft.state.properties.ChestType;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;

import java.util.function.IntConsumer;

import static net.minecraft.block.Blocks.*;

public class ChunkUpgrader {

    public static UpgradeData fixChunk(Chunk chunk) {
        IntList centerIndicesToUpgrade = new IntArrayList();
        int sidesToUpgrade = 0;

        BlockPos.Mutable otherPos = new BlockPos.Mutable();
        for (BlockPos pos : BlockPos.getAllInBoxMutable(chunk.getPos().getXStart(), 0, chunk.getPos().getZStart(),
                chunk.getPos().getXEnd(), chunk.getTopFilledSegment() + 15, chunk.getPos().getZEnd())) {
            BlockState state = chunk.getBlockState(pos);
            Block block = state.getBlock();
            inPlaceFix(chunk, state, pos, otherPos);

            int blockId = Registry.BLOCK.getId(block) & 4095;
            if (ChunkPalettedStorageFixAccessor.getBlocksNeedingSideUpdate().get(blockId)) {
                boolean west = (pos.getX() & 15) == 0;
                boolean east = (pos.getX() & 15) == 15;
                boolean north = (pos.getZ() & 15) == 0;
                boolean south = (pos.getZ() & 15) == 15;
                if (north) {
                    if (east) {
                        sidesToUpgrade |= 2;
                    } else if (west) {
                        sidesToUpgrade |= 128;
                    } else {
                        sidesToUpgrade |= 1;
                    }
                } else if (south) {
                    if (west) {
                        sidesToUpgrade |= 32;
                    } else if (east) {
                        sidesToUpgrade |= 8;
                    } else {
                        sidesToUpgrade |= 16;
                    }
                } else if (east) {
                    sidesToUpgrade |= 4;
                } else if (west) {
                    sidesToUpgrade |= 64;
                } else {
                    centerIndicesToUpgrade.add(pos.getY() << 8 | (pos.getZ() & 15) << 4 | (pos.getX() & 15));
                }
            }
        }

        if (centerIndicesToUpgrade.isEmpty() && sidesToUpgrade == 0)
            return null;

        CompoundNBT upgradeData = new CompoundNBT();
        upgradeData.putInt("Sides", sidesToUpgrade);
        CompoundNBT centerIndices = new CompoundNBT();
        centerIndicesToUpgrade.forEach((IntConsumer) index -> {
            int low = index & 4095;
            int high = index >>> 12;
            INBT tag = centerIndices.get(String.valueOf(high));
            if (tag == null)
                centerIndices.put(String.valueOf(high), tag = new ListNBT());
            ((ListNBT) tag).add(IntNBT.valueOf(low));
        });
        for (String key : centerIndices.keySet()) {
            //noinspection ConstantConditions
            centerIndices.put(key, new IntArrayNBT(((ListNBT) centerIndices.get(key)).stream().mapToInt(val -> ((IntNBT) val).getInt()).toArray()));
        }
        upgradeData.put("Indices", centerIndices);
        return new UpgradeData(upgradeData);
    }

    private static void inPlaceFix(IChunk chunk, BlockState oldState, BlockPos pos, BlockPos.Mutable otherPos) {
        Block block = oldState.getBlock();
        if (block instanceof SnowyDirtBlock) {
            Block above = chunk.getBlockState(otherPos.setPos(pos).offset(Direction.UP)).getBlock();
            if (above == SNOW || above == SNOW_BLOCK)
                chunk.setBlockState(pos, oldState.with(SnowyDirtBlock.SNOWY, true), false);
        } else if (block instanceof DoorBlock && oldState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            otherPos.setPos(pos).offset(Direction.UP);
            BlockState above = chunk.getBlockState(otherPos);
            if (above.getBlock() instanceof DoorBlock) {
                chunk.setBlockState(pos, oldState.with(DoorBlock.HINGE, above.get(DoorBlock.HINGE)).with(DoorBlock.POWERED, above.get(DoorBlock.POWERED)), false);
                chunk.setBlockState(otherPos, above.with(DoorBlock.FACING, oldState.get(DoorBlock.FACING)).with(DoorBlock.OPEN, oldState.get(DoorBlock.OPEN)), false);
            }
        } else if (block instanceof DoublePlantBlock && oldState.get(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = chunk.getBlockState(otherPos.setPos(pos).offset(Direction.DOWN));
            if (below.getBlock() instanceof DoublePlantBlock)
                chunk.setBlockState(pos, below.with(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), false);
        }
    }

    public static void fix(IWorld world, BlockPos pos, int flags) {
        doFix(world, pos, flags);
        for (Direction dir : Direction.values())
            doFix(world, pos.offset(dir), flags);
    }

    private static void doFix(IWorld world, BlockPos pos, int flags) {
        BlockState state = world.getBlockState(pos);
        for (Direction dir : Direction.values()) {
            BlockPos otherPos = pos.offset(dir);
            state = applyAdjacentBlock(state, dir, world, pos, otherPos);
        }
        world.setBlockState(pos, state, flags | 16);

        inPlaceFix(world.getChunk(pos), state, pos, new BlockPos.Mutable());
    }

    private static BlockState applyAdjacentBlock(BlockState state, Direction dir, IWorld world, BlockPos pos, BlockPos otherPos) {
        Block block = state.getBlock();
        if (block instanceof DoorBlock || block instanceof DoublePlantBlock)
            return state;

        if (block == CHEST || block == TRAPPED_CHEST) {
            BlockState otherState = world.getBlockState(otherPos);
            if (dir.getAxis().isHorizontal()) {
                Direction chestFacing = state.get(ChestBlock.FACING);
                ChestType currentType = state.get(ChestBlock.TYPE);
                ChestType correctDoubleType = dir == chestFacing.rotateY() ? ChestType.LEFT : ChestType.RIGHT;
                if (dir.getAxis() != chestFacing.getAxis()) {
                    if (block == otherState.getBlock()) {
                        if (currentType == ChestType.SINGLE && chestFacing == otherState.get(ChestBlock.FACING)) {
                            return state.with(ChestBlock.TYPE, correctDoubleType);
                        }
                    } else if (currentType == correctDoubleType) {
                        return state.with(ChestBlock.TYPE, ChestType.SINGLE);
                    }
                }
            }
            return state;
        }

        if (block instanceof BedBlock) {
            BedPart part = state.get(BedBlock.PART);
            Direction facing = state.get(BedBlock.HORIZONTAL_FACING);
            if (dir == (part == BedPart.FOOT ? facing : facing.getOpposite())) {
                BlockState otherState = world.getBlockState(otherPos);
                if (otherState.getBlock() == block && otherState.get(BedBlock.PART) != part) {
                    return state.with(BedBlock.OCCUPIED, otherState.get(BedBlock.OCCUPIED));
                }
            }
            return state;
        }

        return UpgradeDataAccessor.callFunc_196987_a(state, dir, world, pos, otherPos);
    }
}
