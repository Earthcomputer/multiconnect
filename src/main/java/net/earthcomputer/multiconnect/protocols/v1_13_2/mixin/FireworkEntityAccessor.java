package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.OptionalInt;

@Mixin(FireworkRocketEntity.class)
public interface FireworkEntityAccessor {
    @Accessor("BOOSTED_ENTITY_ID")
    static DataParameter<OptionalInt> getShooter() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("field_213895_d")
    static DataParameter<Boolean> getShotAtAngle() {
        return MixinHelper.fakeInstance();
    }
}
