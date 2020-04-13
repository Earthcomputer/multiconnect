package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.particles.IParticleData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AreaEffectCloudEntity.class)
public interface AreaEffectCloudEntityAccessor {
    @Accessor("PARTICLE")
    static DataParameter<IParticleData> getParticleId() {
        return MixinHelper.fakeInstance();
    }
}
