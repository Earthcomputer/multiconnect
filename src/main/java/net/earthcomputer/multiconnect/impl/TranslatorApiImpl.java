package net.earthcomputer.multiconnect.impl;

import io.netty.channel.Channel;
import net.earthcomputer.multiconnect.api.IMulticonnectTranslatorApi;
import net.earthcomputer.multiconnect.mixin.connect.ConnectionAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.RateKickingConnection;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Consumer;

public class TranslatorApiImpl implements IMulticonnectTranslatorApi {
    @Override
    public int getProtocolVersion() {
        return ConnectionInfo.protocolVersion;
    }

    @Override
    public String getVersion() {
        return Multiconnect.getVersion();
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
    @Nullable
    public Channel getCurrentChannel() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return null;
        }
        return ((ConnectionAccessor) connection.getConnection()).getChannel();
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
