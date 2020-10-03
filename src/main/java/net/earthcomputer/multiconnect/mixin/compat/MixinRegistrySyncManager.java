package net.earthcomputer.multiconnect.mixin.compat;

import com.google.common.base.Joiner;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Set;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager", remap = false)
public class MixinRegistrySyncManager {
    @Dynamic
    @ModifyVariable(method = "apply", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    // The JVM doesn't validate this checked exception at the call site, seems to be implicitly allowed in the JLS.
    // See https://stackoverflow.com/questions/12580598/what-parts-of-the-jls-justify-being-able-to-throw-checked-exceptions-as-if-they
    private static CompoundTag onApplyModdedRegistry(CompoundTag mainTag) throws Exception {
        Set<String> registries = mainTag.getKeys();
        if (!registries.isEmpty()) {
            String registriesStr = Joiner.on(", ").join(registries);
            try {
                throw (Exception) Class.forName("net.fabricmc.fabric.impl.registry.sync.RemapException")
                        .getConstructor(String.class)
                        .newInstance("Server contains Fabric-modded registries: " + registriesStr + "! Multiconnect does not support modded registries.");
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
        return mainTag;
    }
}
