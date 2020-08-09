package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.resource.ServerResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerResourceManager.class)
public class MixinMinecraftServer {

    @Inject(method = "reload", at = @At("RETURN"))
    private static void onReloadDataPacks(CallbackInfoReturnable<CompletableFuture<ServerResourceManager>> ci) {
        ci.getReturnValue().whenComplete((resourceManager, throwable) -> ConnectionInfo.stopReloadingResources());
    }

}
