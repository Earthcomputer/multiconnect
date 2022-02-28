package net.earthcomputer.multiconnect.protocols.generic;

@FunctionalInterface
public interface IRegistryUpdateListener<T> {
    void onUpdate(T thing, boolean inPlace, RegistryMutator.RegistryBuilderSupplier builderSupplier);

    default boolean callOnRestore() {
        return false;
    }
}
