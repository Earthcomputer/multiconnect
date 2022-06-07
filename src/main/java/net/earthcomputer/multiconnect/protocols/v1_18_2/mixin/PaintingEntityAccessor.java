package net.earthcomputer.multiconnect.protocols.v1_18_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.util.registry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PaintingEntity.class)
public interface PaintingEntityAccessor {
    @Accessor("VARIANT")
    static TrackedData<RegistryEntry<PaintingVariant>> getVariant() {
        return MixinHelper.fakeInstance();
    }
}
