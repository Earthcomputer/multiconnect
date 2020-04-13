package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.UUID;

@Mixin(AbstractArrowEntity.class)
public interface ProjectileEntityAccessor {

    @Accessor("field_212362_a")
    static DataParameter<Optional<UUID>> getOptionalUuid() {
        return MixinHelper.fakeInstance();
    }

}
