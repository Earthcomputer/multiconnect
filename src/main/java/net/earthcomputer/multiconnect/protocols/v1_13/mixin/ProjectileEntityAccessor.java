package net.earthcomputer.multiconnect.protocols.v1_13.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;
import java.util.UUID;

@Mixin(ProjectileEntity.class)
public interface ProjectileEntityAccessor {

    @Accessor("OPTIONAL_UUID")
    static TrackedData<Optional<UUID>> getOptionalUuid() {
        return MixinHelper.fakeInstance();
    }

}
