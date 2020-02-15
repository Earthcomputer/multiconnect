package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerScreen.class)
public class MixinMultiplayerScreen extends Screen {
    @Unique private ButtonWidget protocolSelector;

    protected MixinMultiplayerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    public void createButtons(CallbackInfo ci) {
        protocolSelector = new ButtonWidget(width - 80, 5, 70, 20, ConnectionInfo.globalForcedProtocolVersion.getName(), (buttonWidget_1) ->
                ConnectionInfo.globalForcedProtocolVersion = ConnectionInfo.globalForcedProtocolVersion.next()
        );

        addButton(protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow(I18n.translate("multiconnect.changeForcedProtocol") + " ->", width - 85 - MinecraftClient.getInstance().textRenderer.getStringWidth(I18n.translate("multiconnect.changeForcedProtocol") + " ->"), 11, 0xFFFFFF);
        protocolSelector.setMessage(ConnectionInfo.globalForcedProtocolVersion.getName());
    }
}
