package net.earthcomputer.multiconnect.protocols.v1_11_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_11_2.ClientStatusC2SPacket_1_11_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient {

    @Shadow public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasRidingInventory()Z"))
    private void onInventoryKeyPressed(CallbackInfo ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_11_2) {
            getNetworkHandler().sendPacket(new ClientStatusC2SPacket_1_11_2(ClientStatusC2SPacket_1_11_2.Mode.OPEN_INVENTORY_ACHIEVEMENT));
        }
    }

}
