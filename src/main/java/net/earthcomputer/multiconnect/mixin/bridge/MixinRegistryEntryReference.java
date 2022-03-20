package net.earthcomputer.multiconnect.mixin.bridge;

import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryEntry.Reference.class)
public class MixinRegistryEntryReference<T> {
    @Shadow private @Nullable RegistryKey<T> registryKey;
    @Shadow private @Nullable T value;

    @Inject(method = "setKeyAndValue", at = @At("HEAD"), cancellable = true)
    private void onSetKeyAndValue(RegistryKey<T> key, T value, CallbackInfo ci) {
        // bypass some assertions in vanilla
        this.registryKey = key;
        this.value = value;
        ci.cancel();
    }
}
