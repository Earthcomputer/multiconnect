package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class CustomPayloadC2SPacket_1_12_2 implements Packet<ServerPlayPacketListener> {

    private String channel;
    private PacketByteBuf data;
    private boolean blocked;

    public CustomPayloadC2SPacket_1_12_2() {}

    public CustomPayloadC2SPacket_1_12_2(String channel, PacketByteBuf data) {
        this.channel = channel;
        this.data = data;
        this.blocked = !channel.startsWith("MC|");
    }

    @Override
    public void read(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(channel);
        buf.writeBytes(data);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }

    public String getChannel() {
        return channel;
    }

    public PacketByteBuf getData() {
        return data;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void unblock() {
        this.blocked = false;
    }
}
