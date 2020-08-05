package net.earthcomputer.multiconnect.mixin.connect;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.DropDownWidget;
import net.earthcomputer.multiconnect.connect.ServersExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
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
    @Unique private DropDownWidget<ConnectionMode> protocolSelector;
    @Unique private OrderedText forceProtocolLabel;

    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Screen parent, Text title, Text reason, CallbackInfo ci) {
        forceProtocolLabel = new TranslatableText("multiconnect.changeForcedProtocol").append(" ->").asOrderedText();

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
            protocolSelector = new DropDownWidget<>(width - 80, 5, 70, 20, getForcedVersion(), mode -> new LiteralText(mode.getName()));
            protocolSelector.setValueListener(mode -> ServersExt.getInstance().getOrCreateServer(server.address).forcedProtocol = mode.getValue());
            ConnectionMode.populateDropDownWidget(protocolSelector);
            children.add(0, protocolSelector);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isProtocolReason) {
            textRenderer.drawWithShadow(matrixStack, forceProtocolLabel, width - 85 - textRenderer.getWidth(forceProtocolLabel), 11, 0xFFFFFF);
            protocolSelector.render(matrixStack, mouseX, mouseY, delta);
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
