package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.generic.IServerboundSlotPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ClientPlayerInteractionManager.class, priority = -1000)
public class MixinClientPlayerInteractionManager {
    @Shadow @Final private MinecraftClient client;

    @ModifyArg(method = {"clickCreativeStack", "dropCreativeStack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"), index = 0)
    private Packet<?> modifyCreativeActionPacket(Packet<?> packet) {
        if (packet instanceof CreativeInventoryActionC2SPacket creativePacket) {
            // shift slot ids in click slot packet
            ClientPlayerEntity player = client.player;
            assert player != null;
            ScreenHandler screenHandler = player.currentScreenHandler;
            int slot = ConnectionInfo.protocol.clientSlotIdToServer(screenHandler, creativePacket.getSlot());
            if (slot != creativePacket.getSlot()) {
                creativePacket = new CreativeInventoryActionC2SPacket(slot, creativePacket.getItemStack());
            }

            //noinspection ConstantConditions
            ((IServerboundSlotPacket) creativePacket).multiconnect_setProcessed();

            return creativePacket;
        }

        return packet;
    }

    @ModifyArg(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"), index = 0)
    private Packet<?> modifyClickSlotPacket(Packet<?> packet) {
        if (packet instanceof ClickSlotC2SPacket clickSlotPacket) {
            // shift slot ids in click slot packet
            ClientPlayerEntity player = client.player;
            assert player != null;
            ScreenHandler screenHandler = player.currentScreenHandler;
            int slot = ConnectionInfo.protocol.clientSlotIdToServer(screenHandler, clickSlotPacket.getSlot());
            boolean modified = slot != clickSlotPacket.getSlot();
            var modifiedStacks = new Int2ObjectOpenHashMap<ItemStack>();
            for (var entry : clickSlotPacket.getModifiedStacks().int2ObjectEntrySet()) {
                int newSlotId = ConnectionInfo.protocol.clientSlotIdToServer(screenHandler, entry.getIntKey());
                modified |= newSlotId != entry.getIntKey();
                modifiedStacks.put(entry.getIntKey(), entry.getValue());
            }
            if (modified) {
                clickSlotPacket = new ClickSlotC2SPacket(clickSlotPacket.getSyncId(), clickSlotPacket.getRevision(), slot, clickSlotPacket.getButton(), clickSlotPacket.getActionType(), clickSlotPacket.getStack(), modifiedStacks);
            }

            //noinspection ConstantConditions
            ((IServerboundSlotPacket) clickSlotPacket).multiconnect_setProcessed();

            return clickSlotPacket;
        }

        return packet;
    }
}
