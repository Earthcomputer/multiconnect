package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.OldLanguageManager;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {

    @Shadow @Final protected Map<String, String> translations;

    @Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)V", at = @At("RETURN"))
    private void onLoad(ResourceManager resourceManager, List<String> languages, CallbackInfo ci) {
        OldLanguageManager.addExtraTranslations(languages.get(languages.size() - 1), translations, translations::put);
    }

}
