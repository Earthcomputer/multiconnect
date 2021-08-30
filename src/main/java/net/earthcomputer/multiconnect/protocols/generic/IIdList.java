package net.earthcomputer.multiconnect.protocols.generic;


import net.earthcomputer.multiconnect.api.ThreadSafe;
import org.jetbrains.annotations.Nullable;

public interface IIdList<T> {

    void multiconnect_clear();

    Iterable<Integer> multiconnect_ids();

    @ThreadSafe
    void multiconnect_setDefaultValue(@Nullable T defaultValue);

}
