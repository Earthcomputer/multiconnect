package net.earthcomputer.multiconnect.protocols.v1_16_4.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DimensionType.class)
public interface DimensionTypeAccessor {
    @Accessor("OVERWORLD")
    static DimensionType getOverworld() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("THE_NETHER")
    static DimensionType getTheNether() {
        return MixinHelper.fakeInstance();
    }

    @Accessor("THE_END")
    static DimensionType getTheEnd() {
        return MixinHelper.fakeInstance();
    }
}
