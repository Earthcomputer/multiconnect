package net.earthcomputer.multiconnect.protocols.generic;

import com.google.gson.Gson;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
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

public final class TagLoader {
    private TagLoader() {}

    private static final Gson GSON = new Gson();
    private static final ResourceManager DATA_LOADER = Util.make(() -> {
        ResourcePackManager packManager = new ResourcePackManager(ResourceType.SERVER_DATA, new VanillaDataPackProvider());
        packManager.scanPacks();
        packManager.setEnabledProfiles(Collections.singletonList("vanilla"));
        return new LifecycledResourceManagerImpl(ResourceType.SERVER_DATA, packManager.createResourcePacks());
    });

    private static List<Tag<Identifier>> loadTags(RegistryKey<? extends Registry<?>> registry) {
        String registryName;
        if (registry == Registry.BLOCK_KEY) {
            registryName = "blocks";
        } else if (registry == Registry.ITEM_KEY) {
            registryName = "items";
        } else if (registry == Registry.FLUID_KEY) {
            registryName = "fluids";
        } else if (registry == Registry.ENTITY_TYPE_KEY) {
            registryName = "entity_types";
        } else if (registry == Registry.GAME_EVENT_KEY) {
            registryName = "game_events";
        } else {
            registryName = registry.getValue().getPath();
        }
        String startingPath = "tags/" + registryName;
        Map<Identifier, Resource> tagPaths = DATA_LOADER.findResources(startingPath, path -> path.getPath().endsWith(".json"));

        List<Tag<String>> unresolvedTags = tagPaths.entrySet().stream().map(entry -> {
            Identifier tagPath = entry.getKey();
            String path = tagPath.getPath();
            Identifier tagName = new Identifier(tagPath.getNamespace(), path.substring(startingPath.length() + 1, path.length() - 5));
            // load the tag
            Resource resource = entry.getValue();
            try (Reader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                TagJson tagJson = JsonHelper.deserialize(GSON, reader, TagJson.class);
                if (tagJson == null) {
                    throw new IllegalStateException("Illegal built-in tag " + tagName);
                }
                return new Tag<>(tagName, tagJson.values);
            } catch (IOException e) {
                throw new IllegalStateException("Could not load built-in tag " + tagName);
            }
        }).toList();

        // resolve sub-tags
        Map<Identifier, Tag<String>> tagsById = unresolvedTags.stream()
                .collect(Collectors.toMap(Tag::name, Function.identity()));
        Set<Identifier> seenTags = new HashSet<>();
        for (Tag<String> tag : unresolvedTags) {
            resolveTag(tag, tagsById, seenTags);
        }

        // now sub-tags are resolved, convert all the string values to identifiers
        return unresolvedTags.stream()
                .map(tag -> new Tag<>(tag.name(), tag.values().stream().map(Identifier::new).toList()))
                .toList();
    }

    private static void resolveTag(Tag<String> tag, Map<Identifier, Tag<String>> tagsById, Set<Identifier> seenTags) {
        if (!seenTags.add(tag.name())) {
            throw new IllegalStateException("Detected cycle in tag " + tag.name());
        }

        List<String> values = tag.values();
        for (int i = values.size() - 1; i >= 0; i--) {
            String value = values.get(i);
            if (value.startsWith("#")) {
                values.remove(i);
                Identifier subTagId = new Identifier(value.substring(1));
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

    private static final List<Tag<Identifier>> BLOCKS = loadTags(Registry.BLOCK_KEY);
    private static final List<Tag<Identifier>> ITEMS = loadTags(Registry.ITEM_KEY);
    private static final List<Tag<Identifier>> FLUIDS = loadTags(Registry.FLUID_KEY);
    private static final List<Tag<Identifier>> ENTITY_TYPES = loadTags(Registry.ENTITY_TYPE_KEY);
    private static final List<Tag<Identifier>> GAME_EVENTS = loadTags(Registry.GAME_EVENT_KEY);
    private static final List<Tag<Identifier>> INSTRUMENTS = loadTags(Registry.INSTRUMENT_KEY);
    private static final List<Tag<Identifier>> PAINTING_VARIANTS = loadTags(Registry.PAINTING_VARIANT_KEY);
    private static final List<Tag<Identifier>> BANNER_PATTERNS = loadTags(Registry.BANNER_PATTERN_KEY);
    private static final List<Tag<Identifier>> POINT_OF_INTEREST_TYPES = loadTags(Registry.POINT_OF_INTEREST_TYPE_KEY);
    private static final List<Tag<Identifier>> CAT_VARIANTS = loadTags(Registry.CAT_VARIANT_KEY);
    private static final List<Tag<Identifier>> BIOMES = loadTags(Registry.BIOME_KEY);

    @Contract("-> new")
    public static Map<Identifier, IntList> blocks() {
        return convertTags(Registry.BLOCK, BLOCKS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> items() {
        return convertTags(Registry.ITEM, ITEMS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> fluids() {
        return convertTags(Registry.FLUID, FLUIDS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> entityTypes() {
        return convertTags(Registry.ENTITY_TYPE, ENTITY_TYPES);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> gameEvents() {
        return convertTags(Registry.GAME_EVENT, GAME_EVENTS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> instruments() {
        return convertTags(Registry.INSTRUMENT, INSTRUMENTS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> paintingVariants() {
        return convertTags(Registry.PAINTING_VARIANT, PAINTING_VARIANTS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> bannerPatterns() {
        return convertTags(Registry.BANNER_PATTERN, BANNER_PATTERNS);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> pointOfInterestTypes() {
        return convertTags(Registry.POINT_OF_INTEREST_TYPE, POINT_OF_INTEREST_TYPES);
    }

    @Contract("-> new")
    public static Map<Identifier, IntList> catVariants() {
        return convertTags(Registry.CAT_VARIANT, CAT_VARIANTS);
    }

    @Contract("_ -> new")
    public static Map<Identifier, IntList> biomes(DynamicRegistryManager registryManager) {
        return convertTags(registryManager.get(Registry.BIOME_KEY), BIOMES);
    }

    @Contract("_, _ -> new")
    private static <T> Map<Identifier, IntList> convertTags(Registry<T> registry, List<Tag<Identifier>> tags) {
        return tags.stream().collect(Collectors.toMap(Tag::name, tag -> {
            IntList ids = new IntArrayList(tag.values().size());
            for (Identifier name : tag.values()) {
                T value = registry.get(name);
                if (value != null) {
                    ids.add(registry.getRawId(value));
                }
            }
            return ids;
        }));
    }

    private record Tag<V>(Identifier name, List<V> values) {}

    private static class TagJson {
        private List<String> values;
    }
}
