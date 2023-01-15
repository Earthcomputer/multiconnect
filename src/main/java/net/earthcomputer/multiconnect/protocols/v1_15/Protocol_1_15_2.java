package net.earthcomputer.multiconnect.protocols.v1_15;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class Protocol_1_15_2 extends ProtocolBehavior {
    @Override
    @Nullable
    public Float getDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON || state.getBlock() == Blocks.PISTON_HEAD) {
            return 0.5f;
        }
        return null;
    }

    @Override
    @Nullable
    public Float getExplosionResistance(Block block, float resistance) {
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD) {
            return 0.5f;
        }
        return null;
    }
}
