package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModels.class)
public class MixinItemModels {

    @Inject(method = "getModelId", at = @At("HEAD"), cancellable = true)
    private static void getRawModelId(Item item, CallbackInfoReturnable<Integer> ci) {
        ci.setReturnValue(Utils.getUnmodifiedId(Registry.ITEM, item));
    }

}
