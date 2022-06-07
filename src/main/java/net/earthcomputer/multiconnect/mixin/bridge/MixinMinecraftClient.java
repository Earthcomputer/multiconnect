package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @ModifyVariable(method = "reloadSearchProvider", at = @At("HEAD"), argsOnly = true)
    private List<?> modifySearchProvider(List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof ItemStack stack) {
                if (!PacketSystem.doesServerKnow(Registry.ITEM, stack.getItem())) {
                    values.remove(i--);
                }
            }
        }
        return values;
    }
}
