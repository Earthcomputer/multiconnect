package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(DynamicRegistryManager.ImmutableImpl.class)
public interface DynamicRegistryManagerImmutableImplAccessor {
    @Accessor
    Map<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> getRegistries();

    @Mutable
    @Accessor
    void setRegistries(Map<? extends RegistryKey<? extends Registry<?>>, ? extends Registry<?>> registries);
}
