package net.earthcomputer.multiconnect.protocols.v1_15;

import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Protocol_1_15_2 extends Protocol_1_16 {
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
