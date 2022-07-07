package net.earthcomputer.multiconnect.mixin.debug;

import net.earthcomputer.multiconnect.debug.PacketRecorder;
import net.earthcomputer.multiconnect.debug.PacketReplay;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        PacketRecorder.tick();
        PacketReplay.tick();
    }
}
