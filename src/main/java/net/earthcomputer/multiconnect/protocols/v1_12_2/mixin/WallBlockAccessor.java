package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WallBlock.class)
public interface WallBlockAccessor {
    @Invoker
    boolean callShouldConnectTo(BlockState state, boolean faceFullSquare, Direction side);
}
