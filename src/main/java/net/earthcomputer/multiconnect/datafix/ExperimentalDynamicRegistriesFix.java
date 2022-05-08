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

public class ExperimentalDynamicRegistriesFix extends DataFix {
    private static final Logger LOGGER = LogManager.getLogger();

    private final NbtCompound dynamicRegistries;

    public ExperimentalDynamicRegistriesFix(Schema outputSchema, boolean changesType, String version) {
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
                typed -> typed.set(DSL.remainderFinder(), new Dynamic<>(NbtOps.INSTANCE, dynamicRegistries).convert(typed.getOps()))
            ),
            fixTypeEverywhereTyped(
                "DimensionFix" + DataFixUtils.getVersion(getOutputSchema().getVersionKey()),
                getInputSchema().getType(MulticonnectDFU.DIMENSION),
                getOutputSchema().getType(MulticonnectDFU.DIMENSION),
                typed -> typed.update(DSL.remainderFinder(), old -> updateDimensionType(old, typed.getOps()))
            )
        );
    }
    /**
      * Updates the dimension type with the old dimension type,
      * @param oldDimensionType - The old dimension type
      * @return The updated dimension type
    */
    private Dynamic<?> updateDimensionType(Dynamic<?> oldDimensionType, DynamicOps<?> ops) {
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
}
