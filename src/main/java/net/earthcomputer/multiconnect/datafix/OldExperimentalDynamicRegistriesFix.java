package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

public abstract class OldExperimentalDynamicRegistriesFix extends AbstractDynamicRegistriesFix {
    public OldExperimentalDynamicRegistriesFix(Schema outputSchema, boolean changesType, String version) {
        super(outputSchema, changesType, version);
    }

    protected Dynamic<?> updateDimensionType(Dynamic<?> oldDimensionType, DynamicOps<?> ops) {
        String oldId = new Identifier(oldDimensionType.asString().result().orElseGet(() -> {
            return oldDimensionType.get("name").asString().result().orElseGet(() -> {
                return oldDimensionType.get("effects").asString("minecraft:overworld");
            });
        })).toString();

        NbtCompound result = null;

        if (dynamicRegistries.contains("minecraft:dimension_type", NbtElement.COMPOUND_TYPE)) {
            NbtCompound dimensionTypeRegistry = dynamicRegistries.getCompound("minecraft:dimension_type");
            NbtList entries = dimensionTypeRegistry.getList("value", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < entries.size(); i++) {
                NbtCompound entry = entries.getCompound(i);
                if (oldId.equals(entry.getString("name"))) {
                    result = entry.getCompound("element");
                    break;
                }
            }
            if (result == null) {
                result = entries.getCompound(0).getCompound("element");
            }
        } else {
            NbtList dimensions = dynamicRegistries.getList("dimension", NbtElement.COMPOUND_TYPE);
            for (int i = 0; i < dimensions.size(); i++) {
                NbtCompound dimension = dimensions.getCompound(i);
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
    protected Dynamic<?> updateRegistryManager(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        return updateBiomes(fromDynamic, toDynamic);
    }
}
