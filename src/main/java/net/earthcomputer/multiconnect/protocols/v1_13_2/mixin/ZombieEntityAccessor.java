package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ZombieEntity.class)
public interface ZombieEntityAccessor {
    @Accessor("DROWNING")
    static DataParameter<Boolean> getConvertingInWater() {
        return MixinHelper.fakeInstance();
    }
}
