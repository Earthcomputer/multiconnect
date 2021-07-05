package net.earthcomputer.multiconnect.protocols.v1_17;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;

public class EntityDestroyS2CPacket_1_17 implements Packet<ClientPlayPacketListener> {
    private final int entityId;

    public EntityDestroyS2CPacket_1_17(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        listener.onEntitiesDestroy(new EntitiesDestroyS2CPacket(entityId));
    }
}
