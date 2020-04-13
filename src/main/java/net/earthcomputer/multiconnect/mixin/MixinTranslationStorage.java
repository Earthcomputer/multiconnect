package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.OldLanguageManager;
import net.minecraft.client.resources.Locale;
import net.minecraft.resources.IResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(Locale.class)
public class MixinTranslationStorage {

    @Shadow @Final protected Map<String, String> properties;

    @Inject(method = "func_195811_a", at = @At("RETURN"))
    private void onLoad(IResourceManager resourceManager, List<String> languages, CallbackInfo ci) {
        OldLanguageManager.addExtraTranslations(languages.get(languages.size() - 1), properties::put);
    }

}
