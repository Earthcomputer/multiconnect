package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractPiglin.class)
public interface AbstractPiglinAccessor {
    @Accessor("DATA_IMMUNE_TO_ZOMBIFICATION")
    static EntityDataAccessor<Boolean> getDataImmuneToZombification() {
        return MixinHelper.fakeInstance();
    }
}
