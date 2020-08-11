package net.earthcomputer.multiconnect.connect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ServersExt {

    private static final Logger LOGGER = LogManager.getLogger("multiconnect");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "multiconnect/servers_ext.json");
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
            instance.normalize();
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

    private void normalize() {
        servers = servers.entrySet().stream().collect(Collectors.toMap(entry -> ConnectionHandler.normalizeAddress(entry.getKey()), Map.Entry::getValue, (a, b) -> a, HashMap::new));
    }

    public int getForcedProtocol(String address) {
        ServerExt server = servers.get(ConnectionHandler.normalizeAddress(address));
        return server == null ? ConnectionMode.AUTO.getValue() : server.forcedProtocol;
    }

    public boolean hasServer(String address) {
        return servers.containsKey(ConnectionHandler.normalizeAddress(address));
    }

    public ServerExt getServer(String address) {
        return servers.get(ConnectionHandler.normalizeAddress(address));
    }

    public ServerExt getOrCreateServer(String address) {
        return servers.computeIfAbsent(ConnectionHandler.normalizeAddress(address), k -> new ServerExt());
    }

    public static class ServerExt {

        public int forcedProtocol = ConnectionMode.AUTO.getValue();

    }

}
