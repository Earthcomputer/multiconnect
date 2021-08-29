package net.earthcomputer.multiconnect.transformer;

import net.earthcomputer.multiconnect.api.ThreadSafe;

@FunctionalInterface
public interface OutboundTranslator<T> {
    @ThreadSafe
    void onWrite(TransformerByteBuf buf);

    @ThreadSafe
    default T translate(T from) {
        return from;
    }

}
