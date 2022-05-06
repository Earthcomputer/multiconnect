package net.earthcomputer.multiconnect.mixin.compat;

import com.google.common.base.Joiner;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager", remap = false)
public class MixinRegistrySyncManager {
    @Unique
    private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");

    @Dynamic
    @Inject(method = {"sendPacket(Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/fabricmc/fabric/impl/registry/sync/packet/RegistryPacketHandler;)V", "sendPacket(Lnet/minecraft/class_3222;Lnet/fabricmc/fabric/impl/registry/sync/packet/RegistryPacketHandler;)V"},
            at = @At("HEAD"),
            cancellable = true)
    private static void onCreatePacket(CallbackInfo ci) {
        // don't try to sync other clients in LAN mode, or for whatever reason to the singleplayer owner
        MULTICONNECT_LOGGER.info("Cancelling Fabric API registry sync packet server-side");
        ci.cancel();
    }

    @Dynamic
    @Inject(method = "lambda$receivePacket$0", at = @At("HEAD"), cancellable = true)
    private static void onApplyModdedRegistry(Map<Identifier, Object2IntMap<Identifier>> map, Consumer<Exception> errorHandler, CallbackInfoReturnable<Object> ci) {
        if (map == null) {
            return;
        }
        if (!map.isEmpty()) {
            String registriesStr = Joiner.on(", ").join(map.keySet());
            errorHandler.accept(new RuntimeException("Server contains Fabric-modded registries: " + registriesStr + "! Multiconnect does not support modded registries."));
            ci.setReturnValue(null);
        }
    }
}
