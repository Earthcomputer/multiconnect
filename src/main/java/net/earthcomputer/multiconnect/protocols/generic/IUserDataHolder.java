package net.earthcomputer.multiconnect.protocols.generic;

import org.jetbrains.annotations.Nullable;

public interface IUserDataHolder {
    TypedMap multiconnect_getUserData();
    default <T> T multiconnect_getUserData(Key<T> key) {
        return multiconnect_getUserData().get(key);
    }
    default <T> T multiconnect_setUserData(Key<T> key, T value) {
        return multiconnect_getUserData().put(key, value);
    }

    @Nullable
    static TypedMap extractUserData(Object object) {
        if (object instanceof IUserDataHolder holder) {
            return holder.multiconnect_getUserData();
        } else {
            return null;
        }
    }
}
