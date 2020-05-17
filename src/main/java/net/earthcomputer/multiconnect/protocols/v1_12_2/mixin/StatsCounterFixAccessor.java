package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.datafixer.fix.StatsCounterFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(StatsCounterFix.class)
public interface StatsCounterFixAccessor {
    @Accessor("RENAMED_ENTITIES")
    static Map<String, String> getRenamedEntities() {
        return MixinHelper.fakeInstance();
    }
}
