package net.earthcomputer.multiconnect.protocols.v1_16_5;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntityDestroyS2CPacket;

public class EntitiesDestroyS2CPacket_1_16_5 implements Packet<ClientPlayPacketListener> {
    private final int[] entities;

    public EntitiesDestroyS2CPacket_1_16_5(PacketByteBuf buf) {
        entities = new int[buf.readVarInt()];
        for (int i = 0; i < entities.length; i++) {
            entities[i] = buf.readVarInt();
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        for (int entityId : entities) {
            listener.onEntityDestroy(new EntityDestroyS2CPacket(entityId));
        }
    }
}
