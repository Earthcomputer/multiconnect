package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(UnlockRecipesS2CPacket.class)
public interface UnlockRecipesS2CAccessor {
    @Mutable
    @Accessor
    void setRecipeIdsToChange(List<Identifier> recipeIdsToChange);

    @Mutable
    @Accessor
    void setRecipeIdsToInit(List<Identifier> recipeIdsToInit);
}
