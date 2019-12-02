package net.earthcomputer.multiconnect.mixin;

import net.earthcomputer.multiconnect.impl.IItemColors;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.util.IdList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemColors.class)
public abstract class MixinItemColors implements IItemColors {

    @Accessor
    @Override
    public abstract IdList<ItemColorProvider> getProviders();
}
