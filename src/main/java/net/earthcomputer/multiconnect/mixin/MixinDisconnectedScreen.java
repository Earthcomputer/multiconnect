package net.earthcomputer.multiconnect.mixin;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ServersExt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {

    @Unique private static final Set<String> TRIGGER_WORDS = ImmutableSet.of("outdated", "version");
    @Unique private ServerData server;
    @Unique private boolean isProtocolReason;
    @Unique private Button protocolSelector;

    protected MixinDisconnectedScreen(ITextComponent title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Screen parentScreen, String title, ITextComponent reason, CallbackInfo ci) {
        isProtocolReason = false;
        server = Minecraft.getInstance().getCurrentServerData();
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
            protocolSelector = new Button(width - 80, 5, 70, 20, getForcedVersion().getName(), (buttonWidget_1) ->
                    ServersExt.getInstance().getOrCreateServer(server.serverIP).forcedProtocol = getForcedVersion().next().getValue()
            );

            addButton(protocolSelector);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V"))
    private void onRender(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (isProtocolReason) {
            String label = I18n.format("multiconnect.changeForcedProtocol") + " ->";
            font.drawStringWithShadow(label, width - 85 - font.getStringWidth(label), 11, 0xFFFFFF);
            protocolSelector.setMessage(getForcedVersion().getName());
        }
    }

    @Override
    public void removed() {
        ServersExt.save();
    }

    @Unique
    private ConnectionMode getForcedVersion() {
        int protocolVersion = ServersExt.getInstance().getForcedProtocol(server.serverIP);
        return ConnectionMode.byValue(protocolVersion);
    }

}
