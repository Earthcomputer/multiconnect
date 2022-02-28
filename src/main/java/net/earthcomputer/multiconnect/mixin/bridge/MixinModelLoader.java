package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.minecraft.block.Block;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;
import java.util.Set;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getIds()Ljava/util/Set;"))
    private Set<Identifier> redirectGetIds(DefaultedRegistry<?> registry) {
        Registry<?> defaultRegistry = DefaultRegistries.getDefaultRegistry(registry.getKey());
        return defaultRegistry != null ? defaultRegistry.getIds() : registry.getIds();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private Iterator<Block> redirectBlockRegistryIterator(DefaultedRegistry<Block> registry) {
        return DefaultRegistries.getDefaultRegistry(registry.getKey()).iterator();
    }

    @Redirect(method = "method_4736", remap = false, at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/util/registry/DefaultedRegistry;get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;"))
    private static <T> T redirectGet(DefaultedRegistry<T> registry, Identifier id) {
        Registry<T> defaultRegistry = DefaultRegistries.getDefaultRegistry(registry.getKey());
        return defaultRegistry != null ? defaultRegistry.get(id) : registry.get(id);
    }

}
