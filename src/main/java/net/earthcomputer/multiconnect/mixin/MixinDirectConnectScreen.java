package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.ConnectionMode;
import net.earthcomputer.multiconnect.impl.ServersExt;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ServerListScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerListScreen.class)
public class MixinDirectConnectScreen extends Screen {

    @Shadow private TextFieldWidget ipEdit;

    @Unique private String lastAddress;
    @Unique private ConnectionMode selectedProtocol = ConnectionMode.AUTO;
    @Unique private Button protocolSelector;

    protected MixinDirectConnectScreen(ITextComponent title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void createButtons(CallbackInfo ci) {
        protocolSelector = new Button(width - 80, 5, 70, 20, selectedProtocol.getName(), (buttonWidget_1) ->
                selectedProtocol = selectedProtocol.next()
        );

        addButton(protocolSelector);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (!ipEdit.getText().equals(lastAddress)) {
            lastAddress = ipEdit.getText();
            if (ServersExt.getInstance().hasServer(ipEdit.getText())) {
                int protocolVersion = ServersExt.getInstance().getForcedProtocol(ipEdit.getText());
                selectedProtocol = ConnectionMode.byValue(protocolVersion);
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void drawScreen(int mouseX, int mouseY, float delta, CallbackInfo ci) {
        String label = I18n.format("multiconnect.changeForcedProtocol") + " ->";
        font.drawStringWithShadow(label, width - 85 - font.getStringWidth(label), 11, 0xFFFFFF);
        protocolSelector.setMessage(selectedProtocol.getName());
    }

    @Inject(method = "func_195167_h", at = @At("HEAD"))
    private void onSaveAndClose(CallbackInfo ci) {
        ServersExt.getInstance().getOrCreateServer(ipEdit.getText()).forcedProtocol = selectedProtocol.getValue();
    }

}
