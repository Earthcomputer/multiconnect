package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IRegistryUpdateListener;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(StatType.class)
public class MixinStatType<T> {

    @Shadow @Final private Map<T, Stat<T>> stats;
    @Unique private final Map<T, Stat<T>> removedStats = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(Registry<T> registry, CallbackInfo ci) {
        if (registry instanceof ISimpleRegistry) {
            ISimpleRegistry<T> iregistry = (ISimpleRegistry<T>) registry;

            iregistry.multiconnect_addRegisterListener((new IRegistryUpdateListener<>() {
                @Override
                public void onUpdate(T thing, boolean inPlace, RegistryMutator.RegistryBuilderSupplier builderSupplier) {
                    Stat<T> stat = removedStats.remove(thing);
                    if (stat != null) {
                        stats.put(thing, stat);
                    }
                }

                @Override
                public boolean callOnRestore() {
                    return true;
                }
            }));
            iregistry.multiconnect_addUnregisterListener((new IRegistryUpdateListener<>() {
                @Override
                public void onUpdate(T thing, boolean inPlace, RegistryMutator.RegistryBuilderSupplier builderSupplier) {
                    Stat<T> stat = stats.remove(thing);
                    if (stat != null) {
                        removedStats.put(thing, stat);
                    }
                }

                @Override
                public boolean callOnRestore() {
                    return true;
                }
            }));
        }
    }

}
