package net.earthcomputer.multiconnect.mixin.connect;

import com.mojang.blaze3d.vertex.PoseStack;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.connect.ServersExt;
import net.earthcomputer.multiconnect.impl.DropDownWidget;
import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditServerScreen.class)
public abstract class EditServerScreenMixin extends Screen {

    @Shadow @Final private ServerData serverData;

    @Unique private DropDownWidget<IProtocol> multiconnect_protocolSelector;
    @Unique private FormattedCharSequence multiconnect_forceProtocolLabel;

    protected EditServerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        multiconnect_forceProtocolLabel = Component.translatable("multiconnect.changeForcedProtocol").append(" ->").getVisualOrderText();
        multiconnect_protocolSelector = Utils.createVersionDropdown(this, ServersExt.getInstance().getForcedProtocolObj(serverData.ip));
        addRenderableWidget(multiconnect_protocolSelector);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(PoseStack stack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        font.drawShadow(stack, multiconnect_forceProtocolLabel, width - multiconnect_protocolSelector.getWidth() - 10 - font.width(multiconnect_forceProtocolLabel), 11, 0xFFFFFF);
        multiconnect_protocolSelector.render(stack, mouseX, mouseY, delta);
    }

    @Inject(method = "onAdd", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/booleans/BooleanConsumer;accept(Z)V", remap = false))
    private void onAddAndClose(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(serverData.ip).forcedProtocol = multiconnect_protocolSelector.getValue().getValue();
    }

}
