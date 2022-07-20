package net.earthcomputer.multiconnect.compat.mixin.fapi;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(value = MappedRegistry.class, priority = 2000)
public class MappedRegistryMixin {
    @Shadow(remap = false)
    @Final
    @Mutable
    private static Set<String> VANILLA_NAMESPACES;

    static {
        var builder = ImmutableSet.<String>builder();
        builder.addAll(VANILLA_NAMESPACES);
        builder.add("multiconnect");
        VANILLA_NAMESPACES = builder.build();
    }
}
