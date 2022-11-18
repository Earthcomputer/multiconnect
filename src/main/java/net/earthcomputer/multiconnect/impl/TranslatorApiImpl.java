package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.function.Consumer;

public class TranslatorApiImpl implements IMulticonnectTranslatorApi {
    @Override
    public int getProtocolVersion() {
        return ConnectionInfo.protocolVersion;
    }

    @Override
    public String getVersion() {
        return FabricLoader.getInstance().getModContainer("multiconnect")
            .orElseThrow(() -> new RuntimeException("Could not find multiconnect mod container"))
            .getMetadata().getVersion().getFriendlyString();
    }

    @Override
    public boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve("multiconnect");
    }

    @Override
    public void scheduleRepeating(int period, Runnable task) {
        MulticonnectScheduler.schedule(period, task);
    }

    @Override
    public <T> void scheduleRepeatingWeak(int period, T owner, Consumer<T> task) {
        MulticonnectScheduler.scheduleWeak(period, owner, task);
    }
}
