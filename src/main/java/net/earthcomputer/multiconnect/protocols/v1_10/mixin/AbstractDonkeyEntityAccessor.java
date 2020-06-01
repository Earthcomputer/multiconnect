package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractDonkeyEntity.class)
public interface AbstractDonkeyEntityAccessor {
    @Accessor("CHEST")
    static TrackedData<Boolean> getChest() {
        return MixinHelper.fakeInstance();
    }
}
