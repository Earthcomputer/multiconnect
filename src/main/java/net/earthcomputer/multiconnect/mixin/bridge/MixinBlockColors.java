package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockColors.class)
public abstract class MixinBlockColors {

    @Redirect(method = "getColor(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private <T> int redirectGetRawId1(DefaultedRegistry<T> registry, T value) {
        return Utils.getUnmodifiedId(registry, value);
    }

    @Redirect(method = "getColor(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;I)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private <T> int redirectGetRawId2(DefaultedRegistry<T> registry, T value) {
        return Utils.getUnmodifiedId(registry, value);
    }

}
