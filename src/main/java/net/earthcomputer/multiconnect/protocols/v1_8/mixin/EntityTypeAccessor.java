package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityType.class)
public interface EntityTypeAccessor {
    @Mutable
    @Accessor
    void setDimensions(EntityDimensions dimensions);
}
