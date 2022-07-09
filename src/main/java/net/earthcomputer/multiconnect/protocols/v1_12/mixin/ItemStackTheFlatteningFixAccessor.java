package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.util.datafix.fixes.ItemStackTheFlatteningFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemStackTheFlatteningFix.class)
public interface ItemStackTheFlatteningFixAccessor {
    @Accessor("MAP")
    static Map<String, String> getMap() {
        return MixinHelper.fakeInstance();
    }
}
