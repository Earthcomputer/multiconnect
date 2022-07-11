package net.earthcomputer.multiconnect.protocols.v1_16.mixin;

import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MapItemSavedData.class)
public interface MapItemSavedDataAccessor {
    @Accessor
    boolean isTrackingPosition();

    @Mutable
    @Accessor
    void setTrackingPosition(boolean trackingPosition);
}
