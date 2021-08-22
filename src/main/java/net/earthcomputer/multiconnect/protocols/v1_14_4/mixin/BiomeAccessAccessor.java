package net.earthcomputer.multiconnect.protocols.v1_14_4.mixin;

import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeAccessType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeAccess.class)
public interface BiomeAccessAccessor {
    @Accessor
    BiomeAccessType getType();
}
