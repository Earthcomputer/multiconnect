package net.earthcomputer.multiconnect.mixin;

import com.mojang.bridge.game.GameVersion;
import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.minecraft.client.gui.screen.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerSelectionList.NormalEntry.class)
public class MixinServerListIcon {

    @Shadow @Final private ServerData server;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/bridge/game/GameVersion;getProtocolVersion()I", remap = false))
    public int redirectProtocolVersion(GameVersion version) {
        if (!ConnectionMode.isSupportedProtocol(server.version))
            return version.getProtocolVersion();
        else
            return server.version;
    }

}
