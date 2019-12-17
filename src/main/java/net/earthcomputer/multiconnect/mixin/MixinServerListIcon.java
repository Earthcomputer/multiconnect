package net.earthcomputer.multiconnect.mixin;

import com.mojang.bridge.game.GameVersion;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
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

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/bridge/game/GameVersion;getProtocolVersion()I", remap = false))
    public int redirectProtocolVersion(GameVersion version) {
        if (!ProtocolRegistry.isSupported(server.protocolVersion))
            return version.getProtocolVersion();
        else
            return server.protocolVersion;
    }

}
