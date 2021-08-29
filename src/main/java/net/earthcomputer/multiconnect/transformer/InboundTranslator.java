package net.earthcomputer.multiconnect.transformer;

import net.earthcomputer.multiconnect.api.ThreadSafe;

@FunctionalInterface
public interface InboundTranslator<STORED> {
    @ThreadSafe
    void onRead(TransformerByteBuf buf);

    @ThreadSafe
    default STORED translate(STORED from) {
        return from;
    }

}
