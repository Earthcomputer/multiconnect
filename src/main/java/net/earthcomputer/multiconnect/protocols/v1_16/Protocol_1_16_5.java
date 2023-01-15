package net.earthcomputer.multiconnect.protocols.v1_16;

import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class Protocol_1_16_5 extends ProtocolBehavior {
    @Override
    @Nullable
    public Float getDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0f;
        }
        return null;
    }

    @Override
    @Nullable
    public Float getExplosionResistance(Block block, float resistance) {
        if (block instanceof InfestedBlock) {
            return 0.75f;
        }
        return null;
    }
}
