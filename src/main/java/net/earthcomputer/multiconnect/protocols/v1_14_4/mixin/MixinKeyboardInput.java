package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
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
            return this.sneaking;
        } else if (ConnectionInfo.protocolVersion <= Protocols.V1_14_4) {
            // Copied from 1.14.4
            // This `!this.isSpectator()` is actually very interesting
            // because Mojang added it to try and prevent the player
            // from slowing down in spectator mode, but they actually
            // did the opposite and made the player go faster if
            // sneaking while flying in spectator mode because further
            // down in the code they had a condition to undo the sneak
            // slowdown if the player was flying.
            MinecraftClient client = MinecraftClient.getInstance();
            ClientPlayerEntity player = client.player;
            assert player != null;
            return !player.isSpectator() && (this.sneaking || slowDown);
        }
        return slowDown;
    }
}
