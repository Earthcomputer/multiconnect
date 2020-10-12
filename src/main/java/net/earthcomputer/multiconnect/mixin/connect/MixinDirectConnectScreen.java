package net.earthcomputer.multiconnect.mixin.connect;

import net.earthcomputer.multiconnect.connect.ConnectionMode;
import net.earthcomputer.multiconnect.impl.DropDownWidget;
import net.earthcomputer.multiconnect.connect.ServersExt;
import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.client.gui.screen.DirectConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectConnectScreen.class)
public class MixinDirectConnectScreen extends Screen {

    @Shadow private TextFieldWidget addressField;

    @Unique private String lastAddress;
    @Unique private DropDownWidget<ConnectionMode> protocolSelector;
    @Unique private OrderedText forceProtocolLabel;

    protected MixinDirectConnectScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        forceProtocolLabel = new TranslatableText("multiconnect.changeForcedProtocol").append(" ->").asOrderedText();
        protocolSelector = Utils.createVersionDropdown(this, ConnectionMode.AUTO);
        children.add(0, protocolSelector);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (!addressField.getText().equals(lastAddress)) {
            lastAddress = addressField.getText();
            if (ServersExt.getInstance().hasServer(addressField.getText())) {
                int protocolVersion = ServersExt.getInstance().getForcedProtocol(addressField.getText());
                protocolSelector.setValue(ConnectionMode.byValue(protocolVersion));
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(MatrixStack matrixStack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        textRenderer.drawWithShadow(matrixStack, forceProtocolLabel, width - 85 - textRenderer.getWidth(forceProtocolLabel), 11, 0xFFFFFF);
        protocolSelector.render(matrixStack, mouseX, mouseY, delta);
    }

    @Inject(method = "saveAndClose", at = @At("HEAD"))
    private void onSaveAndClose(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(addressField.getText()).forcedProtocol = protocolSelector.getValue().getValue();
        ServersExt.save();
    }

}
