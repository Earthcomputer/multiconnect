package net.earthcomputer.multiconnect.connect;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.text.Text;

import java.util.concurrent.Semaphore;

public class GetProtocolPacketListener implements ClientQueryPacketListener {

    private final ClientConnection connection;
    private final Semaphore semaphore = new Semaphore(0);
    private int protocol;
    private boolean completed = false;
    private boolean failed = false;

    public GetProtocolPacketListener(ClientConnection connection) {
        this.connection = connection;
    }

    @Override
    public void onResponse(QueryResponseS2CPacket packet) {
        protocol = packet.getServerMetadata().getVersion().getProtocolVersion();
        completed = true;
        semaphore.release();
        connection.disconnect(Text.translatable("multiplayer.status.finished"));
    }

    @Override
    public void onPong(QueryPongS2CPacket packet) {
    }

    @Override
    public void onDisconnected(Text reason) {
        if (!completed) {
            completed = true;
            failed = true;
            semaphore.release();
        }
    }

    @Override
    public ClientConnection getConnection() {
        return connection;
    }

    public int getProtocol() {
        return protocol;
    }

    public void await() throws InterruptedException {
        semaphore.acquire();
    }

    public boolean hasFailed() {
        return failed;
    }
}
