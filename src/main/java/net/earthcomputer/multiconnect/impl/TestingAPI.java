package net.earthcomputer.multiconnect.impl;

import com.google.common.annotations.VisibleForTesting;

import java.util.function.Consumer;

public class TestingAPI {
    private static Consumer<Throwable> unexpectedDisconnectListener = t -> {};

    @VisibleForTesting
    public static void setUnexpectedDisconnectListener(Consumer<Throwable> listener) {
        unexpectedDisconnectListener = listener;
    }

    public static void onUnexpectedDisconnect(Throwable throwable) {
        unexpectedDisconnectListener.accept(throwable);
    }
}
