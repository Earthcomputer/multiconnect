package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.tag.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;

@Mixin(targets = "net.minecraft.tag.GlobalTagAccessor$CachedTag")
public class MixinCachedTag<T> {

    @Shadow private Tag<T> currentTag;

    @Inject(method = "get()Lnet/minecraft/tag/Tag;", at = @At("HEAD"))
    private void onGet(CallbackInfoReturnable<Tag<T>> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2 && currentTag == null) {
            currentTag = Tag.of(Collections.emptySet());
        }
    }

}
