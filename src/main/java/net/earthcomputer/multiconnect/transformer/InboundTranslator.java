package net.earthcomputer.multiconnect.transformer;

@FunctionalInterface
public interface InboundTranslator<STORED> {

    void onRead(TransformerByteBuf buf);

    default STORED translate(STORED from) {
        return from;
    }

}
