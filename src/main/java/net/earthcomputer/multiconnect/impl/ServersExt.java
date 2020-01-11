package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.EnumProtocol;

import java.util.HashMap;
import java.util.Map;

public class ServersExt {

    public Map<String, ServerExt> servers = new HashMap<>();

    public static class ServerExt {

        public int forcedProtocol = EnumProtocol.AUTO.getValue();

    }

}
