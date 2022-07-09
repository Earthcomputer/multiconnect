package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(AxeItem.class)
public class AxeItemMixin {

	@Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
	public void useOnBlock(UseOnContext context, CallbackInfoReturnable<InteractionResult> ci) {
		if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
			ci.setReturnValue(InteractionResult.PASS);
		}
	}
}
