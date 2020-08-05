package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicRegistryManager.class)
public interface DynamicRegistryManagerAccessor {
    @Accessor("INFOS")
    static Map<RegistryKey<? extends Registry<?>>, DynamicRegistryManager.Info<?>> getInfos() {
        return MixinHelper.fakeInstance();
    }
}
