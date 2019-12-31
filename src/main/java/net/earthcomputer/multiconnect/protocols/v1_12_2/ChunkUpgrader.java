package net.earthcomputer.multiconnect.protocols.v1_12_2;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.block.*;
import net.minecraft.datafixer.fix.ChunkPalettedStorageFix;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.IntConsumer;

import static net.minecraft.block.Blocks.*;

public class ChunkUpgrader {

    private static final BitSet BLOCKS_NEEDING_SIDE_UPDATE;
    static {
        try {
            Field field = Arrays.stream(ChunkPalettedStorageFix.class.getDeclaredFields())
                    .filter(f -> f.getType() == BitSet.class)
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            field.setAccessible(true);
            BLOCKS_NEEDING_SIDE_UPDATE = (BitSet) field.get(null);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public static UpgradeData fixChunk(WorldChunk chunk) {
        IntList centerIndicesToUpgrade = new IntArrayList();
        int sidesToUpgrade = 0;

        BlockPos.Mutable otherPos = new BlockPos.Mutable();
        for (BlockPos pos : BlockPos.iterate(chunk.getPos().getStartX(), 0, chunk.getPos().getStartZ(),
                chunk.getPos().getEndX(), chunk.getHighestNonEmptySectionYOffset() + 15, chunk.getPos().getEndZ())) {
            BlockState state = chunk.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof SnowyBlock) {
                Block above = chunk.getBlockState(otherPos.set(pos).setOffset(Direction.UP)).getBlock();
                if (above == SNOW || above == SNOW_BLOCK)
                    chunk.setBlockState(pos, state.with(SnowyBlock.SNOWY, true), false);
            }

            int blockId = Registry.BLOCK.getRawId(block) & 4095;
            if (BLOCKS_NEEDING_SIDE_UPDATE.get(blockId)) {
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
}
