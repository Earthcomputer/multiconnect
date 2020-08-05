package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.DropDownWidget;
import net.earthcomputer.multiconnect.connect.ServersExt;
import net.minecraft.client.gui.screen.AddServerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
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

    @Unique private DropDownWidget<ConnectionMode> protocolSelector;
    @Unique private OrderedText forceProtocolLabel;

    protected MixinAddServerScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        forceProtocolLabel = new TranslatableText("multiconnect.changeForcedProtocol").append(" ->").asOrderedText();
        protocolSelector = new DropDownWidget<>(width - 80, 5, 70, 20, ConnectionMode.byValue(ServersExt.getInstance().getForcedProtocol(server.address)), mode -> new LiteralText(mode.getName()));
        ConnectionMode.populateDropDownWidget(protocolSelector);
        children.add(0, protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        textRenderer.drawWithShadow(matrixStack, forceProtocolLabel, width - 85 - textRenderer.getWidth(forceProtocolLabel), 11, 0xFFFFFF);
        protocolSelector.render(matrixStack, mouseX, mouseY, delta);
    }

    @Inject(method = "addAndClose", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V", remap = false))
    private void onAddAndClose(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(server.address).forcedProtocol = protocolSelector.getValue().getValue();
    }

}
