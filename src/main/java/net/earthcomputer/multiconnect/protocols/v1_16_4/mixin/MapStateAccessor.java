package net.earthcomputer.multiconnect.protocols.v1_16_4.mixin;

import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapState.class)
public interface MapStateAccessor {
    @Accessor
    boolean isShowIcons();

    @Mutable
    @Accessor
    void setShowIcons(boolean showIcons);
}
