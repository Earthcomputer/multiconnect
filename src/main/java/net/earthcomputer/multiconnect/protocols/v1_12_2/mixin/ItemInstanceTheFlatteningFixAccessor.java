package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.datafixer.fix.ItemInstanceTheFlatteningFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ItemInstanceTheFlatteningFix.class)
public interface ItemInstanceTheFlatteningFixAccessor {
    @Accessor("FLATTENING_MAP")
    static Map<String, String> getFlatteningMap() {
        return MixinHelper.fakeInstance();
    }
}
