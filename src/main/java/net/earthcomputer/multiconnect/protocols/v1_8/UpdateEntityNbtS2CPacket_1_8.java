package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class UpdateEntityNbtS2CPacket_1_8 implements Packet<ClientPlayPacketListener> {
    @Override
    public void read(PacketByteBuf buf) {
        buf.readVarInt(); // entity id
        buf.readCompoundTag(); // nbt
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        // packet appears to do nothing
    }
}
