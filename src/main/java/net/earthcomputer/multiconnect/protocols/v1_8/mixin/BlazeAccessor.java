package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Blaze;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Blaze.class)
public interface BlazeAccessor {
    @Accessor("DATA_FLAGS_ID")
    static EntityDataAccessor<Byte> getDataFlagsId() {
        return MixinHelper.fakeInstance();
    }
}
