package net.earthcomputer.multiconnect.protocols.generic;

import com.google.gson.Gson;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.packs.VanillaPackResources;
import org.jetbrains.annotations.Contract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

public final class TagLoader {
    private TagLoader() {}

    private static final Gson GSON = new Gson();
    private static final ResourceManager DATA_LOADER = Util.make(() -> {
        VanillaPackResources resources = new VanillaPackResources(ServerPacksSource.BUILT_IN_METADATA, "minecraft");
        return new MultiPackResourceManager(PackType.SERVER_DATA, List.of(resources));
    });

    private static List<Tag<ResourceLocation>> loadTags(ResourceKey<? extends Registry<?>> registry) {
        String registryName;
        if (registry == Registry.BLOCK_REGISTRY) {
            registryName = "blocks";
        } else if (registry == Registry.ITEM_REGISTRY) {
            registryName = "items";
        } else if (registry == Registry.FLUID_REGISTRY) {
            registryName = "fluids";
        } else if (registry == Registry.ENTITY_TYPE_REGISTRY) {
            registryName = "entity_types";
        } else if (registry == Registry.GAME_EVENT_REGISTRY) {
            registryName = "game_events";
        } else {
            registryName = registry.location().getPath();
        }
        String startingPath = "tags/" + registryName;
        Map<ResourceLocation, Resource> tagPaths = DATA_LOADER.listResources(startingPath, path -> path.getPath().endsWith(".json"));

        List<Tag<String>> unresolvedTags = tagPaths.entrySet().stream().map(entry -> {
            ResourceLocation tagPath = entry.getKey();
            String path = tagPath.getPath();
            ResourceLocation tagName = new ResourceLocation(tagPath.getNamespace(), path.substring(startingPath.length() + 1, path.length() - 5));
            // load the tag
            Resource resource = entry.getValue();
            try (Reader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
                TagJson tagJson = GsonHelper.fromJson(GSON, reader, TagJson.class);
                if (tagJson == null) {
                    throw new IllegalStateException("Illegal built-in tag " + tagName);
                }
                return new Tag<>(tagName, tagJson.values);
            } catch (IOException e) {
                throw new IllegalStateException("Could not load built-in tag " + tagName);
            }
        }).toList();

        // resolve sub-tags
        Map<ResourceLocation, Tag<String>> tagsById = unresolvedTags.stream()
                .collect(Collectors.toMap(Tag::name, Function.identity()));
        Set<ResourceLocation> seenTags = new HashSet<>();
        for (Tag<String> tag : unresolvedTags) {
            resolveTag(tag, tagsById, seenTags);
        }

        // now sub-tags are resolved, convert all the string values to identifiers
        return unresolvedTags.stream()
                .map(tag -> new Tag<>(tag.name(), tag.values().stream().map(ResourceLocation::new).toList()))
                .toList();
    }

    private static void resolveTag(Tag<String> tag, Map<ResourceLocation, Tag<String>> tagsById, Set<ResourceLocation> seenTags) {
        if (!seenTags.add(tag.name())) {
            throw new IllegalStateException("Detected cycle in tag " + tag.name());
        }

        List<String> values = tag.values();
        for (int i = values.size() - 1; i >= 0; i--) {
            String value = values.get(i);
            if (value.startsWith("#")) {
                values.remove(i);
                ResourceLocation subTagId = new ResourceLocation(value.substring(1));
                Tag<String> subTag = tagsById.get(subTagId);
                if (subTag == null) {
                    throw new IllegalStateException("Unknown tag " + subTagId);
                }
                resolveTag(subTag, tagsById, seenTags);
                values.addAll(i, subTag.values());
            }
        }

        seenTags.remove(tag.name());
    }

    private static final List<Tag<ResourceLocation>> BLOCKS = loadTags(Registry.BLOCK_REGISTRY);
    private static final List<Tag<ResourceLocation>> ITEMS = loadTags(Registry.ITEM_REGISTRY);
    private static final List<Tag<ResourceLocation>> FLUIDS = loadTags(Registry.FLUID_REGISTRY);
    private static final List<Tag<ResourceLocation>> ENTITY_TYPES = loadTags(Registry.ENTITY_TYPE_REGISTRY);
    private static final List<Tag<ResourceLocation>> GAME_EVENTS = loadTags(Registry.GAME_EVENT_REGISTRY);
    private static final List<Tag<ResourceLocation>> INSTRUMENTS = loadTags(Registry.INSTRUMENT_REGISTRY);
    private static final List<Tag<ResourceLocation>> PAINTING_VARIANTS = loadTags(Registry.PAINTING_VARIANT_REGISTRY);
    private static final List<Tag<ResourceLocation>> BANNER_PATTERNS = loadTags(Registry.BANNER_PATTERN_REGISTRY);
    private static final List<Tag<ResourceLocation>> POINT_OF_INTEREST_TYPES = loadTags(Registry.POINT_OF_INTEREST_TYPE_REGISTRY);
    private static final List<Tag<ResourceLocation>> CAT_VARIANTS = loadTags(Registry.CAT_VARIANT_REGISTRY);
    private static final List<Tag<ResourceLocation>> BIOMES = loadTags(Registry.BIOME_REGISTRY);

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> blocks() {
        return convertTags(Registry.BLOCK, BLOCKS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> items() {
        return convertTags(Registry.ITEM, ITEMS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> fluids() {
        return convertTags(Registry.FLUID, FLUIDS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> entityTypes() {
        return convertTags(Registry.ENTITY_TYPE, ENTITY_TYPES);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> gameEvents() {
        return convertTags(Registry.GAME_EVENT, GAME_EVENTS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> instruments() {
        return convertTags(Registry.INSTRUMENT, INSTRUMENTS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> paintingVariants() {
        return convertTags(Registry.PAINTING_VARIANT, PAINTING_VARIANTS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> bannerPatterns() {
        return convertTags(Registry.BANNER_PATTERN, BANNER_PATTERNS);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> pointOfInterestTypes() {
        return convertTags(Registry.POINT_OF_INTEREST_TYPE, POINT_OF_INTEREST_TYPES);
    }

    @Contract("-> new")
    public static Map<ResourceLocation, IntList> catVariants() {
        return convertTags(Registry.CAT_VARIANT, CAT_VARIANTS);
    }

    @Contract("_ -> new")
    public static Map<ResourceLocation, IntList> biomes(RegistryAccess registryAccess) {
        return convertTags(registryAccess.registryOrThrow(Registry.BIOME_REGISTRY), BIOMES);
    }

    @Contract("_, _ -> new")
    private static <T> Map<ResourceLocation, IntList> convertTags(Registry<T> registry, List<Tag<ResourceLocation>> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::name, tag -> {
            IntList ids = new IntArrayList(tag.values().size());
            for (ResourceLocation name : tag.values()) {
                T value = registry.get(name);
                if (value != null) {
                    ids.add(registry.getId(value));
                }
            }
            return ids;
        }));
    }

    private record Tag<V>(ResourceLocation name, List<V> values) {}

    private static class TagJson {
        private List<String> values;
    }
}
