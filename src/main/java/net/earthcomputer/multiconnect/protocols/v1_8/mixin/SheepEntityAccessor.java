package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SheepEntity.class)
public interface SheepEntityAccessor {
    @Accessor("COLOR")
    static TrackedData<Byte> getColor() {
        return MixinHelper.fakeInstance();
    }
}
