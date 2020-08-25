package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;

@Mixin(StatusEffectSpriteManager.class)
public class MixinStatusEffectSpriteManager {

    @Redirect(method = "getSprites", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/Registry;getIds()Ljava/util/Set;"))
    private Set<Identifier> redirectGetIds(Registry<?> registry) {
        DefaultRegistries<?> defaultRegistry = DefaultRegistries.DEFAULT_REGISTRIES.get(registry);
        return defaultRegistry != null ? defaultRegistry.defaultIdToEntry.keySet() : registry.getIds();
    }

}
