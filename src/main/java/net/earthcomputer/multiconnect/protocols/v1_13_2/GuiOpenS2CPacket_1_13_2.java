package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.OpenHorseScreenS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public class GuiOpenS2CPacket_1_13_2 implements Packet<ClientPlayNetworkHandler> {

    private int syncId;
    private String type;
    private Text title;
    private int slotCount;
    private int horseId;

    @Override
    public void read(PacketByteBuf buf) {
        syncId = buf.readUnsignedByte();
        type = buf.readString(32);
        title = buf.readText();
        slotCount = buf.readUnsignedByte();
        if (type.equals("EntityHorse"))
            horseId = buf.readInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayNetworkHandler handler) {
        if ("minecraft:container".equals(type)) {
            handler.onOpenScreen(new OpenScreenS2CPacket(syncId, slotCount <= 27 ? ScreenHandlerType.GENERIC_9X3 : ScreenHandlerType.GENERIC_9X6, title));
        } else if ("minecraft:villager".equals(type)) {
            handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.MERCHANT, title));
        } else if ("EntityHorse".equals(type)) {
            handler.onOpenHorseScreen(new OpenHorseScreenS2CPacket(syncId, slotCount, horseId));
        } else if (slotCount <= 0) {
            if ("minecraft:crafting_table".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.CRAFTING, title));
            } else if ("minecraft:enchanting_table".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.ENCHANTMENT, title));
            } else if ("minecraft:anvil".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.ANVIL, title));
            }
        } else {
            if ("minecraft:chest".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, slotCount <= 27 ? ScreenHandlerType.GENERIC_9X3 : ScreenHandlerType.GENERIC_9X6, title));
            } else if ("minecraft:hopper".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.HOPPER, title));
            } else if ("minecraft:furnace".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.FURNACE, title));
            } else if ("minecraft:brewing_stand".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.BREWING_STAND, title));
            } else if ("minecraft:beacon".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.BEACON, title));
            } else if ("minecraft:dispenser".equals(type) || "minecraft:dropper".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.GENERIC_3X3, title));
            } else if ("minecraft:shulker_box".equals(type)) {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, ScreenHandlerType.SHULKER_BOX, title));
            } else {
                handler.onOpenScreen(new OpenScreenS2CPacket(syncId, slotCount <= 27 ? ScreenHandlerType.GENERIC_9X3 : ScreenHandlerType.GENERIC_9X6, title));
            }
        }
    }
}
