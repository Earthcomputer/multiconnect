package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.OptionalInt;

@Mixin(FireworkRocketEntity.class)
public interface FireworkRocketEntityAccessor {
    @Accessor("DATA_ATTACHED_TO_TARGET")
    static EntityDataAccessor<OptionalInt> getDataAttachedToTarget() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("DATA_SHOT_AT_ANGLE")
    static EntityDataAccessor<Boolean> getDataShotAtAngle() {
        return MixinHelper.fakeInstance();
    }
}
