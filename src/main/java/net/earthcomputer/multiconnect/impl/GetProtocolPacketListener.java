package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.network.status.IClientStatusNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.server.SPongPacket;
import net.minecraft.network.status.server.SServerInfoPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GetProtocolPacketListener implements IClientStatusNetHandler {

    private NetworkManager connection;
    private volatile boolean completed = false;
    private boolean failed = false;

    public GetProtocolPacketListener(NetworkManager connection) {
        this.connection = connection;
    }

    @Override
    public void handleServerInfo(SServerInfoPacket packet) {
        ConnectionInfo.protocolVersion = packet.getResponse().getVersion().getProtocol();
        connection.sendPacket(new CPingPacket());
    }

    @Override
    public void handlePong(SPongPacket packet) {
        completed = true;
        connection.closeChannel(new TranslationTextComponent("multiplayer.status.finished"));
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
        if (!completed) {
            completed = true;
            failed = true;
        }
    }

    @Override
    public NetworkManager getNetworkManager() {
        return connection;
    }

    public boolean hasCompleted() {
        return completed;
    }

    public boolean hasFailed() {
        return failed;
    }
}
