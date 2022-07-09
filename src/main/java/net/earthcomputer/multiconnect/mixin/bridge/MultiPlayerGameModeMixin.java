package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = MultiPlayerGameMode.class, priority = -1000)
public class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    @ModifyArg(method = {"handleCreativeModeItemAdd", "handleCreativeModeItemDrop"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), index = 0)
    private Packet<?> modifyCreativeActionPacket(Packet<?> packet) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket creativePacket) {
            // shift slot ids in click slot packet
            LocalPlayer player = minecraft.player;
            assert player != null;
            AbstractContainerMenu menu = player.containerMenu;
            int slot = ConnectionInfo.protocol.clientSlotIdToServer(menu, creativePacket.getSlotNum());
            if (slot != creativePacket.getSlotNum()) {
                creativePacket = new ServerboundSetCreativeModeSlotPacket(slot, creativePacket.getItem());
            }

            return creativePacket;
        }

        return packet;
    }

    @ModifyArg(method = "handleInventoryMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), index = 0)
    private Packet<?> modifyInventoryMouseClickPacket(Packet<?> packet) {
        if (packet instanceof ServerboundContainerClickPacket clickPacket) {
            // shift slot ids in click slot packet
            LocalPlayer player = minecraft.player;
            assert player != null;
            AbstractContainerMenu menu = player.containerMenu;
            int slot = ConnectionInfo.protocol.clientSlotIdToServer(menu, clickPacket.getSlotNum());
            boolean modified = slot != clickPacket.getSlotNum();
            var modifiedStacks = new Int2ObjectOpenHashMap<ItemStack>();
            for (var entry : clickPacket.getChangedSlots().int2ObjectEntrySet()) {
                int newSlotId = ConnectionInfo.protocol.clientSlotIdToServer(menu, entry.getIntKey());
                modified |= newSlotId != entry.getIntKey();
                modifiedStacks.put(entry.getIntKey(), entry.getValue());
            }
            if (modified) {
                clickPacket = new ServerboundContainerClickPacket(clickPacket.getContainerId(), clickPacket.getStateId(), slot, clickPacket.getButtonNum(), clickPacket.getClickType(), clickPacket.getCarriedItem(), modifiedStacks);
            }

            return clickPacket;
        }

        return packet;
    }
}
