package net.earthcomputer.multiconnect.mixin.compat;

import com.google.common.base.Joiner;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager", remap = false)
public class MixinRegistrySyncManager {
    @Unique
    private static final Logger MULTICONNECT_LOGGER = LogManager.getLogger("multiconnect");

    @Dynamic
    @Inject(method = "createPacket", at = @At("HEAD"), cancellable = true)
    private static void onCreatePacket(CallbackInfoReturnable<Packet<?>> ci) {
        // don't try to sync other clients in LAN mode, or for whatever reason to the singleplayer owner
        MULTICONNECT_LOGGER.info("Cancelling Fabric API registry sync packet server-side");
        ci.setReturnValue(null);
    }

    @Dynamic
    @Inject(method = "lambda$receivePacket$0", at = @At("HEAD"), cancellable = true)
    private static void onApplyModdedRegistry(NbtCompound tag, Consumer<Exception> errorHandler, CallbackInfoReturnable<Object> ci) {
        if (tag == null) {
            return;
        }
        NbtCompound mainNbt = tag.getCompound("registries");
        Set<String> registries = mainNbt.getKeys();
        if (!registries.isEmpty()) {
            String registriesStr = Joiner.on(", ").join(registries);
            errorHandler.accept(new RuntimeException("Server contains Fabric-modded registries: " + registriesStr + "! Multiconnect does not support modded registries."));
            ci.setReturnValue(null);
        }
    }
}
