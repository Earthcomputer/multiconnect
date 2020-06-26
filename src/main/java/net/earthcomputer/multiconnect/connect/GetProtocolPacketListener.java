package net.earthcomputer.multiconnect.connect;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GetProtocolPacketListener implements ClientQueryPacketListener {

    private ClientConnection connection;
    private int protocol;
    private volatile boolean completed = false;
    private boolean failed = false;

    public GetProtocolPacketListener(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onResponse(QueryResponseS2CPacket packet) {
        protocol = packet.getServerMetadata().getVersion().getProtocolVersion();
        connection.send(new QueryPingC2SPacket());
    }

    @Override
    public void onPong(QueryPongS2CPacket packet) {
        completed = true;
        connection.disconnect(new TranslatableText("multiplayer.status.finished"));
    }

    @Override
    public void onDisconnected(Text reason) {
        if (!completed) {
            completed = true;
            failed = true;
        }
    }

    @Override
    public ClientConnection getConnection() {
        return connection;
    }

    public int getProtocol() {
        return protocol;
    }

    public boolean hasCompleted() {
        return completed;
    }

    public boolean hasFailed() {
        return failed;
    }
}
