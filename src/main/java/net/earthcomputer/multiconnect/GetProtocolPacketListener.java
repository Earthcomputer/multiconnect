package net.earthcomputer.multiconnect;

import net.minecraft.client.network.packet.QueryPongS2CPacket;
import net.minecraft.client.network.packet.QueryResponseS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.server.network.packet.QueryPingC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class GetProtocolPacketListener implements ClientQueryPacketListener {

    private ClientConnection connection;
    private volatile boolean completed = false;
    private boolean failed = false;

    public GetProtocolPacketListener(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onResponse(QueryResponseS2CPacket packet) {
        ConnectionInfo.protocol = packet.getServerMetadata().getVersion().getProtocolVersion();
        connection.send(new QueryPingC2SPacket());
    }

    @Override
    public void onPong(QueryPongS2CPacket packet) {
        completed = true;
        connection.disconnect(new TranslatableText("multiplayer.status.finished"));
    }

    @Override
    public void onDisconnected(Text reason) {
        completed = true;
        failed = true;
    }

    @Override
    public ClientConnection getConnection() {
        return connection;
    }

    public boolean hasCompleted() {
        return completed;
    }

    public boolean hasFailed() {
        return failed;
    }
}
