package net.earthcomputer.multiconnect.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class MulticonnectConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final MulticonnectConfig INSTANCE = load();

    @Nullable
    public Boolean allowOldUnsignedChat;
    @Nullable
    public Boolean enablePacketRecorder;
    @Nullable
    public Boolean debugKey;

    private static MulticonnectConfig load() {
        MulticonnectConfig config = null;
        Path configFile = configFile();

        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                config = GSON.fromJson(reader, MulticonnectConfig.class);
            } catch (IOException | JsonParseException e) {
                LOGGER.warn("Unable to load multiconnect config", e);
            }
        }

        if (config == null) {
            config = new MulticonnectConfig();
            config.save();
        }
        return config;
    }

    public void save() {
        Path configFile = configFile();
        try {
            Files.createDirectories(configFile.getParent());
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Unable to save multiconnect config", e);
        }
    }

    private static Path configFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("multiconnect").resolve("config.json");
    }
}
