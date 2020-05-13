package net.earthcomputer.multiconnect.mixin;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ServersExt;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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

    @Unique private ConnectionMode currentProtocol;
    @Unique private ButtonWidget protocolSelector;
    @Unique private Text forceProtocolLabel;

    protected MixinAddServerScreen(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructor(Screen parent, BooleanConsumer callback, ServerInfo server, CallbackInfo ci) {
        currentProtocol = ConnectionMode.byValue(ServersExt.getInstance().getForcedProtocol(server.address));
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        forceProtocolLabel = new TranslatableText("multiconnect.changeForcedProtocol").append(" ->");
        protocolSelector = new ButtonWidget(width - 80, 5, 70, 20, new LiteralText(currentProtocol.getName()), (buttonWidget_1) ->
                currentProtocol = currentProtocol.next()
        );
        addButton(protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        textRenderer.drawWithShadow(matrixStack, forceProtocolLabel, width - 85 - textRenderer.getWidth(forceProtocolLabel), 11, 0xFFFFFF);
        protocolSelector.setMessage(new LiteralText(currentProtocol.getName()));
    }

    @Inject(method = "addAndClose", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V", remap = false))
    private void onAddAndClose(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(server.address).forcedProtocol = currentProtocol.getValue();
    }

}
