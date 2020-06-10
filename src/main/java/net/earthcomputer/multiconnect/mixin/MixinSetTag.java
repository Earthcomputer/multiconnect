package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.class_5394;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.Set;

// Temporary hack fix for https://bugs.mojang.com/browse/MC-188517
// TODO: remove when this bug is fixed
@Mixin(class_5394.class)
public class MixinSetTag {
    @Invoker("<init>")
    private static <T> class_5394<T> createSetTag(Set<T> set, Class<?> clazz) {
        return MixinHelper.fakeInstance();
    }

    @Inject(method = "method_29898", at = @At("HEAD"), cancellable = true)
    private static <T> void onGetEmptyTag(CallbackInfoReturnable<class_5394<T>> ci) {
        ci.setReturnValue(createSetTag(Collections.emptySet(), Object.class));
    }
}
