package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.datafixer.fix.RenameItemStackAttributesFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenameItemStackAttributesFix.class)
public interface RenameItemStackAttributesFixAccessor {
    @Accessor("RENAMES")
    static Map<String, String> getRenames() {
        return MixinHelper.fakeInstance();
    }
}
