package net.earthcomputer.multiconnect.mixin;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {
    @Accessor("OVERWORLD")
    static DimensionType getOverworld() {
        throw new UnsupportedOperationException();
    }

    @Accessor("THE_NETHER")
    static DimensionType getTheNether() {
        throw new UnsupportedOperationException();
    }

    @Accessor("THE_END")
    static DimensionType getTheEnd() {
        throw new UnsupportedOperationException();
    }
}
