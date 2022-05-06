package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.minecraft.GameVersion;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public class MixinServerListIcon {

    @Shadow @Final private ServerInfo server;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/GameVersion;getProtocolVersion()I"))
    public int redirectProtocolVersion(GameVersion version) {
        if (!ConnectionMode.isSupportedProtocol(server.protocolVersion))
            return version.getProtocolVersion();
        else
            return server.protocolVersion;
    }

}
