package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderMan.class)
public interface EnderManAccessor {

    @Accessor("DATA_STARED_AT")
    static EntityDataAccessor<Boolean> getDataStaredAt() {
        return MixinHelper.fakeInstance();
    }

}
