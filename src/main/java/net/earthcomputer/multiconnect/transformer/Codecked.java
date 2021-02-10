package net.earthcomputer.multiconnect.transformer;

import com.mojang.serialization.Codec;

public final class Codecked<T> {
    private final Codec<T> codec;
    private T value;

    public Codecked(Codec<T> codec, T value) {
        this.codec = codec;
        this.value = value;
    }

    public Codec<T> getCodec() {
        return codec;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
