package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

import java.util.stream.Collectors;

public abstract class NewExperimentalDynamicRegistriesFix extends AbstractDynamicRegistriesFix {
    public NewExperimentalDynamicRegistriesFix(Schema outputSchema, boolean changesType, String version) {
        super(outputSchema, changesType, version);
    }

    @Override
    protected Dynamic<?> updateRegistryManager(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        Dynamic<?> fromDimTypes = fromDynamic.get("minecraft:dimension_type").orElseEmptyMap();
        var fromMap = fromDimTypes.get("value").orElseEmptyList().asStream().collect(Collectors.groupingBy(
                dynamic -> dynamic.get("name").asString(""),
                Collectors.reducing(null, (a, b) -> a == null ? b : a)
        ));
        toDynamic = toDynamic.update("minecraft:dimension_type", toDimTypes -> {
            return toDimTypes.update("value", dynamic -> {
                return dynamic.createList(dynamic.asStream().map(toDimension -> {
                    String dimensionName = toDimension.get("name").asString("");
                    Dynamic<?> fromDimension = fromMap.get(dimensionName);
                    Dynamic<?> fromElement = fromDimension.get("element").orElseEmptyMap();
                    return toDimension.update("element", toElement -> {
                        toElement = toElement.set("logical_height", fromElement.createInt(fromElement.get("logical_height").asInt(256)));
                        toElement = toElement.set("height", fromElement.createInt(fromElement.get("height").asInt(256)));
                        toElement = toElement.set("min_y", fromElement.createInt(fromElement.get("min_y").asInt(0)));
                        return toElement;
                    });
                }));
            });
        });

        return updateBiomes(fromDynamic, toDynamic);
    }

    @Override
    protected Dynamic<?> updateDimensionType(Dynamic<?> oldDimensionType, DynamicOps<?> ops) {
        String oldId = new Identifier(oldDimensionType.asString().result().orElseGet(() -> {
            return oldDimensionType.get("name").asString().result().orElseGet(() -> {
                return oldDimensionType.get("effects").asString("minecraft:overworld");
            });
        })).toString();

        NbtCompound result = null;

        NbtCompound dimensionTypeRegistry = dynamicRegistries.getCompound("minecraft:dimension_type");
        NbtList entries = dimensionTypeRegistry.getList("value", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < entries.size(); i++) {
            NbtCompound entry = entries.getCompound(i);
            if (oldId.equals(entry.getString("name"))) {
                result = entry.getCompound("element").copy();
                break;
            }
        }
        if (result == null) {
            result = entries.getCompound(0).getCompound("element").copy();
        }

        result.putInt("logical_height", oldDimensionType.get("logical_height").asInt(256));
        result.putInt("height", oldDimensionType.get("height").asInt(256));
        result.putInt("min_y", oldDimensionType.get("min_y").asInt(0));

        return new Dynamic<>(NbtOps.INSTANCE, result).convert(ops);
    }
}
