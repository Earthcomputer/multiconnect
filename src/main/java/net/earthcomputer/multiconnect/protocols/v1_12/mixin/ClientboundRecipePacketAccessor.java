package net.earthcomputer.multiconnect.protocols.v1_12.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;

@Mixin(ClientboundRecipePacket.class)
public interface ClientboundRecipePacketAccessor {
    @Mutable
    @Accessor
    void setRecipes(List<ResourceLocation> recipes);

    @Mutable
    @Accessor
    void setToHighlight(List<ResourceLocation> toHighlight);
}
