package net.earthcomputer.multiconnect.transformer;

import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;

@FunctionalInterface
public interface InboundTranslator<STORED> {
    @ThreadSafe
    void onRead(TransformerByteBuf buf);

    @ThreadSafe
    default STORED translate(STORED from) {
        return from;
    }

    @ThreadSafe
    default void storeUserData(STORED target, TypedMap userData) {
        if (target instanceof IUserDataHolder holder) {
            holder.multiconnect_getUserData().putAll(userData);
        }
    }

}
