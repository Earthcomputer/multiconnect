package net.earthcomputer.multiconnect.transformer;

import net.earthcomputer.multiconnect.api.ThreadSafe;
import net.earthcomputer.multiconnect.protocols.generic.IUserDataHolder;
import net.earthcomputer.multiconnect.protocols.generic.TypedMap;

@FunctionalInterface
public interface OutboundTranslator<T> {
    @ThreadSafe
    void onWrite(TransformerByteBuf buf);

    @ThreadSafe
    default T translate(T from) {
        return from;
    }

    @ThreadSafe
    default void loadUserData(T from, TypedMap into) {
        if (from instanceof IUserDataHolder holder) {
            into.putAll(holder.multiconnect_getUserData());
        }
    }

}
