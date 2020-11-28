package net.earthcomputer.multiconnect.protocols.v1_15_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.entity.JigsawBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.JigsawBlockScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JigsawBlockScreen.class)
public class MixinJigsawBlockScreen extends Screen {

    @Shadow private TextFieldWidget nameField;
    @Shadow private TextFieldWidget targetField;
    @Shadow private CyclingButtonWidget<JigsawBlockEntity.Joint> jointRotationButton;

    protected MixinJigsawBlockScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            nameField.active = false;
            jointRotationButton.active = false;
            int index = buttons.indexOf(jointRotationButton);
            buttons.get(index + 1).active = false; // levels slider
            buttons.get(index + 2).active = false; // keep jigsaws toggle
            buttons.get(index + 3).active = false; // generate button
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            nameField.setText(targetField.getText());
        }
    }

}
