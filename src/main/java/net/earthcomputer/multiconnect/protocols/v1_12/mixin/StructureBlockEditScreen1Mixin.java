package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen$1")
public class StructureBlockEditScreen1Mixin extends EditBox {
    public StructureBlockEditScreen1Mixin(Font font, int x, int y, int width, int height, EditBox copyFrom, Component text) {
        super(font, x, y, width, height, copyFrom, text);
    }

    @Inject(method = "charTyped(CI)Z", at = @At("HEAD"), cancellable = true)
    private void onCharTyped(char chr, int keyCode, CallbackInfoReturnable<Boolean> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            ci.setReturnValue(super.charTyped(chr, keyCode));
        }
    }
}
