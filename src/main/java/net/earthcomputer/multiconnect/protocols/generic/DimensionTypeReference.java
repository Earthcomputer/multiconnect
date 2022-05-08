package net.earthcomputer.multiconnect.protocols.generic;

import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;

public record DimensionTypeReference(RegistryEntry<DimensionType> value) {
}
