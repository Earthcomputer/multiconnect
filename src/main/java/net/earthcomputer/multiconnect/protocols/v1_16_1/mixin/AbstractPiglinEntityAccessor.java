package net.earthcomputer.multiconnect.protocols.v1_16_1.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.class_5418;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_5418.class)
public interface AbstractPiglinEntityAccessor {
    @Accessor("field_25758")
    static TrackedData<Boolean> getImmuneToZombification() {
        return MixinHelper.fakeInstance();
    }
}
