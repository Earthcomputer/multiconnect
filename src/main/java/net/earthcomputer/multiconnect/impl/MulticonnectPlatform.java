package net.earthcomputer.multiconnect.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.logging.LogUtils;
import com.viaversion.viaversion.ViaAPIBase;
import com.viaversion.viaversion.api.ViaAPI;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.configuration.ConfigurationProvider;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;
import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.platform.ViaPlatform;
import com.viaversion.viaversion.libs.gson.JsonObject;
import net.earthcomputer.multiconnect.provider.MulticonnectViaConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import org.slf4j.Logger;

import java.io.File;
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
        return FabricLoader.getInstance().getModContainer("multiconnect")
            .orElseThrow(() -> new RuntimeException("Could not find multiconnect mod container"))
            .getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public String getPluginVersion() {
        return "4.4.3-SNAPSHOT"; // TODO
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

    private final ViaAPI<UUID> api = new ViaAPIBase<>() {};

    @Override
    public ViaAPI<UUID> getApi() {
        return api;
    }

    private final File dataFolder = FabricLoader.getInstance().getConfigDir().resolve("multiconnect").toFile();
    private final MulticonnectViaConfig config = new MulticonnectViaConfig(new File(dataFolder, "viaversion.yml"));

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
    public JsonObject getDump() {
        return null; // TODO
    }

    @Override
    public boolean isOldClientsAllowed() {
        return false;
    }

    @Override
    public boolean hasPlugin(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    @Override
    public boolean isProxy() {
        return true;
    }
}
