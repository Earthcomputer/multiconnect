package net.earthcomputer.multiconnect.impl;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import org.jetbrains.annotations.NotNull;

public class PacketSystem {
    @NotNull
    public static <T> T defaultConstruct(Class<T> type) {
        // TODO
        return null;
    }

    @NotNull
    public static Packet<?> asPacket(int protocol, Object packet) {
        // TODO
        return null;
    }

    public static void sendToServer(ClientPlayNetworkHandler networkHandler, int protocol, Object packet) {
        // TODO
    }

    public static void sendToClient(ClientPlayNetworkHandler networkHandler, int protocol, Object packet) {
        // TODO
    }
}
