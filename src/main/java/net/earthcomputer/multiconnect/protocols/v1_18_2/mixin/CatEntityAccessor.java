package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.CatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CatEntity.class)
public interface CatEntityAccessor {
    @Accessor("CAT_VARIANT")
    static TrackedData<Integer> getCatVariant() {
        return MixinHelper.fakeInstance();
    }
}
