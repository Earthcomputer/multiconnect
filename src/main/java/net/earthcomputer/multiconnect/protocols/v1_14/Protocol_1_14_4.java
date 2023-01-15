package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class Protocol_1_14_4 extends ProtocolBehavior {
    @Override
    @Nullable
    public Float getDestroySpeed(BlockState state, float destroySpeed) {
        Block block = state.getBlock();
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            return 0.8f;
        }
        return null;
    }

    @Override
    @Nullable
    public Float getExplosionResistance(Block block, float resistance) {
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            return 0.8f;
        }
        return null;
    }
}
