package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.IMinecraftClient;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.search.SearchManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClient {

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    public void onDisconnect(Screen screen, CallbackInfo ci) {
        ConnectionInfo.ip = null;
        ConnectionInfo.port = -1;
        ConnectionInfo.protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
        ConnectionInfo.protocol = ProtocolRegistry.get(ConnectionInfo.protocolVersion);
    }

    @Accessor
    @Override
    public abstract ItemColors getItemColorMap();

    @Invoker
    @Override
    public abstract void callInitializeSearchableContainers();

    @Accessor
    @Override
    public abstract SearchManager getSearchManager();
}
