package net.earthcomputer.multiconnect.transformer;

@FunctionalInterface
public interface OutboundTranslator<T> {

    void onWrite(TransformerByteBuf buf);

    default T translate(T from) {
        return from;
    }

}
