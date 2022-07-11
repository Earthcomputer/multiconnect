package net.earthcomputer.multiconnect.protocols.v1_15.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.util.datafix.fixes.AttributesRename;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributesRename.class)
public interface AttributesRenameAccessor {
    @Accessor("RENAMES")
    static Map<String, String> getRenames() {
        return MixinHelper.fakeInstance();
    }
}
