package net.earthcomputer.multiconnect.protocols.v1_12_2;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.ChunkPalettedStorageFixAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12_2.mixin.UpgradeDataAccessor;
import net.minecraft.block.*;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;

import java.util.function.IntConsumer;

import static net.minecraft.block.Blocks.*;

public class ChunkUpgrader {

    public static UpgradeData fixChunk(WorldChunk chunk) {
        IntList centerIndicesToUpgrade = new IntArrayList();
        int sidesToUpgrade = 0;

        BlockPos.Mutable otherPos = new BlockPos.Mutable();
        for (BlockPos pos : BlockPos.iterate(chunk.getPos().getStartX(), 0, chunk.getPos().getStartZ(),
                chunk.getPos().getEndX(), chunk.getHighestNonEmptySectionYOffset() + 15, chunk.getPos().getEndZ())) {
            BlockState state = chunk.getBlockState(pos);
            Block block = state.getBlock();
            inPlaceFix(chunk, state, pos, otherPos);

            int blockId = Registry.BLOCK.getRawId(block) & 4095;
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

        CompoundTag upgradeData = new CompoundTag();
        upgradeData.putInt("Sides", sidesToUpgrade);
        CompoundTag centerIndices = new CompoundTag();
        centerIndicesToUpgrade.forEach((IntConsumer) index -> {
            int low = index & 4095;
            int high = index >>> 12;
            Tag tag = centerIndices.get(String.valueOf(high));
            if (tag == null)
                centerIndices.put(String.valueOf(high), tag = new ListTag());
            ((ListTag) tag).add(IntTag.of(low));
        });
        for (String key : centerIndices.getKeys()) {
            //noinspection ConstantConditions
            centerIndices.put(key, new IntArrayTag(((ListTag) centerIndices.get(key)).stream().mapToInt(val -> ((IntTag) val).getInt()).toArray()));
        }
        upgradeData.put("Indices", centerIndices);
        return new UpgradeData(upgradeData);
    }

    private static void inPlaceFix(Chunk chunk, BlockState oldState, BlockPos pos, BlockPos.Mutable otherPos) {
        Block block = oldState.getBlock();
        if (block instanceof SnowyBlock) {
            Block above = chunk.getBlockState(otherPos.set(pos).setOffset(Direction.UP)).getBlock();
            if (above == SNOW || above == SNOW_BLOCK)
                chunk.setBlockState(pos, oldState.with(SnowyBlock.SNOWY, true), false);
        } else if (block instanceof DoorBlock && oldState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            otherPos.set(pos).setOffset(Direction.UP);
            BlockState above = chunk.getBlockState(otherPos);
            if (above.getBlock() instanceof DoorBlock) {
                chunk.setBlockState(pos, oldState.with(DoorBlock.HINGE, above.get(DoorBlock.HINGE)).with(DoorBlock.POWERED, above.get(DoorBlock.POWERED)), false);
                chunk.setBlockState(otherPos, above.with(DoorBlock.FACING, oldState.get(DoorBlock.FACING)).with(DoorBlock.OPEN, oldState.get(DoorBlock.OPEN)), false);
            }
        } else if (block instanceof TallPlantBlock && oldState.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = chunk.getBlockState(otherPos.set(pos).setOffset(Direction.DOWN));
            if (below.getBlock() instanceof TallPlantBlock)
                chunk.setBlockState(pos, below.with(TallPlantBlock.HALF, DoubleBlockHalf.UPPER), false);
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
        if (block instanceof DoorBlock || block instanceof TallPlantBlock)
            return state;

        if (block == CHEST || block == TRAPPED_CHEST) {
            BlockState otherState = world.getBlockState(otherPos);
            if (dir.getAxis().isHorizontal()) {
                Direction chestFacing = state.get(ChestBlock.FACING);
                ChestType currentType = state.get(ChestBlock.CHEST_TYPE);
                ChestType correctDoubleType = dir == chestFacing.rotateYClockwise() ? ChestType.LEFT : ChestType.RIGHT;
                if (dir.getAxis() != chestFacing.getAxis()) {
                    if (block == otherState.getBlock()) {
                        if (currentType == ChestType.SINGLE && chestFacing == otherState.get(ChestBlock.FACING)) {
                            return state.with(ChestBlock.CHEST_TYPE, correctDoubleType);
                        }
                    } else if (currentType == correctDoubleType) {
                        return state.with(ChestBlock.CHEST_TYPE, ChestType.SINGLE);
                    }
                }
            }
            return state;
        }

        if (block instanceof BedBlock) {
            BedPart part = state.get(BedBlock.PART);
            Direction facing = state.get(BedBlock.FACING);
            if (dir == (part == BedPart.FOOT ? facing : facing.getOpposite())) {
                BlockState otherState = world.getBlockState(otherPos);
                if (otherState.getBlock() == block && otherState.get(BedBlock.PART) != part) {
                    return state.with(BedBlock.OCCUPIED, otherState.get(BedBlock.OCCUPIED));
                }
            }
            return state;
        }

        return UpgradeDataAccessor.callApplyAdjacentBlock(state, dir, world, pos, otherPos);
    }
}
