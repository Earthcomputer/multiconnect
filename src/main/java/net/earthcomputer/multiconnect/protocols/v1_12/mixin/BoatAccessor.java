package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.vehicle.Boat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Boat.class)
public interface BoatAccessor {
    @Accessor("DATA_ID_BUBBLE_TIME")
    static EntityDataAccessor<Integer> getDataIdBubbleTime() {
        return MixinHelper.fakeInstance();
    }
}
