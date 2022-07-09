package net.earthcomputer.multiconnect.mixin.bridge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

@Mixin(RegistryAccess.ImmutableRegistryAccess.class)
public interface ImmutableRegistryAccessAccessor {
    @Accessor
    Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> getRegistries();

    @Mutable
    @Accessor
    void setRegistries(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries);
}
