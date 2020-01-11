package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.api.EnumProtocol;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.IMixinScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public abstract class MixinMultiplayerScreen {
    private ButtonWidget protocolSelector;

    @Inject(method = "init", at = @At("RETURN"))
    public void createButtons(CallbackInfo ci) {
        IMixinScreen screen = (IMixinScreen) this;

        protocolSelector = new ButtonWidget(5, 5, 70, 20, EnumProtocol.getEnumNameForValue(ConnectionInfo.forcedProtocolVersion), (buttonWidget_1) ->
                ConnectionInfo.forcedProtocolVersion = EnumProtocol.valueOf(EnumProtocol.getEnumNameForValue(ConnectionInfo.forcedProtocolVersion)).next().getValue());

        screen.addGuiButtonToList(protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow("<- Change Version", 80, 10, 0xFFFFFF);
        protocolSelector.setMessage(EnumProtocol.getEnumNameForValue(ConnectionInfo.forcedProtocolVersion));
    }
}
