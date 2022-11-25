package net.earthcomputer.multiconnect.api;

import io.netty.channel.Channel;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface IMulticonnectTranslatorApi {
    int getProtocolVersion();
    String getVersion();
    boolean isModLoaded(String modid);
    Path getConfigDir();
    @Nullable
    Channel getCurrentChannel();

    default void scheduleRepeating(Runnable task) {
        scheduleRepeating(1, task);
    }
    void scheduleRepeating(int period, Runnable task);
    default <T> void scheduleRepeatingWeak(T owner, Consumer<T> task) {
        scheduleRepeatingWeak(1, owner, task);
    }
    <T> void scheduleRepeatingWeak(int period, T owner, Consumer<T> task);
}
