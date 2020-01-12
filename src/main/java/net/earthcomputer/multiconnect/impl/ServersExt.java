package net.earthcomputer.multiconnect.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.earthcomputer.multiconnect.api.EnumProtocol;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.ServerAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public final class ServersExt {

    private static final Logger LOGGER = LogManager.getLogger("multiconnect");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File configFile = new File(FabricLoader.getInstance().getConfigDirectory(), "multiconnect/servers_ext.json");
    private static ServersExt instance;

    public static ServersExt getInstance() {
        if (instance == null) {
            if (configFile.exists()) {
                try (FileReader reader = new FileReader(configFile)) {
                    instance = GSON.fromJson(reader, ServersExt.class);
                } catch (IOException e) {
                    LOGGER.error("Failed to load extra server data", e);
                }
            }
            if (instance == null) {
                instance = new ServersExt();
            }
        }
        return instance;
    }

    public static void save() {
        //noinspection ResultOfMethodCallIgnored
        configFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(getInstance(), writer);
            writer.flush();
        } catch (IOException e) {
            LOGGER.error("Failed to save extra server data", e);
        }
    }

    private ServersExt() {}

    private Map<String, ServerExt> servers = new HashMap<>();

    public int getForcedProtocol(String address) {
        ServerExt server = servers.get(normalizeAddress(address));
        return server == null ? -1 : server.forcedProtocol;
    }

    public boolean hasServer(String address) {
        return servers.containsKey(normalizeAddress(address));
    }

    public ServerExt getServer(String address) {
        return servers.get(normalizeAddress(address));
    }

    public ServerExt getOrCreateServer(String address) {
        return servers.computeIfAbsent(normalizeAddress(address), k -> new ServerExt());
    }

    private static String normalizeAddress(String address) {
        ServerAddress addr = ServerAddress.parse(address);
        return addr.getAddress() + ":" + addr.getPort();
    }

    public static class ServerExt {

        public int forcedProtocol = EnumProtocol.AUTO.getValue();

    }

}
