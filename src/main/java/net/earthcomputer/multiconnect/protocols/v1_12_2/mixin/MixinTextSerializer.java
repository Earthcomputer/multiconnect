package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.StringTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.minecraft.util.text.ITextComponent$Serializer")
public class MixinTextSerializer {
    @Redirect(method = "fromJson(Ljava/lang/String;)Lnet/minecraft/util/text/ITextComponent;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/JSONUtils;fromJson(Lcom/google/gson/Gson;Ljava/lang/String;Ljava/lang/Class;Z)Ljava/lang/Object;"))
    private static <T> T supportForNonJSONStrings(Gson gson, String content, Class<T> var2, boolean lenient) {
        Object toReturn;
        try {
            toReturn = JSONUtils.fromJson(gson,content,var2, lenient);
        } catch (JsonSyntaxException jsonException) {
            if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
                toReturn = new StringTextComponent(content);
            } else throw jsonException;
        }
        return (T) toReturn;
    }
}
