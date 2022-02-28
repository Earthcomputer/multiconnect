package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.util.registry.RegistryKey;

import java.util.List;
import java.util.Set;

public interface ISimpleRegistry<T> {
    void multiconnect_unfreeze();

    boolean multiconnect_isFrozen();

    Set<RegistryKey<T>> multiconnect_getRealEntries();

    void multiconnect_lockRealEntries();

    void multiconnect_clear();

    void multiconnect_addRegisterListener(IRegistryUpdateListener<T> listener);

    List<IRegistryUpdateListener<T>> multiconnect_getRegisterListeners();

    void multiconnect_addUnregisterListener(IRegistryUpdateListener<T> listener);

    List<IRegistryUpdateListener<T>> multiconnect_getUnregisterListeners();

    void multiconnect_dump();
}
