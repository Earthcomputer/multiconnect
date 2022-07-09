package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerTeam.class)
public abstract class PlayerTeamMixin {
    @Shadow private Component playerPrefix;
    @Shadow private Component playerSuffix;

    @Shadow public abstract ChatFormatting getColor();

    @Inject(method = "getFormattedName", at = @At("HEAD"), cancellable = true)
    private void modifyTeamNameDecoration(Component name, CallbackInfoReturnable<MutableComponent> cir) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            String concated = this.playerPrefix.getString() + name.getString() + this.playerSuffix.getString();
            MutableComponent mutableText = Component.literal(concated);
            ChatFormatting formatting = this.getColor();
            if (formatting != ChatFormatting.RESET) {
                mutableText.withStyle(formatting);
            }
            cir.setReturnValue(mutableText);
        }
    }
}
