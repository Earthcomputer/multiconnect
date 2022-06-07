package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public record DimensionTypeReference(Identifier value) {
    public DimensionType getValue(DynamicRegistryManager registryManager) {
        return registryManager.get(Registry.DIMENSION_TYPE_KEY).get(value);
    }
}
