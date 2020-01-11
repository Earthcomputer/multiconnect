package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.EnumProtocol;
import net.minecraft.client.network.ServerInfo;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public interface IServerInfo {

    Set<ServerInfo> INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());

    EnumProtocol multiconnect_getForcedVersion();

    void multiconnect_setForcedVersion(EnumProtocol forcedVersion);
}
