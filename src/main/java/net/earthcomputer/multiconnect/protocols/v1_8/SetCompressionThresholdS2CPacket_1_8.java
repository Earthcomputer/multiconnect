package net.earthcomputer.multiconnect.protocols.v1_8;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

public class SetCompressionThresholdS2CPacket_1_8 implements Packet<ClientPlayPacketListener> {
    private final int compressionThreshold;

    public SetCompressionThresholdS2CPacket_1_8(PacketByteBuf buf) {
        compressionThreshold = buf.readVarInt();
    }

    @Override
    public void write(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        if (!listener.getConnection().isLocal()) {
            listener.getConnection().setCompressionThreshold(compressionThreshold);
        }
    }
}
