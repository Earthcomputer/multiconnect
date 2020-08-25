package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockModels.class)
public class MixinBlockModels {

    @Redirect(method = "getModelId(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/util/ModelIdentifier;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getId(Ljava/lang/Object;)Lnet/minecraft/util/Identifier;"))
    private static <T> Identifier redirectGetId(DefaultedRegistry<T> registry, T entry) {
        Identifier unmodifiedName = Utils.getUnmodifiedName(registry, entry);
        return unmodifiedName == null ? registry.getId(entry) : unmodifiedName;
    }

}
