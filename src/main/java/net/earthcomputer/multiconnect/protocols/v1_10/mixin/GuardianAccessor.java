package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Guardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Guardian.class)
public interface GuardianAccessor {
    @Accessor("DATA_ID_MOVING")
    static EntityDataAccessor<Boolean> getDataIdMoving() {
        return MixinHelper.fakeInstance();
    }
}
