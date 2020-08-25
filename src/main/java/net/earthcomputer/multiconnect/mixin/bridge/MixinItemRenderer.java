package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.registry.DefaultedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Iterator;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {

    @SuppressWarnings("unchecked")
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/registry/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private Iterator<Item> redirectItemRegistryIterator(DefaultedRegistry<Item> registry) {
        return (Iterator<Item>) DefaultRegistries.DEFAULT_REGISTRIES.get(registry).defaultEntryToRawId.keySet().iterator();
    }

}
