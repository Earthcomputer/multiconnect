package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.OldLanguageManager;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {

    @Inject(method = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", remap = false),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onLoad(ResourceManager resourceManager, List<LanguageDefinition> languages, CallbackInfoReturnable<TranslationStorage> ci, Map<String, String> translations) {
        OldLanguageManager.addExtraTranslations(languages.get(languages.size() - 1).getCode(), translations::put);
    }

}
