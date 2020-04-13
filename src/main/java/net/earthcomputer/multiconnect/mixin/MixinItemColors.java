package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemColors.class)
public abstract class MixinItemColors {

    /*@Redirect(method = "getColorMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private <T> int redirectGetRawId(DefaultedRegistry<T> registry, T value) {
        return AbstractProtocol.getUnmodifiedId(registry, value);
    }*/

}
