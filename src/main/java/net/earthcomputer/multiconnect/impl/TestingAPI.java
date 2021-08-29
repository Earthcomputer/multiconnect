package net.earthcomputer.multiconnect.impl;

import com.google.common.annotations.VisibleForTesting;
import net.earthcomputer.multiconnect.api.ThreadSafe;

import java.util.function.Consumer;

public class TestingAPI {
    private static Consumer<Throwable> unexpectedDisconnectListener = t -> {};

    @VisibleForTesting
    public static void setUnexpectedDisconnectListener(Consumer<Throwable> listener) {
        unexpectedDisconnectListener = listener;
    }

    @ThreadSafe
    public static void onUnexpectedDisconnect(Throwable throwable) {
        unexpectedDisconnectListener.accept(throwable);
    }
}
