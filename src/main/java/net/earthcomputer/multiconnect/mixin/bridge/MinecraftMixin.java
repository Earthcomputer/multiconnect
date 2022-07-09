package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyVariable(method = "populateSearchTree", at = @At("HEAD"), argsOnly = true)
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
