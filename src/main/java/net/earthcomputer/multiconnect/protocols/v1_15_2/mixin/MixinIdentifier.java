package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Identifier.class)
public abstract class MixinIdentifier {

    private static boolean isPathValid(String path) {
        return path.chars().allMatch((c) -> c == 95 || c == 45 || c >= 97 && c <= 122 || c >= 48 && c <= 57 || c == 47 || c == 46);
    }

    @ModifyVariable(method = "<init>([Ljava/lang/String;)V", at = @At("HEAD"))
    private static String[] modifyEntry(String[] id) {
        String namespace = StringUtils.isEmpty(id[0]) ? "minecraft" : id[0];
        String path = id[1];

        if (!isPathValid(path) && (path.contains("generic.") || path.contains("horse."))) {
            path = path.replaceAll("([A-Z])", "_$1").toLowerCase();
            id[1] = path;
        }

        return id;
    }

}
