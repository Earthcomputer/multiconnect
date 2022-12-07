package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public class CreativeModeTabItemDisplayBuilderMixin {
    @Redirect(method = "accept", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private boolean modifyIsEnabled(Item item, FeatureFlagSet featureFlags) {
        if (!MultiConnectAPI.instance().doesServerKnow(BuiltInRegistries.ITEM, item)) {
            return false;
        }
        return item.isEnabled(featureFlags);
    }
}
