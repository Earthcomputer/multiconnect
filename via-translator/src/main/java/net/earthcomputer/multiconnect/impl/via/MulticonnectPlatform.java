package net.earthcomputer.multiconnect.impl.via;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.earthcomputer.multiconnect.impl.via.provider.MulticonnectViaConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class MulticonnectPlatform implements ViaPlatform<UUID> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final java.util.logging.Logger JAVA_LOGGER = new java.util.logging.Logger("multiconnect", null) {
        @Override
        public void log(LogRecord record) {
            this.log(record.getLevel(), record.getMessage());
        }

        @Override
        public void log(Level level, String msg) {
            if (level == Level.FINE) {
                LOGGER.debug(msg);
            } else if (level == Level.WARNING) {
                LOGGER.warn(msg);
            } else if (level == Level.SEVERE) {
                LOGGER.error(msg);
            } else if (level == Level.INFO) {
                LOGGER.info(msg);
            } else {
                LOGGER.trace(msg);
            }
        }

        @Override
        public void log(Level level, String msg, Object param1) {
            if (level == Level.FINE) {
                LOGGER.debug(msg, param1);
            } else if (level == Level.WARNING) {
                LOGGER.warn(msg, param1);
            } else if (level == Level.SEVERE) {
                LOGGER.error(msg, param1);
            } else if (level == Level.INFO) {
                LOGGER.info(msg, param1);
            } else {
                LOGGER.trace(msg, param1);
            }
        }

        @Override
        public void log(Level level, String msg, Object[] params) {
            log(level, MessageFormat.format(msg, params));
        }

        @Override
        public void log(Level level, String msg, Throwable params) {
            if (level == Level.FINE) {
                LOGGER.debug(msg, params);
            } else if (level == Level.WARNING) {
                LOGGER.warn(msg, params);
            } else if (level == Level.SEVERE) {
                LOGGER.error(msg, params);
            } else if (level == Level.INFO) {
                LOGGER.info(msg, params);
            } else {
                LOGGER.trace(msg, params);
            }
        }
    };

    private final IMulticonnectTranslatorApi api;

    private final File dataFolder;
    private final MulticonnectViaConfig config;

    public MulticonnectPlatform(IMulticonnectTranslatorApi api) {
        this.api = api;
        dataFolder = api.getConfigDir().toFile();
        config = new MulticonnectViaConfig(new File(dataFolder, "viaversion.yml"));
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return JAVA_LOGGER;
    }

    @Override
    public String getPlatformName() {
        return "multiconnect";
    }

    @Override
    public String getPlatformVersion() {
        return api.getVersion();
    }

    private static final class PluginVersionHolder {
        private PluginVersionHolder() {
        }

        private static final String pluginVersion = computePluginVersion();

        private static String computePluginVersion() {
            try {
                URL resource = Via.class.getClassLoader().getResource(Via.class.getName().replace('.', '/') + ".class");
                if (resource == null) {
                    throw new RuntimeException("Could not find Via class resoruce");
                }
                String resLocation = resource.toString();
                int exclamationIndex = resLocation.indexOf('!');
                if (exclamationIndex == -1) {
                    throw new RuntimeException("Via location is not in a jar file");
                }
                resLocation = resLocation.substring(0, exclamationIndex + 1) + "/fabric.mod.json";
                try {
                    resource = new URL(resLocation);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                try (InputStream is = resource.openStream()) {
                    JsonObject json = new Gson().fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
                    return GsonHelper.getAsString(json, "version");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to read via's fabric.mod.json", e);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    @Override
    public String getPluginVersion() {
        return PluginVersionHolder.pluginVersion;
    }

    private static final ExecutorService ASYNC_EXECUTOR = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors()),
        new ThreadFactoryBuilder().setDaemon(true).setNameFormat("multiconnect async executor #%d").build()
    );

    private static final Executor SYNC_EXECUTOR = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("multiconnect sync executor").build());

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("multiconnect scheduler").build());

    private static Executor syncExecutor() {
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        return server != null ? server : SYNC_EXECUTOR;
    }

    private static <T> PlatformTask<Future<T>> wrapFuture(Future<T> future) {
        return new PlatformTask<>() {
            @Override
            public Future<T> getObject() {
                return future;
            }

            @Override
            public void cancel() {
                future.cancel(false);
            }
        };
    }

    @Override
    public PlatformTask<?> runAsync(Runnable runnable) {
        return wrapFuture(CompletableFuture.runAsync(runnable, ASYNC_EXECUTOR));
    }

    @Override
    public PlatformTask<?> runSync(Runnable runnable) {
        return wrapFuture(CompletableFuture.runAsync(runnable, syncExecutor()));
    }

    @Override
    public PlatformTask<?> runSync(Runnable runnable, long ticks) {
        return wrapFuture(SCHEDULER.schedule(() -> runSync(runnable), ticks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public PlatformTask<?> runRepeatingSync(Runnable runnable, long ticks) {
        return wrapFuture(SCHEDULER.scheduleAtFixedRate(() -> runSync(runnable), 0, ticks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ViaCommandSender[] getOnlinePlayers() {
        return new ViaCommandSender[0];
    }

    @Override
    public void sendMessage(UUID uuid, String message) {
    }

    @Override
    public boolean kickPlayer(UUID uuid, String message) {
        return false;
    }

    @Override
    public boolean isPluginEnabled() {
        return true;
    }

    private final ViaAPI<UUID> viaApi = new ViaAPIBase<>() {};

    @Override
    public ViaAPI<UUID> getApi() {
        return viaApi;
    }

    @Override
    public ViaVersionConfig getConf() {
        return config;
    }

    @Override
    public ConfigurationProvider getConfigurationProvider() {
        return config;
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public void onReload() {
    }

    @Override
    public com.viaversion.viaversion.libs.gson.JsonObject getDump() {
        var dump = new com.viaversion.viaversion.libs.gson.JsonObject();
        var mods = new com.viaversion.viaversion.libs.gson.JsonArray();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            var modJson = new com.viaversion.viaversion.libs.gson.JsonObject();
            modJson.addProperty("id", mod.getMetadata().getId());
            modJson.addProperty("name", mod.getMetadata().getName());
            modJson.addProperty("version", mod.getMetadata().getVersion().getFriendlyString());
            var authors = new com.viaversion.viaversion.libs.gson.JsonArray();
            for (Person author : mod.getMetadata().getAuthors()) {
                var authorJson = new com.viaversion.viaversion.libs.gson.JsonObject();
                authorJson.addProperty("name", author.getName());
                var contact = new com.viaversion.viaversion.libs.gson.JsonObject();
                author.getContact().asMap().forEach(contact::addProperty);
                if (contact.size() != 0) {
                    authorJson.add("contact", contact);
                }
                authors.add(authorJson);
            }
            modJson.add("authors", authors);
            mods.add(modJson);
        }
        dump.add("mods", mods);
        return dump;
    }

    @Override
    public boolean isOldClientsAllowed() {
        return false;
    }

    @Override
    public boolean hasPlugin(String name) {
        return api.isModLoaded(name);
    }

    @Override
    public boolean isProxy() {
        return true;
    }
}
