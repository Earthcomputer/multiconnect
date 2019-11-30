package net.earthcomputer.multiconnect.protocols.v1_13_2.mixin;

import net.earthcomputer.multiconnect.impl.TransformerByteBuf;
import net.minecraft.client.network.packet.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SynchronizeRecipesS2CPacket.class)
public class MixinSynchronizeRecipesPacket {

    @Inject(method = "readRecipe", at = @At("HEAD"))
    private static void onReadRecipe(PacketByteBuf buf, CallbackInfoReturnable<Recipe<?>> ci) {
        if (buf instanceof TransformerByteBuf)
            ((TransformerByteBuf) buf).setUserData(0);
    }

}
