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
import java.util.function.UnaryOperator;
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

    private Dynamic<?> updateRegistry(
        Dynamic<?> fromDynamic,
        Dynamic<?> toDynamic,
        String registryName,
        UnaryOperator<String> nameRemapper,
        UnaryOperator<Dynamic<?>> translator
    ) {
        Dynamic<?> oldRegistry = fromDynamic.get(registryName).orElseEmptyMap().get("value").orElseEmptyList();
        Dynamic<?> newRegistry = toDynamic.get(registryName).orElseEmptyMap().get("value").orElseEmptyList();
        // iterate over old registry, find new element for each old element if possible
        Map<String, Dynamic<?>> newElementByName = newRegistry.asStream().collect(Collectors.toMap(
            entry -> entry.get("name").asString(""),
            Function.identity()
        ));
        IntSet takenIds = new IntOpenHashSet();
        List<Dynamic<?>> newElementList = oldRegistry.asStream()
            .map(oldEntry -> {
                int id = oldEntry.get("id").asInt(0);
                takenIds.add(id);
                String name = nameRemapper.apply(oldEntry.get("name").asString(""));
                Dynamic<?> newEntry = newElementByName.remove(name);
                if (newEntry == null) {
                    // translate the element if new element not found by name
                    Dynamic<?> oldElement = oldEntry.get("element").orElseEmptyMap();
                    Dynamic<?> newElement = translator.apply(oldElement);
                    newEntry = oldEntry.set("element", newElement).set("name", oldEntry.createString(name));
                } else {
                    newEntry = newEntry.set("id", newEntry.createInt(id));
                }
                return newEntry;
            }).collect(Collectors.toCollection(ArrayList::new));

        // for each new element that is not in the old elements list, allocate a new id to it
        int nextId = -1;
        for (Dynamic<?> element : newElementByName.values()) {
            do {
                nextId++;
            } while (takenIds.contains(nextId));

            element = element.set("id", element.createInt(nextId));
            newElementList.add(element);
        }

        return toDynamic.update(registryName, dynamic -> {
            return dynamic.set("value", toDynamic.createList(newElementList.stream()));
        });
    }

    protected Dynamic<?> updateBiomes(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        return updateRegistry(fromDynamic, toDynamic, "minecraft:worldgen/biome", this::mapBiomeName, this::translateBiome);
    }

    protected String mapBiomeName(String biomeName) {
        return biomeName;
    }

    protected Dynamic<?> translateBiome(Dynamic<?> fromDynamic) {
        return fromDynamic;
    }

    protected Dynamic<?> updateChatTypes(Dynamic<?> fromDynamic, Dynamic<?> toDynamic) {
        return updateRegistry(fromDynamic, toDynamic, "minecraft:chat_type", this::mapChatTypeName, this::translateChatType);
    }

    protected String mapChatTypeName(String chatTypeName) {
        return chatTypeName;
    }

    protected Dynamic<?> translateChatType(Dynamic<?> fromDynamic) {
        return fromDynamic;
    }
}
