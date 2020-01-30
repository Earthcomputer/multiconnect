package net.earthcomputer.multiconnect.protocols.v1_13_2;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.packet.OpenContainerS2CPacket;
import net.minecraft.client.network.packet.OpenHorseContainerS2CPacket;
import net.minecraft.container.ContainerType;
import net.minecraft.network.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.PacketByteBuf;

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
            handler.onOpenContainer(new OpenContainerS2CPacket(syncId, slotCount <= 27 ? ContainerType.GENERIC_9X3 : ContainerType.GENERIC_9X6, title));
        } else if ("minecraft:villager".equals(type)) {
            handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.MERCHANT, title));
        } else // "GuiOpen" packet is only for horses
            if ("EntityHorse".equals(type))
                handler.onOpenHorseContainer(new OpenHorseContainerS2CPacket(syncId, slotCount, horseId));
            else if (slotCount <= 0) {
            if ("minecraft:crafting_table".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.CRAFTING, title));
            } else if ("minecraft:enchanting_table".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.ENCHANTMENT, title));
            } else if ("minecraft:anvil".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.ANVIL, title));
            }
        } else {
            if ("minecraft:chest".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, slotCount <= 27 ? ContainerType.GENERIC_9X3 : ContainerType.GENERIC_9X6, title));
            } else if ("minecraft:hopper".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.HOPPER, title));
            } else if ("minecraft:furnace".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.FURNACE, title));
            } else if ("minecraft:brewing_stand".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.BREWING_STAND, title));
            } else if ("minecraft:beacon".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.BEACON, title));
            } else if ("minecraft:dispenser".equals(type) || "minecraft:dropper".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.GENERIC_3X3, title));
            } else if ("minecraft:shulker_box".equals(type)) {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, ContainerType.SHULKER_BOX, title));
            } else {
                handler.onOpenContainer(new OpenContainerS2CPacket(syncId, slotCount <= 27 ? ContainerType.GENERIC_9X3 : ContainerType.GENERIC_9X6, title));
            }
        }
    }
}
