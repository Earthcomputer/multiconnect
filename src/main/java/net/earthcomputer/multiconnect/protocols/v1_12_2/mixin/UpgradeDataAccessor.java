package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UpgradeData.class)
public interface UpgradeDataAccessor {

    @Invoker
    static BlockState callApplyAdjacentBlock(BlockState oldState, Direction dir, IWorld world, BlockPos currentPos, BlockPos otherPos) {
        return MixinHelper.fakeInstance();
    }

}
