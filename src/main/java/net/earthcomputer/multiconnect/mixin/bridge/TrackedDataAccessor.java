package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.network.syncher.EntityDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityDataAccessor.class)
public interface TrackedDataAccessor {

    @Accessor
    @Mutable
    void setId(int id);

}
