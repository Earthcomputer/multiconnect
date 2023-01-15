package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.WorldVersion;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public class OnlineServerEntryMixin {

    @Shadow @Final private ServerData serverData;

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/WorldVersion;getProtocolVersion()I"))
    public int redirectProtocolVersion(WorldVersion version) {
        if (!ProtocolRegistry.isSupported(serverData.protocol))
            return version.getProtocolVersion();
        else
            return serverData.protocol;
    }

}
