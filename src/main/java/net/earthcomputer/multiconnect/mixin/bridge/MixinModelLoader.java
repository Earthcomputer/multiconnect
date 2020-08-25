package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getIds()Ljava/util/Set;"))
    private Set<Identifier> redirectGetIds(DefaultedRegistry<?> registry) {
        DefaultRegistries<?> defaultRegistry = DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        return defaultRegistry != null ? defaultRegistry.defaultIdToEntry.keySet() : registry.getIds();
    }

    @SuppressWarnings({"unchecked", "UnresolvedMixinReference"})
    @Redirect(method = "method_4736", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;get(Lnet/minecraft/util/Identifier;)Ljava/lang/Object;"))
    private static <T> T redirectGet(DefaultedRegistry<T> registry, Identifier id) {
        DefaultRegistries<T> defaultRegistry = (DefaultRegistries<T>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        return defaultRegistry != null ? defaultRegistry.defaultIdToEntry.get(id) : registry.get(id);
    }

}
