package net.earthcomputer.multiconnect.protocols.v1_12_1.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class MixinDownloadingTerrainScreen {
    @Unique private int tickCounter;

    @Inject(method = "tick", at = @At("HEAD"))
    public void onTick(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_1) {
            tickCounter++;

            if (tickCounter % 20 == 0) {
                //noinspection ConstantConditions
                MinecraftClient.getInstance().getNetworkHandler().sendPacket(new KeepAliveC2SPacket(0));
            }
        }
    }
}
