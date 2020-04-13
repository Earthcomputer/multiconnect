package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TridentEntity.class)
public interface TridentEntityAccessor {

    @Accessor("field_226571_aq_")
    static DataParameter<Boolean> getHasEnchantmentGlint() {
        return MixinHelper.fakeInstance();
    }

}
