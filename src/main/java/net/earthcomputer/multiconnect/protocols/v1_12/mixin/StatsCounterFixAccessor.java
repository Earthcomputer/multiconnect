package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.util.datafix.fixes.StatsCounterFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StatsCounterFix.class)
public interface StatsCounterFixAccessor {
    @Accessor("ENTITIES")
    static Map<String, String> getEntities() {
        return MixinHelper.fakeInstance();
    }
}
