package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class EntityS2CPacket_1_16_4 implements Packet<ClientPlayPacketListener> {
    public EntityS2CPacket_1_16_4(PacketByteBuf buf) {
        buf.readVarInt(); // entity id
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        // nothing to do
    }
}
