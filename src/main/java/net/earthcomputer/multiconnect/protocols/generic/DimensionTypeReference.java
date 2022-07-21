package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

public record DimensionTypeReference(ResourceLocation value) {
    public DimensionType getValue(RegistryAccess registryAccess) {
        return registryAccess.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY).get(value);
    }
}
