package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ServersExt;
import net.minecraft.client.multiplayer.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerList.class)
public class MixinServerList {

    @Inject(method = "saveServerList", at = @At("HEAD"))
    private void onSaveFile(CallbackInfo ci) {
        ServersExt.save();
    }

}
