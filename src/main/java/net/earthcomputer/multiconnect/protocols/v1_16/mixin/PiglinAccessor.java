package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.piglin.Piglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Piglin.class)
public interface PiglinAccessor {
    @Accessor("DATA_IS_CHARGING_CROSSBOW")
    static EntityDataAccessor<Boolean> getDataIsChargingCrossbow() {
        return MixinHelper.fakeInstance();
    }
}
