package net.earthcomputer.multiconnect.mixin.bridge;

import com.google.common.collect.Iterators;
import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.minecraft.core.Registry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Iterator;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin {
    @ModifyVariable(method = "fillItemList",
            at = @At(value = "STORE", ordinal = 0),
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/core/Registry;ITEM:Lnet/minecraft/core/DefaultedRegistry;", ordinal = 0)),
            ordinal = 0)
    private Iterator<Item> modifyItemIterator(Iterator<Item> itr) {
        return Iterators.filter(itr, item -> MultiConnectAPI.instance().doesServerKnow(Registry.ITEM, item));
    }
}
