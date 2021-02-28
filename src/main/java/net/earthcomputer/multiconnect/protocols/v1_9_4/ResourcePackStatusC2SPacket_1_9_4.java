package net.earthcomputer.multiconnect.protocols.v1_9_4;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;

public class ResourcePackStatusC2SPacket_1_9_4 implements Packet<ServerPlayPacketListener> {
    private final String hash;
    private final ResourcePackStatusC2SPacket.Status status;

    public ResourcePackStatusC2SPacket_1_9_4(PacketByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    public ResourcePackStatusC2SPacket_1_9_4(String hash, ResourcePackStatusC2SPacket.Status status) {
        if (hash.length() > 40) {
            hash = hash.substring(0, 40);
        }
        this.hash = hash;
        this.status = status;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(hash);
        buf.writeEnumConstant(status);
    }

    @Override
    public void apply(ServerPlayPacketListener listener) {
        throw new UnsupportedOperationException();
    }
}
