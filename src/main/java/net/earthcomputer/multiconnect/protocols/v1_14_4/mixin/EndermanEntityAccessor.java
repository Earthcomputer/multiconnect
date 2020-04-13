package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EndermanEntity.class)
public interface EndermanEntityAccessor {

    @Accessor("field_226535_bx_")
    static DataParameter<Boolean> getHasScreamed() {
        return MixinHelper.fakeInstance();
    }

}
