package net.earthcomputer.multiconnect.protocols.v1_12_2;

import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;

public class CustomPayloadC2SPacket_1_12_2 implements IPacket<IServerPlayNetHandler> {

    private String channel;
    private PacketBuffer data;

    public CustomPayloadC2SPacket_1_12_2() {}

    public CustomPayloadC2SPacket_1_12_2(String channel, PacketBuffer data) {
        this.channel = channel;
        this.data = data;
    }

    @Override
    public void readPacketData(PacketBuffer buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writePacketData(PacketBuffer buf) {
        buf.writeString(channel);
        buf.writeBytes(data);
    }

    @Override
    public void processPacket(IServerPlayNetHandler listener) {
        throw new UnsupportedOperationException();
    }
}
