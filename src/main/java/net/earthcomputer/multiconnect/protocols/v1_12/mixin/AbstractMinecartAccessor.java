package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecart.class)
public interface AbstractMinecartAccessor {
    @Accessor("DATA_ID_DISPLAY_BLOCK")
    static EntityDataAccessor<Integer> getDataIdDisplayBlock() {
        return MixinHelper.fakeInstance();
    }
}
