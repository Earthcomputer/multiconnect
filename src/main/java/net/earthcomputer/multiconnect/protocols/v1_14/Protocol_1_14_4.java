package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Protocol_1_14_4 extends Protocol_1_15 {
    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        destroySpeed = super.getBlockDestroySpeed(state, destroySpeed);
        Block block = state.getBlock();
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            destroySpeed = 0.8f;
        }
        return destroySpeed;
    }

    @Override
    public float getBlockExplosionResistance(Block block, float resistance) {
        resistance = super.getBlockExplosionResistance(block, resistance);
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            resistance = 0.8f;
        }
        return resistance;
    }
}
