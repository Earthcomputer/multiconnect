package net.earthcomputer.multiconnect.protocols.v1_15.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.JigsawBlockEditScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JigsawBlockEditScreen.class)
public class JigsawBlockEditScreenMixin extends Screen {

    @Shadow private EditBox nameEdit;
    @Shadow private EditBox targetEdit;
    @Shadow private CycleButton<JigsawBlockEntity.JointType> jointButton;

    protected JigsawBlockEditScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            nameEdit.active = false;
            jointButton.active = false;
            int index = children().indexOf(jointButton);
            ((AbstractWidget) children().get(index + 1)).active = false; // levels slider
            ((AbstractWidget) children().get(index + 2)).active = false; // keep jigsaws toggle
            ((AbstractWidget) children().get(index + 3)).active = false; // generate button
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_15_2) {
            nameEdit.setValue(targetEdit.getValue());
        }
    }

}
