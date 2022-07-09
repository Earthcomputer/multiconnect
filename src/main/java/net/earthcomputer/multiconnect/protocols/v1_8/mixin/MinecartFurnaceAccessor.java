package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecartFurnace.class)
public interface MinecartFurnaceAccessor {
    @Accessor("DATA_ID_FUEL")
    static EntityDataAccessor<Boolean> getDataIdFuel() {
        return MixinHelper.fakeInstance();
    }
}
