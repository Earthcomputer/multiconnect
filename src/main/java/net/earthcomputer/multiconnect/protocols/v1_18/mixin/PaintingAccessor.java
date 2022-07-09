package net.earthcomputer.multiconnect.protocols.v1_18.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Painting.class)
public interface PaintingAccessor {
    @Accessor("DATA_PAINTING_VARIANT_ID")
    static EntityDataAccessor<Holder<PaintingVariant>> getDataPaintingVariantId() {
        return MixinHelper.fakeInstance();
    }
}
