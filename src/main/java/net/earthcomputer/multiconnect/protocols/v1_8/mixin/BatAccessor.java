package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.ambient.Bat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Bat.class)
public interface BatAccessor {
    @Accessor("DATA_ID_FLAGS")
    static EntityDataAccessor<Byte> getDataIdFlags() {
        return MixinHelper.fakeInstance();
    }
}
