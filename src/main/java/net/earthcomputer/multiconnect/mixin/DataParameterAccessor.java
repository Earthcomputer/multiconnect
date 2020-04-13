package net.earthcomputer.multiconnect.mixin;

import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DataParameter.class)
public interface DataParameterAccessor {

    @Accessor
    @Mutable
    void setId(int id);

}
