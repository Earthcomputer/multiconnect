package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UpgradeData.class)
public interface UpgradeDataAccessor {

    @Invoker
    static BlockState callFunc_196987_a(BlockState oldState, Direction dir, IWorld world, BlockPos currentPos, BlockPos otherPos) {
        return MixinHelper.fakeInstance();
    }

}
