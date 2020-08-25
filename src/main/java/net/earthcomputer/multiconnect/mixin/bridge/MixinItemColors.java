package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.item.Item;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemColors.class)
public abstract class MixinItemColors {

    @Redirect(method = "getColorMultiplier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;getRawId(Ljava/lang/Object;)I"))
    private <T> int redirectGetRawId1(DefaultedRegistry<T> registry, T value) {
        return Utils.getUnmodifiedId(registry, value);
    }

    @Redirect(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getRawId(Lnet/minecraft/item/Item;)I"))
    private int redirectGetRawId2(Item item) {
        return Utils.getUnmodifiedId(Registry.ITEM, item);
    }

}
