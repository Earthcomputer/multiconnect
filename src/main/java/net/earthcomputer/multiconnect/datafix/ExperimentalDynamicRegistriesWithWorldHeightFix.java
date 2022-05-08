package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Collectors;

public class ExperimentalDynamicRegistriesWithWorldHeightFix extends DataFix {
    private static final Logger LOGGER = LogManager.getLogger();

    private final NbtCompound dynamicRegistries;

    public ExperimentalDynamicRegistriesWithWorldHeightFix(Schema outputSchema, boolean changesType, String version) {
        super(outputSchema, changesType);

        Identifier resource = new Identifier("multiconnect", "dynamic_registries/" + version + ".nbt");

        NbtCompound value;
        try (InputStream input = MinecraftClient.getInstance().getResourceManager().getResource(resource).getInputStream()) {
            value = NbtIo.readCompressed(input);
        } catch (IOException e) {
            LOGGER.error(() -> "Error reading resource " + resource, e);
            value = new NbtCompound();
        }
        this.dynamicRegistries = value;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(
            fixTypeEverywhereTyped(
                "RegistryManagerFix" + DataFixUtils.getVersion(getOutputSchema().getVersionKey()),
                getInputSchema().getType(MulticonnectDFU.REGISTRY_MANAGER),
                getOutputSchema().getType(MulticonnectDFU.REGISTRY_MANAGER),
                typed -> {
                    Dynamic<?> destRegistries = new Dynamic<>(NbtOps.INSTANCE, dynamicRegistries.copy());
                    copyDimensionHeights(typed.get(DSL.remainderFinder()), destRegistries);
                    return typed.set(DSL.remainderFinder(), destRegistries.convert(typed.getOps()));
                }
            ),
            fixTypeEverywhereTyped(
                "DimensionFix" + DataFixUtils.getVersion(getOutputSchema().getVersionKey()),
                getInputSchema().getType(MulticonnectDFU.DIMENSION),
                getOutputSchema().getType(MulticonnectDFU.DIMENSION),
                typed -> typed.update(DSL.remainderFinder(), old -> updateDimensionType(old, typed.getOps()))
            )
        );
    }

    private void copyDimensionHeights(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        Dynamic<?> fromDimTypes = fromDynamic.get("minecraft:dimension_type").orElseEmptyMap();
        Dynamic<?> toDimTypes = toDynamic.get("minecraft:dimension_type").orElseEmptyMap();
        var fromMap = fromDimTypes.get("value").orElseEmptyList().asStream().collect(Collectors.groupingBy(
                dynamic -> dynamic.get("name").asString(""),
                Collectors.reducing(null, (a, b) -> a == null ? b : a)
        ));
        toDimTypes.get("value").orElseEmptyList().asStream().forEach(toDimension -> {
            String dimensionName = toDimension.get("name").asString("");
            Dynamic<?> fromDimension = fromMap.get(dimensionName);
            Dynamic<?> fromElement = fromDimension.get("element").orElseEmptyMap();
            Dynamic<?> toElement = toDimension.get("element").orElseEmptyMap();
            toElement.set("logical_height", fromElement.createInt(fromElement.get("logical_height").asInt(256)));
            toElement.set("height", fromElement.createInt(fromElement.get("height").asInt(256)));
            toElement.set("min_y", fromElement.createInt(fromElement.get("min_y").asInt(0)));
        });
    }

    private Dynamic<?> updateDimensionType(Dynamic<?> oldDimensionType, DynamicOps<?> ops) {
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
