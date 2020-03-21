package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.OptionalInt;

@Mixin(FireworkRocketEntity.class)
public interface FireworkEntityAccessor {
    @Accessor("SHOOTER_ENTITY_ID")
    static TrackedData<OptionalInt> getShooter() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("SHOT_AT_ANGLE")
    static TrackedData<Boolean> getShotAtAngle() {
        return MixinHelper.fakeInstance();
    }
}
