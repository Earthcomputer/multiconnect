package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public abstract class OldExperimentalRegistryAccessFix extends AbstractRegistryAccessFix {
    public OldExperimentalRegistryAccessFix(Schema outputSchema, boolean changesType, String version) {
        super(outputSchema, changesType, version);
    }

    protected Dynamic<?> updateDimensionType(Dynamic<?> oldDimensionType, DynamicOps<?> ops) {
        String oldId = new ResourceLocation(oldDimensionType.asString().result().orElseGet(() -> {
            return oldDimensionType.get("name").asString().result().orElseGet(() -> {
                return oldDimensionType.get("effects").asString("minecraft:overworld");
            });
        })).toString();

        CompoundTag result = null;

        if (registryAccesses.contains("minecraft:dimension_type", Tag.TAG_COMPOUND)) {
            CompoundTag dimensionTypeRegistry = registryAccesses.getCompound("minecraft:dimension_type");
            ListTag entries = dimensionTypeRegistry.getList("value", Tag.TAG_COMPOUND);
            for (int i = 0; i < entries.size(); i++) {
                CompoundTag entry = entries.getCompound(i);
                if (oldId.equals(entry.getString("name"))) {
                    result = entry.getCompound("element");
                    break;
                }
            }
            if (result == null) {
                result = entries.getCompound(0).getCompound("element");
            }
        } else {
            ListTag dimensions = registryAccesses.getList("dimension", Tag.TAG_COMPOUND);
            for (int i = 0; i < dimensions.size(); i++) {
                CompoundTag dimension = dimensions.getCompound(i);
                if (oldId.equals(dimension.getString("name"))) {
                    result = dimension;
                    break;
                }
            }
            if (result == null) {
                result = dimensions.getCompound(0);
            }
        }

        return new Dynamic<>(NbtOps.INSTANCE, result).convert(ops);
    }

    @Override
    protected Dynamic<?> updateRegistryAccess(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        return updateBiomes(fromDynamic, toDynamic);
    }
}
