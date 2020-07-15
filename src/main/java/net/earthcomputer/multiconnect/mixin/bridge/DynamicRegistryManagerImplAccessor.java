package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicRegistryManager.Impl.class)
public interface DynamicRegistryManagerImplAccessor {
    @Accessor
    Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> getRegistries();

    @Mutable
    @Accessor
    void setRegistries(Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> registries);
}
