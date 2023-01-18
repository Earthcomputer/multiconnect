package net.earthcomputer.multiconnect.mixin.connect;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.connect.ServersExt;
import net.earthcomputer.multiconnect.impl.DropDownWidget;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {

    @Unique private static final Set<String> MULTICONNECT_TRIGGER_WORDS = ImmutableSet.of("outdated", "incompatible", "version");
    @Unique private ServerData multiconnect_server;
    @Unique private boolean multiconnect_isProtocolReason;
    @Unique private DropDownWidget<IProtocol> multiconnect_protocolSelector;
    @Unique private FormattedCharSequence multiconnect_forceProtocolLabel;

    protected DisconnectedScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Screen parent, Component title, Component reason, CallbackInfo ci) {
        multiconnect_forceProtocolLabel = Component.translatable("multiconnect.changeForcedProtocol").append(" ->").getVisualOrderText();

        multiconnect_isProtocolReason = false;
        multiconnect_server = Minecraft.getInstance().getCurrentServer();
        if (multiconnect_server != null) {
            String reasonText = reason.getString().toLowerCase();
            for (IProtocol protocol : ProtocolRegistry.getProtocols()) {
                if (reasonText.contains(protocol.getName())) {
                    multiconnect_isProtocolReason = true;
                    break;
                }
            }
            for (String word : MULTICONNECT_TRIGGER_WORDS) {
                if (reasonText.contains(word)) {
                    multiconnect_isProtocolReason = true;
                    break;
                }
            }
        }
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void addButtons(CallbackInfo ci) {
        if (multiconnect_isProtocolReason) {
            multiconnect_protocolSelector = Utils.createVersionDropdown(this, getForcedVersion());
            multiconnect_protocolSelector.setValueListener(mode -> ServersExt.getInstance().getOrCreateServer(multiconnect_server.ip).forcedProtocol = mode.getValue());
            addRenderableWidget(multiconnect_protocolSelector);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(PoseStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (multiconnect_isProtocolReason) {
            font.drawShadow(matrixStack, multiconnect_forceProtocolLabel, width - multiconnect_protocolSelector.getWidth() - 10 - font.width(multiconnect_forceProtocolLabel), 11, 0xFFFFFF);
            multiconnect_protocolSelector.render(matrixStack, mouseX, mouseY, delta);
        }
    }

    @Override
    public void removed() {
        ServersExt.save();
    }

    @Unique
    private IProtocol getForcedVersion() {
        return ServersExt.getInstance().getForcedProtocolObj(multiconnect_server.ip);
    }

}
