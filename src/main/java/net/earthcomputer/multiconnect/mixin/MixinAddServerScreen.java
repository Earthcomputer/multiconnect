package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IServerInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AddServerScreen.class)
public abstract class MixinAddServerScreen extends Screen {

    @Shadow @Final private ServerInfo server;
    @Unique private ButtonWidget protocolSelector;

    protected MixinAddServerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        protocolSelector = new ButtonWidget(5, 5, 70, 20, ((IServerInfo) server).multiconnect_getForcedVersion().getName(), (buttonWidget_1) ->
                ((IServerInfo) server).multiconnect_setForcedVersion(((IServerInfo) server).multiconnect_getForcedVersion().next())
        );

        addButton(protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow("<- " + I18n.translate("multiconnect.changeForcedProtocol"), 80, 10, 0xFFFFFF);
        protocolSelector.setMessage(((IServerInfo) server).multiconnect_getForcedVersion().getName());
    }
}
