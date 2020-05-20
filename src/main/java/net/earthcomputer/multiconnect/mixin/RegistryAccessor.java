package net.earthcomputer.multiconnect.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Registry.class)
public interface RegistryAccessor<T> {
    @Accessor("registryKey")
    RegistryKey<Registry<T>> multiconnect_getRegistryKey();

    @Accessor
    Lifecycle getLifecycle();
}
