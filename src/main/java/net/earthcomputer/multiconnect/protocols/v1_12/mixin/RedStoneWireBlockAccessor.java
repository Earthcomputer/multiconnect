package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RedStoneWireBlock.class)
public interface RedStoneWireBlockAccessor {
    @Invoker
    static boolean callShouldConnectTo(BlockState state, Direction dir) {
        throw new UnsupportedOperationException();
    }
}
