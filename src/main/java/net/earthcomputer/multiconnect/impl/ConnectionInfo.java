package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.protocols.AbstractProtocol;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.minecraft.SharedConstants;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionInfo {

    public static String ip;
    public static int port = -1;
    public static ConnectionMode globalForcedProtocolVersion = ConnectionMode.AUTO;
    public static int protocolVersion = SharedConstants.getGameVersion().getProtocolVersion();
    public static AbstractProtocol protocol = ProtocolRegistry.latest();
    public static boolean reloadingResources = false;
    public static ReadWriteLock resourceReloadLock = new ReentrantReadWriteLock();

    public static void startReloadingResources() {
        resourceReloadLock.writeLock().lock();
        reloadingResources = true;
    }

    public static void stopReloadingResources() {
        reloadingResources = false;
        resourceReloadLock.writeLock().unlock();
    }

}
