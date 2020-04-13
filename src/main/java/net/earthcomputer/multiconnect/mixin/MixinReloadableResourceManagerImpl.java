package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.resources.IAsyncReloader;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SimpleReloadableResourceManager.class)
public class MixinReloadableResourceManagerImpl {

    @Inject(method = "reloadResources", at = @At("HEAD"))
    private void onResourceReload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, List<IResourcePack> packs, CallbackInfoReturnable<IAsyncReloader> ci) {
        ConnectionInfo.startReloadingResources();
        ProtocolRegistry.latest().setup(true);
    }

}
