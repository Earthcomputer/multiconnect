package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneWireBlockAccessor {
    @Invoker
    static boolean callConnectsTo(BlockState state, Direction dir) {
        throw new UnsupportedOperationException();
    }
}
