package net.earthcomputer.multiconnect.protocols.v1_14.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @ModifyVariable(
        method = "tick",
        at = @At(
            value = "LOAD",
            ordinal = 0
        ),
        argsOnly = true
    )
    private boolean changeSneakSlowdownCondition(boolean slowDown) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_13_2) {
            // Copied from 1.13.2
            return this.shiftKeyDown;
        } else if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            // Copied from 1.14.4
            // This `!this.isSpectator()` is actually very interesting
            // because Mojang added it to try and prevent the player
            // from slowing down in spectator mode, but they actually
            // did the opposite and made the player go faster if
            // sneaking while flying in spectator mode because further
            // down in the code they had a condition to undo the sneak
            // slowdown if the player was flying.
            Minecraft client = Minecraft.getInstance();
            LocalPlayer player = client.player;
            assert player != null;
            return !player.isSpectator() && (this.shiftKeyDown || slowDown);
        }
        return slowDown;
    }
}
