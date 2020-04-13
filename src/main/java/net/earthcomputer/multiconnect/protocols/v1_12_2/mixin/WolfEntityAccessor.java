package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WolfEntity.class)
public interface WolfEntityAccessor {
    @Accessor("COLLAR_COLOR")
    static DataParameter<Integer> getCollarColor() {
        return MixinHelper.fakeInstance();
    }
}
