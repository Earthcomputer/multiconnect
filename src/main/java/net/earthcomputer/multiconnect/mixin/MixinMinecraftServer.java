package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {

    @Inject(method = "loadDataPacks(Lnet/minecraft/world/storage/WorldInfo;)V", at = @At("RETURN"))
    private void onReloadDataPacks(WorldInfo levelProperties, CallbackInfo ci) {
        ConnectionInfo.stopReloadingResources();
    }

}
