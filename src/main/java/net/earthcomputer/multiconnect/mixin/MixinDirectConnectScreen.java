package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ServersExt;
import net.minecraft.client.gui.screen.DirectConnectScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
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
    @Unique private ConnectionMode selectedProtocol = ConnectionMode.AUTO;
    @Unique private ButtonWidget protocolSelector;

    protected MixinDirectConnectScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        protocolSelector = new ButtonWidget(width - 80, 5, 70, 20, selectedProtocol.getName(), (buttonWidget_1) ->
                selectedProtocol = selectedProtocol.next()
        );

        addButton(protocolSelector);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (!addressField.getText().equals(lastAddress)) {
            lastAddress = addressField.getText();
            if (ServersExt.getInstance().hasServer(addressField.getText())) {
                int protocolVersion = ServersExt.getInstance().getForcedProtocol(addressField.getText());
                selectedProtocol = ConnectionMode.byValue(protocolVersion);
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String label = I18n.translate("multiconnect.changeForcedProtocol") + " ->";
        font.drawWithShadow(label, width - 85 - font.getStringWidth(label), 11, 0xFFFFFF);
        protocolSelector.setMessage(selectedProtocol.getName());
    }

    @Inject(method = "saveAndClose", at = @At("HEAD"))
    private void onSaveAndClose(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(addressField.getText()).forcedProtocol = selectedProtocol.getValue();
    }

}
