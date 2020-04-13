package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("BED_POSITION")
    static DataParameter<Optional<BlockPos>> getSleepingPosition() {
        return MixinHelper.fakeInstance();
    }
}
