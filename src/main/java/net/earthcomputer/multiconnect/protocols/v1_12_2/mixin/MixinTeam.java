package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Team.class)
public abstract class MixinTeam {
    @Shadow private Text prefix;
    @Shadow private Text suffix;

    @Shadow public abstract Formatting getColor();

    @Inject(
        method = "decorateName(Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void modifyTeamNameDecoration(Text name, CallbackInfoReturnable<MutableText> cir) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
            String concated = this.prefix.asString() + name.asString() + this.suffix.asString();
            MutableText mutableText = new LiteralText(concated);
            Formatting formatting = this.getColor();
            if (formatting != Formatting.RESET) {
                mutableText.formatted(formatting);
            }
            cir.setReturnValue(mutableText);
        }
    }
}
