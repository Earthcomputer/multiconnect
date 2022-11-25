package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.api.MultiConnectAPI;
import net.earthcomputer.multiconnect.impl.MulticonnectScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @ModifyVariable(method = "populateSearchTree", at = @At("HEAD"), argsOnly = true)
    private List<?> modifySearchProvider(List<?> values) {
        for (int i = 0; i < values.size(); i++) {
            Object value = values.get(i);
            if (value instanceof ItemStack stack) {
                if (!MultiConnectAPI.instance().doesServerKnow(Registry.ITEM, stack.getItem())) {
                    values.remove(i--);
                }
            }
        }
        return values;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        MulticonnectScheduler.tick();
    }
}
