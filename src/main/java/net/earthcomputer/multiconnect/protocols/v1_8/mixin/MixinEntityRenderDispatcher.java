package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.BoatRenderer_1_8;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {
    @Unique
    private BoatRenderer_1_8 boatRenderer;

    @SuppressWarnings("unchecked")
    @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
    private <T extends Entity> void onGetRenderer(T entity, CallbackInfoReturnable<EntityRenderer<? super T>> ci) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8 && entity instanceof BoatEntity) {
            ci.setReturnValue((EntityRenderer<? super T>) boatRenderer);
        }
    }

    @Inject(method = "apply", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void onApply(ResourceManager manager, CallbackInfo ci, EntityRendererFactory.Context context) {
        boatRenderer = new BoatRenderer_1_8(context);
    }
}
