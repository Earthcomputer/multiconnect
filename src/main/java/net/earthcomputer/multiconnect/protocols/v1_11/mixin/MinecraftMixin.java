package net.earthcomputer.multiconnect.protocols.v1_11.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public abstract ClientPacketListener getConnection();

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isServerControlledInventory()Z"))
    private void onInventoryKeyPressed(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            // TODO: rewrite 1.11.2
//            getConnection().sendPacket(new ClientStatusC2SPacket_1_11_2(ClientStatusC2SPacket_1_11_2.Mode.OPEN_INVENTORY_ACHIEVEMENT));
        }
    }

}
