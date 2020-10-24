package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.GuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuardianEntity.class)
public interface GuardianEntityAccessor {
    @Accessor("BEAM_TARGET_ID")
    static TrackedData<Integer> getBeamTargetId() {
        return MixinHelper.fakeInstance();
    }
}
