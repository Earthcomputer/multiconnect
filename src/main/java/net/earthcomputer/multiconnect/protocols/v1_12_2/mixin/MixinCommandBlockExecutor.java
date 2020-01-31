package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import com.google.gson.JsonSyntaxException;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.CommandBlockExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CommandBlockExecutor.class)
public class MixinCommandBlockExecutor {
    @Redirect(method = "deserialize(Lnet/minecraft/nbt/CompoundTag;)V",at = @At(value = "INVOKE", target = "Lnet/minecraft/text/Text$Serializer;fromJson(Ljava/lang/String;)Lnet/minecraft/text/Text;"))
    private Text supportForNonJSONStrings(String json) {
        Text toReturn;
        try {
            toReturn = Text.Serializer.fromJson(json);
        } catch (JsonSyntaxException jsonException) {
            if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
                toReturn = new LiteralText(json);
            } else throw jsonException;
        }
        return toReturn;
    }
}
