package net.earthcomputer.multiconnect.api;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface IMulticonnectTranslatorApi {
    int getProtocolVersion();
    String getVersion();
    boolean isModLoaded(String modid);
    Path getConfigDir();

    default void scheduleRepeating(Runnable task) {
        scheduleRepeating(1, task);
    }
    void scheduleRepeating(int period, Runnable task);
    default <T> void scheduleRepeatingWeak(T owner, Consumer<T> task) {
        scheduleRepeatingWeak(1, owner, task);
    }
    <T> void scheduleRepeatingWeak(int period, T owner, Consumer<T> task);
}
