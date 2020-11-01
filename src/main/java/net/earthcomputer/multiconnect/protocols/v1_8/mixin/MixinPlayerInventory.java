package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.AbstractList;

@Mixin(PlayerInventory.class)
public class MixinPlayerInventory {
    @Redirect(method = "<init>", slice = @Slice(from = @At(value = "CONSTANT", args = "intValue=1")), at = @At(value = "INVOKE", target = "Lnet/minecraft/util/collection/DefaultedList;ofSize(ILjava/lang/Object;)Lnet/minecraft/util/collection/DefaultedList;", ordinal = 0))
    private <T> DefaultedList<T> redirectOffhandInventory(int size, T def) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            // Create a list which has size 0 but doesn't throw array index out of bounds exceptions.
            //noinspection MixinInnerClass
            return new DefaultedList<T>(new AbstractList<T>() {
                @Override
                public T get(int index) {
                    return def;
                }

                @Override
                public T set(int index, T element) {
                    return def;
                }

                @Override
                public int size() {
                    return 0;
                }
            }, def) {};
        } else {
            return DefaultedList.ofSize(size, def);
        }
    }
}
