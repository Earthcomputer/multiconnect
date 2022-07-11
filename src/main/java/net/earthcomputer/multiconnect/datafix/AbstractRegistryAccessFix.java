package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractRegistryAccessFix extends DataFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final CompoundTag registryAccesses;

    public AbstractRegistryAccessFix(Schema outputSchema, boolean changesType, String version) {
        super(outputSchema, changesType);

        ResourceLocation resource = new ResourceLocation("multiconnect", "registry_access/" + version + ".nbt");

        CompoundTag value;
        try (InputStream input = Minecraft.getInstance().getResourceManager().getResourceOrThrow(resource).open()) {
            value = NbtIo.readCompressed(input);
        } catch (IOException e) {
            LOGGER.error("Error reading resource " + resource, e);
            value = new CompoundTag();
        }
        this.registryAccesses = value;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq(
            fixTypeEverywhereTyped(
                "RegistryAccessFix" + DataFixUtils.getVersion(getOutputSchema().getVersionKey()),
                getInputSchema().getType(MulticonnectDFU.REGISTRY_ACCESS),
                getOutputSchema().getType(MulticonnectDFU.REGISTRY_ACCESS),
                typed -> {
                    Dynamic<?> destRegistries = new Dynamic<>(NbtOps.INSTANCE, registryAccesses.copy());
                    destRegistries = updateRegistryAccess(typed.get(DSL.remainderFinder()), destRegistries);
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

    protected abstract Dynamic<?> updateRegistryAccess(Dynamic<?> fromDynamic, Dynamic<?> toDynamic);

    protected abstract Dynamic<?> updateDimensionType(Dynamic<?> oldDimensionType, DynamicOps<?> ops);

    protected Dynamic<?> updateBiomes(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        Dynamic<?> oldBiomeRegistry = fromDynamic.get("minecraft:worldgen/biome").orElseEmptyMap().get("value").orElseEmptyList();
        Dynamic<?> newBiomeRegistry = toDynamic.get("minecraft:worldgen/biome").orElseEmptyMap().get("value").orElseEmptyList();
        // iterate over old registry, find new biome for each old biome if possible
        Map<String, Dynamic<?>> newBiomeByName = newBiomeRegistry.asStream().collect(Collectors.toMap(
                entry -> entry.get("name").asString(""),
                Function.identity()
        ));
        IntSet takenBiomeIds = new IntOpenHashSet();
        List<Dynamic<?>> newBiomeList = oldBiomeRegistry.asStream()
                .map(oldEntry -> {
                    int id = oldEntry.get("id").asInt(0);
                    takenBiomeIds.add(id);
                    String name = oldEntry.get("name").asString("");
                    Dynamic<?> newEntry = newBiomeByName.remove(name);
                    if (newEntry == null) {
                        // translate the biome if new biome not found by name
                        Dynamic<?> oldElement = oldEntry.get("element").orElseEmptyMap();
                        Dynamic<?> newElement = translateBiome(oldElement);
                        newEntry = oldEntry.set("element", newElement);
                    } else {
                        newEntry = newEntry.set("id", newEntry.createInt(id));
                    }
                    return newEntry;
                }).collect(Collectors.toCollection(ArrayList::new));

        // for each new biome that is not in the old biomes list, allocate a new id to it
        int nextBiomeId = -1;
        for (Dynamic<?> biome : newBiomeByName.values()) {
            do {
                nextBiomeId++;
            } while (takenBiomeIds.contains(nextBiomeId));

            biome.set("id", biome.createInt(nextBiomeId));
            newBiomeList.add(biome);
        }

        return toDynamic.update("minecraft:worldgen/biome", dynamic -> {
            return dynamic.set("value", toDynamic.createList(newBiomeList.stream()));
        });
    }

    protected abstract Dynamic<?> translateBiome(Dynamic<?> fromDynamic);
}
