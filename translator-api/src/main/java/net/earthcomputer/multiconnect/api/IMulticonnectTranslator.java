package net.earthcomputer.multiconnect.api;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public interface IMulticonnectTranslator {
    default int priority() {
        return 0;
    }

    boolean isApplicableInEnvironment(IMulticonnectTranslatorApi api);

    void init(IMulticonnectTranslatorApi api);
    void inject(Channel channel);
    void postPipelineModifiers(Channel channel);

    boolean doesServerKnow(String registry, String entry);
    void sendStringCustomPayload(Channel channel, String payloadChannel, ByteBuf payload) throws Exception;
}
