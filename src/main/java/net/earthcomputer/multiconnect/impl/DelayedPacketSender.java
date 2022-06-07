package net.earthcomputer.multiconnect.impl;

@FunctionalInterface
public interface DelayedPacketSender<T> {
    void send(T packet);
}
