package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandBlockScreen.class)
public abstract class MixinCommandBlockScreen {
    @Shadow private CyclingButtonWidget<CommandBlockBlockEntity.Type> modeButton;
    @Shadow private CyclingButtonWidget<Boolean> conditionalModeButton;
    @Shadow private CyclingButtonWidget<Boolean> redstoneTriggerButton;

    @Shadow public abstract void updateCommandBlock();

    @Inject(method = "init", at = @At("TAIL"))
    private void afterInit(CallbackInfo ci) {
        modeButton.visible = false;
        conditionalModeButton.visible = false;
        redstoneTriggerButton.visible = false;
        updateCommandBlock();
    }
}
