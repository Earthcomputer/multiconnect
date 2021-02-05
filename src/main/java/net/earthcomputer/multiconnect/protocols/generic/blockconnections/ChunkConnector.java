package net.earthcomputer.multiconnect.protocols.generic.blockconnections;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
    private final EnumMap<EightWayDirection, IntSet> blocksNeedingUpdate;

    public ChunkConnector(WorldChunk chunk, BlockConnector connector, EnumMap<EightWayDirection, IntSet> blocksNeedingUpdate) {
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

    public static int packLocalPos(int minY, BlockPos pos) {
        return (pos.getX() & 15) | ((pos.getZ() & 15) << 4) | (((pos.getY() - minY) & 2047) << 8);
    }

    private int packLocalPos(BlockPos pos) {
        return packLocalPos(worldView.getMinY(), pos);
    }

    private int unpackLocalX(int packed) {
        return packed & 15;
    }

    private int unpackLocalY(int packed) {
        return (packed >> 8) & 2047 + worldView.getMinY();
    }

    private int unpackLocalZ(int packed) {
        return (packed >> 4) & 15;
    }

    public void onNeighborChunkLoaded(Direction side) {
        BlockPos.Mutable pos = new BlockPos.Mutable();
        for (EightWayDirection dir : EightWayDirection.values()) {
            IntSet set = blocksNeedingUpdate.get(dir);
            if (set != null && dir.getDirections().contains(side)) {
                boolean allNeighborsLoaded = true;
                for (Direction requiredDir : dir.getDirections()) {
                    if (requiredDir != side && chunk.getWorld().getChunk(chunk.getPos().x + requiredDir.getOffsetX(), chunk.getPos().z + requiredDir.getOffsetZ(), ChunkStatus.FULL, false) == null) {
                        allNeighborsLoaded = false;
                        break;
                    }
                }

                if (allNeighborsLoaded) {
                    IntIterator itr = set.iterator();
                    while (itr.hasNext()) {
                        int packed = itr.nextInt();
                        pos.set(chunk.getPos().getStartX() + unpackLocalX(packed), unpackLocalY(packed), chunk.getPos().getStartZ() + unpackLocalZ(packed));
                        connector.fix(worldView, pos, worldView.getBlockState(pos).getBlock());
                    }
                    blocksNeedingUpdate.remove(dir);
                }
            }
        }
    }

    public void onBlockChange(BlockPos pos, Block newBlock, boolean updateNeighbors) {
        if (updateBlock(pos, newBlock) || updateNeighbors) {
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

    public boolean updateBlock(BlockPos pos, Block newBlock) {
        if (pos.getY() < worldView.getMinY() || pos.getY() > worldView.getMaxY()) {
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
            IntSet set = blocksNeedingUpdate.get(dir);
            if (set != null) {
                set.remove(packLocalPos(pos));
                if (set.isEmpty()) {
                    blocksNeedingUpdate.remove(dir);
                }
            }
            return connector.fix(worldView, pos, newBlock);
        } else {
            blocksNeedingUpdate.computeIfAbsent(dir, k -> new IntOpenHashSet()).add(packLocalPos(pos));
            return false;
        }
    }
}
