package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.Iterators;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Iterator;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @ModifyVariable(method = "initializeSearchableContainers",
            at = @At(value = "STORE", ordinal = 0),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/registry/Registry;ITEM:Lnet/minecraft/util/registry/DefaultedRegistry;", ordinal = 0)),
            ordinal = 0)
    private Iterator<Item> modifyItemIterator(Iterator<Item> itr) {
        return Iterators.filter(itr, item -> MultiConnectAPI.instance().doesServerKnow(Registry.ITEM, item));
    }
}
