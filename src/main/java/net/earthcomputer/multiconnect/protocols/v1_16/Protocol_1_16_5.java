package net.earthcomputer.multiconnect.protocols.v1_16;

import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Protocol_1_16_5 extends Protocol_1_17 {
    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0;
        }
        return super.getBlockDestroySpeed(state, destroySpeed);
    }

    @Override
    public float getBlockExplosionResistance(Block block, float resistance) {
        if (block instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockExplosionResistance(block, resistance);
    }
}
