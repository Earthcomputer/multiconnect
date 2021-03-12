package net.earthcomputer.multiconnect.protocols.v1_16_4;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;

public class AckScreenActionC2SPacket_1_16_4 implements Packet<ServerPlayPacketListener> {
    private final int syncId;
    private final short actionId;
    private final boolean accepted;

    public AckScreenActionC2SPacket_1_16_4(int syncId, short actionId, boolean accepted) {
        this.syncId = syncId;
        this.actionId = actionId;
        this.accepted = accepted;
    }

    public AckScreenActionC2SPacket_1_16_4(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeShort(actionId);
        buf.writeByte(accepted ? 1 : 0);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }
}
