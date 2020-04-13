package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockColors.class)
public abstract class MixinBlockColors {

    /*@Redirect(method = "getColorOrMaterialColor(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private <T> int redirectGetRawId1(DefaultedRegistry<T> registry, T value) {
        return AbstractProtocol.getUnmodifiedId(registry, value);
    }

    @Redirect(method = "getColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private <T> int redirectGetRawId2(DefaultedRegistry<T> registry, T value) {
        return AbstractProtocol.getUnmodifiedId(registry, value);
    }*/

}
