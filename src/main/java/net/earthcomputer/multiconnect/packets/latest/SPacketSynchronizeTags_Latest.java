package net.earthcomputer.multiconnect.packets.latest;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.CustomFix;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.GlobalData;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;
import net.earthcomputer.multiconnect.protocols.generic.TagLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketSynchronizeTags_Latest implements SPacketSynchronizeTags {
    @Introduce(compute = "computeGroups")
    @CustomFix(value = "fixGroups", recursive = true)
    public List<Group> groups;

    public static List<Group> computeGroups(
            @Argument("blocks") List<Tag> blocks,
            @Argument("items") List<Tag> items,
            @Argument("fluids") List<Tag> fluids,
            @Argument("entities") List<Tag> entities,
            @DefaultConstruct BlockGroup blockGroup,
            @DefaultConstruct ItemGroup itemGroup,
            @DefaultConstruct FluidGroup fluidGroup,
            @DefaultConstruct EntityTypeGroup entityTypeGroup,
            @DefaultConstruct GameEventGroup gameEventGroup
    ) {
        List<Group> ret = new ArrayList<>(5);
        blockGroup.tags = blocks;
        ret.add(blockGroup);
        itemGroup.tags = items;
        ret.add(itemGroup);
        fluidGroup.tags = fluids;
        ret.add(fluidGroup);
        entityTypeGroup.tags = entities;
        ret.add(entityTypeGroup);
        ret.add(gameEventGroup);
        return ret;
    }

    private static final Map<ResourceLocation, Supplier<Group>> REQUIRED_GROUPS = ImmutableMap.<ResourceLocation, Supplier<Group>>builder()
        .put(new ResourceLocation("block"), BlockGroup::create)
        .put(new ResourceLocation("item"), ItemGroup::create)
        .put(new ResourceLocation("fluid"), FluidGroup::create)
        .put(new ResourceLocation("entity_type"), EntityTypeGroup::create)
        .put(new ResourceLocation("game_event"), GameEventGroup::create)
        .put(new ResourceLocation("instrument"), InstrumentGroup::create)
        .put(new ResourceLocation("painting_variant"), PaintingVariantGroup::create)
        .put(new ResourceLocation("banner_pattern"), BannerPatternGroup::create)
        .put(new ResourceLocation("point_of_interest_type"), PointOfInterestTypeGroup::create)
        .put(new ResourceLocation("cat_variant"), CatVariantGroup::create)
        .put(new ResourceLocation("worldgen/biome"), BiomeGroup::create)
        .build();

    public static List<Group> fixGroups(List<Group> groups) {
        var groupsMap = groups.stream().collect(Collectors.groupingBy(group -> group.id));
        List<Group> ret = new ArrayList<>(REQUIRED_GROUPS.size());
        REQUIRED_GROUPS.forEach((id, supplier) -> {
            List<Group> group = groupsMap.get(id);
            if (group == null) {
                Group g = supplier.get();
                g.id = id;
                ret.add(g);
            } else {
                ret.addAll(group);
            }
        });
        return ret;
    }

    @Polymorphic
    @MessageVariant
    public static abstract class Group {
        public ResourceLocation id;
    }

    @Polymorphic(stringValue = "block")
    @MessageVariant
    public static class BlockGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static BlockGroup create() {
            BlockGroup ret = new BlockGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.BLOCK, TagLoader::blocks, tags);
        }
    }

    @Polymorphic(stringValue = "item")
    @MessageVariant
    public static class ItemGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static ItemGroup create() {
            ItemGroup ret = new ItemGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.ITEM, TagLoader::items, tags);
        }
    }

    @Polymorphic(stringValue = "fluid")
    @MessageVariant
    public static class FluidGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static FluidGroup create() {
            FluidGroup ret = new FluidGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.FLUID, TagLoader::fluids, tags);
        }
    }

    @Polymorphic(stringValue = "entity_type")
    @MessageVariant
    public static class EntityTypeGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static EntityTypeGroup create() {
            EntityTypeGroup ret = new EntityTypeGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.ENTITY_TYPE, TagLoader::entityTypes, tags);
        }
    }

    @Polymorphic(stringValue = "game_event")
    @MessageVariant
    public static class GameEventGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static GameEventGroup create() {
            GameEventGroup ret = new GameEventGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.GAME_EVENT, TagLoader::gameEvents, tags);
        }
    }

    @Polymorphic(stringValue = "instrument")
    @MessageVariant
    public static class InstrumentGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static InstrumentGroup create() {
            InstrumentGroup ret = new InstrumentGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.INSTRUMENT, TagLoader::instruments, tags);
        }
    }

    @Polymorphic(stringValue = "painting_variant")
    @MessageVariant
    public static class PaintingVariantGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static PaintingVariantGroup create() {
            PaintingVariantGroup ret = new PaintingVariantGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.PAINTING_VARIANT, TagLoader::paintingVariants, tags);
        }
    }

    @Polymorphic(stringValue = "banner_pattern")
    @MessageVariant
    public static class BannerPatternGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static BannerPatternGroup create() {
            BannerPatternGroup ret = new BannerPatternGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.BANNER_PATTERN, TagLoader::bannerPatterns, tags);
        }
    }

    @Polymorphic(stringValue = "point_of_interest_type")
    @MessageVariant
    public static class PointOfInterestTypeGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static PointOfInterestTypeGroup create() {
            PointOfInterestTypeGroup ret = new PointOfInterestTypeGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.POINT_OF_INTEREST_TYPE, TagLoader::pointOfInterestTypes, tags);
        }
    }

    @Polymorphic(stringValue = "cat_variant")
    @MessageVariant
    public static class CatVariantGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static CatVariantGroup create() {
            CatVariantGroup ret = new CatVariantGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.CAT_VARIANT, TagLoader::catVariants, tags);
        }
    }

    @Polymorphic(stringValue = "worldgen/biome")
    @MessageVariant
    public static class BiomeGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        private static BiomeGroup create() {
            BiomeGroup ret = new BiomeGroup();
            ret.tags = new ArrayList<>();
            return ret;
        }

        public static List<Tag> fixTags(List<Tag> tags, @GlobalData RegistryAccess registryManager) {
            return doFixTags(null, () -> TagLoader.biomes(registryManager), tags);
        }
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class OtherGroup extends Group {
        public List<Tag> tags;
    }

    private static List<Tag> doFixTags(
            @Nullable Registry<?> registry,
            Supplier<Map<ResourceLocation, IntList>> vanillaSupplier,
            List<Tag> tags
    ) {
        Map<ResourceLocation, IntList> vanillaTags = vanillaSupplier.get();
        for (Tag tag : tags) {
            vanillaTags.remove(tag.name);
            if (registry != null) {
                for (int i = 0; i < tag.entries.size(); i++) {
                    tag.entries.set(i, PacketSystem.serverRawIdToClient(registry, tag.entries.getInt(i)));
                }
            }
        }
        vanillaTags.forEach((name, entries) -> tags.add(new Tag(name, entries)));
        return tags;
    }

    @MessageVariant
    public static class Tag {
        public ResourceLocation name;
        public IntList entries;

        public Tag() {}
        public Tag(ResourceLocation name, IntList entries) {
            this.name = name;
            this.entries = entries;
        }
    }
}
