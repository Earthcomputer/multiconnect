package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PersistentProjectileEntity.class)
public interface ProjectileEntityAccessor {
    @Accessor("PIERCE_LEVEL")
    static TrackedData<Byte> getPierceLevel() {
        return MixinHelper.fakeInstance();
    }
}
