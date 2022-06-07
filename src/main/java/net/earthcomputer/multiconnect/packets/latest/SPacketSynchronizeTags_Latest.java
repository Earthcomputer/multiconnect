package net.earthcomputer.multiconnect.packets.latest;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.CustomFix;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;
import net.earthcomputer.multiconnect.protocols.generic.TagLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketSynchronizeTags_Latest implements SPacketSynchronizeTags {
    @Introduce(compute = "computeGroups")
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

    @Polymorphic
    @MessageVariant
    public static abstract class Group {
        public Identifier id;
    }

    @Polymorphic(stringValue = "block")
    @MessageVariant
    public static class BlockGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.BLOCK, TagLoader::blocks, tags);
        }
    }

    @Polymorphic(stringValue = "item")
    @MessageVariant
    public static class ItemGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.ITEM, TagLoader::items, tags);
        }
    }

    @Polymorphic(stringValue = "fluid")
    @MessageVariant
    public static class FluidGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.FLUID, TagLoader::fluids, tags);
        }
    }

    @Polymorphic(stringValue = "entity_type")
    @MessageVariant
    public static class EntityTypeGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.ENTITY_TYPE, TagLoader::entityTypes, tags);
        }
    }

    @Polymorphic(stringValue = "game_event")
    @MessageVariant
    public static class GameEventGroup extends Group {
        @CustomFix("fixTags")
        public List<Tag> tags;

        public static List<Tag> fixTags(List<Tag> tags) {
            return doFixTags(Registry.GAME_EVENT, TagLoader::gameEvents, tags);
        }
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class OtherGroup extends Group {
        public List<Tag> tags;
    }

    private static List<Tag> doFixTags(
            Registry<?> registry,
            Supplier<Map<Identifier, IntList>> vanillaSupplier,
            List<Tag> tags
    ) {
        Map<Identifier, IntList> vanillaTags = vanillaSupplier.get();
        for (Tag tag : tags) {
            vanillaTags.remove(tag.name);
            for (int i = 0; i < tag.entries.size(); i++) {
                tag.entries.set(i, PacketSystem.serverRawIdToClient(registry, tag.entries.getInt(i)));
            }
        }
        vanillaTags.forEach((name, entries) -> tags.add(new Tag(name, entries)));
        return tags;
    }

    @MessageVariant
    public static class Tag {
        public Identifier name;
        public IntList entries;

        public Tag() {}
        public Tag(Identifier name, IntList entries) {
            this.name = name;
            this.entries = entries;
        }
    }
}
