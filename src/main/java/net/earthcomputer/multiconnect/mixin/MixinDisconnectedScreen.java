package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ServersExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {

    @Unique private static final Set<String> TRIGGER_WORDS = ImmutableSet.of("outdated", "version");
    @Unique private ServerInfo server;
    @Unique private boolean isProtocolReason;
    @Unique private ButtonWidget protocolSelector;
    @Unique private Text forceProtocolLabel;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Screen parentScreen, String title, Text reason, CallbackInfo ci) {
        forceProtocolLabel = new TranslatableText("multiconnect.changeForcedProtocol").append(" ->");

        isProtocolReason = false;
        server = MinecraftClient.getInstance().getCurrentServerEntry();
        if (server != null) {
            String reasonText = reason.getString().toLowerCase();
            for (ConnectionMode protocol : ConnectionMode.values()) {
                if (protocol != ConnectionMode.AUTO && reasonText.contains(protocol.getName())) {
                    isProtocolReason = true;
                    break;
                }
            }
            for (String word : TRIGGER_WORDS) {
                if (reasonText.contains(word)) {
                    isProtocolReason = true;
                    break;
                }
            }
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addButtons(CallbackInfo ci) {
        if (isProtocolReason) {
            protocolSelector = new ButtonWidget(width - 80, 5, 70, 20, new LiteralText(getForcedVersion().getName()), (buttonWidget_1) ->
                    ServersExt.getInstance().getOrCreateServer(server.address).forcedProtocol = getForcedVersion().next().getValue()
            );

            addButton(protocolSelector);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V"))
    private void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isProtocolReason) {
            textRenderer.drawWithShadow(matrixStack, forceProtocolLabel, width - 85 - textRenderer.getStringWidth(forceProtocolLabel), 11, 0xFFFFFF);
            protocolSelector.setMessage(new LiteralText(getForcedVersion().getName()));
        }
    }

    @Override
    public void removed() {
        ServersExt.save();
    }

    @Unique
    private ConnectionMode getForcedVersion() {
        int protocolVersion = ServersExt.getInstance().getForcedProtocol(server.address);
        return ConnectionMode.byValue(protocolVersion);
    }

}
