package net.earthcomputer.multiconnect.connect;

import java.util.concurrent.Semaphore;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;

public class GetProtocolPacketListener implements ClientStatusPacketListener {

    private final Connection connection;
    private final Semaphore semaphore = new Semaphore(0);
    private int protocol;
    private boolean completed = false;
    private boolean failed = false;

    public GetProtocolPacketListener(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void handleStatusResponse(ClientboundStatusResponsePacket packet) {
        protocol = packet.getStatus().getVersion().getProtocol();
        completed = true;
        semaphore.release();
        connection.disconnect(Component.translatable("multiplayer.status.finished"));
    }

    @Override
    public void handlePongResponse(ClientboundPongResponsePacket packet) {
    }

    @Override
    public void onDisconnect(Component reason) {
        if (!completed) {
            completed = true;
            failed = true;
            semaphore.release();
        }
    }

    @Override
    public Connection getConnection() {
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
