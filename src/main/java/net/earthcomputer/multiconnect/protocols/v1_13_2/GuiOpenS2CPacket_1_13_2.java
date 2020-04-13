package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SOpenHorseWindowPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.util.text.ITextComponent;

public class GuiOpenS2CPacket_1_13_2 implements IPacket<ClientPlayNetHandler> {

    private int syncId;
    private String type;
    private ITextComponent title;
    private int slotCount;
    private int horseId;

    @Override
    public void readPacketData(PacketBuffer buf) {
        syncId = buf.readUnsignedByte();
        type = buf.readString(32);
        title = buf.readTextComponent();
        slotCount = buf.readUnsignedByte();
        if (type.equals("EntityHorse"))
            horseId = buf.readInt();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void processPacket(ClientPlayNetHandler handler) {
        if ("minecraft:container".equals(type)) {
            handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, slotCount <= 27 ? ContainerType.GENERIC_9X3 : ContainerType.GENERIC_9X6, title));
        } else if ("minecraft:villager".equals(type)) {
            handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.MERCHANT, title));
        } else if ("EntityHorse".equals(type)) {
            // "GuiOpen" packet is only for horses
            handler.handleOpenHorseWindow(new SOpenHorseWindowPacket(syncId, slotCount, horseId));
        } else if (slotCount <= 0) {
            if ("minecraft:crafting_table".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.CRAFTING, title));
            } else if ("minecraft:enchanting_table".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.ENCHANTMENT, title));
            } else if ("minecraft:anvil".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.ANVIL, title));
            }
        } else {
            if ("minecraft:chest".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, slotCount <= 27 ? ContainerType.GENERIC_9X3 : ContainerType.GENERIC_9X6, title));
            } else if ("minecraft:hopper".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.HOPPER, title));
            } else if ("minecraft:furnace".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.FURNACE, title));
            } else if ("minecraft:brewing_stand".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.BREWING_STAND, title));
            } else if ("minecraft:beacon".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.BEACON, title));
            } else if ("minecraft:dispenser".equals(type) || "minecraft:dropper".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.GENERIC_3X3, title));
            } else if ("minecraft:shulker_box".equals(type)) {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, ContainerType.SHULKER_BOX, title));
            } else {
                handler.handleOpenWindowPacket(new SOpenWindowPacket(syncId, slotCount <= 27 ? ContainerType.GENERIC_9X3 : ContainerType.GENERIC_9X6, title));
            }
        }
    }
}
