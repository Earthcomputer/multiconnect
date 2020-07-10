package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.class_5455;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(class_5455.class_5457.class)
public interface MutableDynamicRegistriesAccessor {
    @Accessor("field_25924")
    Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> getRegistries();

    @Mutable
    @Accessor("field_25924")
    void setRegistries(Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> registries);
}
