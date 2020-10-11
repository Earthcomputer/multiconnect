package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.block.Block;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.EnumMap;

public class ChunkConnector {
    private final WorldChunk chunk;
    private final BlockConnectionsWorldView worldView;
    private final BlockConnector connector;
    private final EnumMap<EightWayDirection, ShortSet> blocksNeedingUpdate;

    public ChunkConnector(WorldChunk chunk, BlockConnector connector, EnumMap<EightWayDirection, ShortSet> blocksNeedingUpdate) {
        this.chunk = chunk;
        this.worldView = new BlockConnectionsWorldView(chunk.getWorld());
        this.connector = connector;
        this.blocksNeedingUpdate = blocksNeedingUpdate;
    }

    public static EightWayDirection directionForPos(BlockPos pos) {
        int x = pos.getX() & 15;
        int z = pos.getZ() & 15;
        if (x == 0) {
            if (z == 0) {
                return EightWayDirection.NORTH_WEST;
            } else if (z == 15) {
                return EightWayDirection.SOUTH_WEST;
            } else {
                return EightWayDirection.WEST;
            }
        } else if (x == 15) {
            if (z == 0) {
                return EightWayDirection.NORTH_EAST;
            } else if (z == 15) {
                return EightWayDirection.SOUTH_EAST;
            } else {
                return EightWayDirection.EAST;
            }
        } else {
            if (z == 0) {
                return EightWayDirection.NORTH;
            } else if (z == 15) {
                return EightWayDirection.SOUTH;
            } else {
                return null;
            }
        }
    }

    public static short packLocalPos(BlockPos pos) {
        return (short) ((pos.getX() & 15) | ((pos.getZ() & 15) << 4) | ((pos.getY() & 255) << 8));
    }

    public static int unpackLocalX(short packed) {
        return packed & 15;
    }

    public static int unpackLocalY(short packed) {
        return (packed >> 8) & 255;
    }

    public static int unpackLocalZ(short packed) {
        return (packed >> 4) & 15;
    }

    public void onNeighborChunkLoaded(Direction side) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (EightWayDirection dir : EightWayDirection.values()) {
            ShortSet set = blocksNeedingUpdate.get(dir);
            if (set != null && dir.getDirections().contains(side)) {
                ShortIterator itr = set.iterator();
                while (itr.hasNext()) {
                    short packed = itr.nextShort();
                    pos.set(chunk.getPos().getStartX() + unpackLocalX(packed), unpackLocalY(packed), chunk.getPos().getStartZ() + unpackLocalZ(packed));
                    updateBlock(pos, worldView.getBlockState(pos).getBlock(), false);
                }
                blocksNeedingUpdate.remove(dir);
            }
        }
    }

    public void onBlockChange(BlockPos pos, Block newBlock, boolean updateNeighbors) {
        if (updateBlock(pos, newBlock, true) || updateNeighbors) {
            for (Direction dir : Direction.values()) {
                BlockPos offsetPos = new BlockPos(
                        chunk.getPos().getStartX() + (pos.getX() & 15) + dir.getOffsetX(),
                        pos.getY() + dir.getOffsetY(),
                        chunk.getPos().getStartZ() + (pos.getZ() & 15) + dir.getOffsetZ());
                ChunkPos offsetChunkPos = new ChunkPos(offsetPos);
                Chunk offsetChunk = offsetChunkPos.equals(chunk.getPos()) ? chunk : chunk.getWorld().getChunk(offsetChunkPos.x, offsetChunkPos.z, ChunkStatus.FULL, false);
                if (offsetChunk != null) {
                    ((IBlockConnectableChunk) offsetChunk).multiconnect_getChunkConnector().onBlockChange(offsetPos, offsetChunk.getBlockState(offsetPos).getBlock(), false);
                }
            }
        }
    }

    public boolean updateBlock(BlockPos pos, Block newBlock, boolean checkForUpdateRemoval) {
        if (pos.getY() < 0 || pos.getY() > 255) {
            return false;
        }

        if (!checkForUpdateRemoval && !connector.shouldProcess(newBlock)) {
            return false;
        }

        EightWayDirection dir = directionForPos(pos);
        if (dir == null) {
            return connector.fix(worldView, pos, newBlock);
        }

        boolean needsNeighbors = connector.needsNeighbors(newBlock);
        if (needsNeighbors) {
            boolean allChunksLoaded = true;
            for (Direction offset : dir.getDirections()) {
                int chunkX = chunk.getPos().x + offset.getOffsetX();
                int chunkZ = chunk.getPos().z + offset.getOffsetZ();
                if (chunk.getWorld().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false) == null) {
                    allChunksLoaded = false;
                    break;
                }
            }
            if (allChunksLoaded) {
                needsNeighbors = false;
            }
        }

        if (!needsNeighbors) {
            ShortSet set = blocksNeedingUpdate.get(dir);
            if (set != null) {
                set.remove(packLocalPos(pos));
                if (set.isEmpty()) {
                    blocksNeedingUpdate.remove(dir);
                }
            }
            return connector.fix(worldView, pos, newBlock);
        } else {
            blocksNeedingUpdate.computeIfAbsent(dir, k -> new ShortOpenHashSet()).add(packLocalPos(pos));
            return false;
        }
    }
}
