package net.earthcomputer.multiconnect.protocols.v1_18.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.animal.Cat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Cat.class)
public interface CatAccessor {
    @Accessor("DATA_VARIANT_ID")
    static EntityDataAccessor<Integer> getDataVariantId() {
        return MixinHelper.fakeInstance();
    }
}
