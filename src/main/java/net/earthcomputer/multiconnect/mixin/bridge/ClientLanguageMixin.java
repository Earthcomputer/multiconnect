package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.AssetDownloader;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Inject(method = "loadFrom",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;copyOf(Ljava/util/Map;)Lcom/google/common/collect/ImmutableMap;", remap = false),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onLoadFrom(ResourceManager resourceManager, List<LanguageInfo> languages, CallbackInfoReturnable<ClientLanguage> ci, Map<String, String> translations) {
        AssetDownloader.addExtraTranslations(languages.get(languages.size() - 1).getCode(), translations::put);
    }

}
