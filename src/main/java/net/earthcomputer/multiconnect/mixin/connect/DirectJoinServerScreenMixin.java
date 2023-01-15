package net.earthcomputer.multiconnect.mixin.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.DropDownWidget;
import net.earthcomputer.multiconnect.connect.ServersExt;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectJoinServerScreen.class)
public class DirectJoinServerScreenMixin extends Screen {

    @Shadow private EditBox ipEdit;

    @Unique private String multiconnect_lastAddress;
    @Unique private DropDownWidget<IProtocol> multiconnect_protocolSelector;
    @Unique private FormattedCharSequence multiconnect_forceProtocolLabel;

    protected DirectJoinServerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        multiconnect_forceProtocolLabel = Component.translatable("multiconnect.changeForcedProtocol").append(" ->").getVisualOrderText();
        multiconnect_protocolSelector = Utils.createVersionDropdown(this, ConnectionMode.AUTO);
        addRenderableWidget(multiconnect_protocolSelector);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (!ipEdit.getValue().equals(multiconnect_lastAddress)) {
            multiconnect_lastAddress = ipEdit.getValue();
            if (ServersExt.getInstance().hasServer(ipEdit.getValue())) {
                int protocolVersion = ServersExt.getInstance().getForcedProtocol(ipEdit.getValue());
                multiconnect_protocolSelector.setValue(ProtocolRegistry.get(protocolVersion));
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(PoseStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        font.drawShadow(matrixStack, multiconnect_forceProtocolLabel, width - 85 - font.width(multiconnect_forceProtocolLabel), 11, 0xFFFFFF);
        multiconnect_protocolSelector.render(matrixStack, mouseX, mouseY, delta);
    }

    @Inject(method = "onSelect", at = @At("HEAD"))
    private void onOnSelect(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(ipEdit.getValue()).forcedProtocol = multiconnect_protocolSelector.getValue().getValue();
        ServersExt.save();
    }

}
