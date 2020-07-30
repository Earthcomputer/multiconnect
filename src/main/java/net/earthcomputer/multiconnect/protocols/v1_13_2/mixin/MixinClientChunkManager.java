package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier fallingBehind, CallbackInfo ci) {
        if (MinecraftClient.getInstance().getCameraEntity() != null
                && MinecraftClient.getInstance().getCameraEntity() != MinecraftClient.getInstance().player
                && MinecraftClient.getInstance().getCameraEntity().isAlive()) {
            Protocol_1_13_2.updateCameraPosition();
        }
    }

}
