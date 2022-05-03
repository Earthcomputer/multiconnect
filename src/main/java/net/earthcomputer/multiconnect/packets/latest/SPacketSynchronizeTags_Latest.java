package net.earthcomputer.multiconnect.packets.latest;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketSynchronizeTags;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@MessageVariant(minVersion = Protocols.V1_17)
public class SPacketSynchronizeTags_Latest implements SPacketSynchronizeTags {
    @Introduce(compute = "computeGroups")
    public List<Group> groups;

    public static List<Group> computeGroups(
            @Argument("blocks") List<BlockGroup.Tag> blocks,
            @Argument("items") List<ItemGroup.Tag> items,
            @Argument("fluids") List<FluidGroup.Tag> fluids,
            @Argument("entities") List<EntityTypeGroup.Tag> entities,
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
        public List<Tag> tags;

        @MessageVariant
        public static class Tag {
            public Identifier name;
            @Registry(Registries.BLOCK)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "item")
    @MessageVariant
    public static class ItemGroup extends Group {
        public List<Tag> tags;

        @MessageVariant
        public static class Tag {
            public Identifier name;
            @Registry(Registries.ITEM)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "fluid")
    @MessageVariant
    public static class FluidGroup extends Group {
        public List<Tag> tags;

        @MessageVariant
        public static class Tag {
            public Identifier name;
            @Registry(Registries.FLUID)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "entity_type")
    @MessageVariant
    public static class EntityTypeGroup extends Group {
        public List<Tag> tags;

        @MessageVariant
        public static class Tag {
            public Identifier name;
            @Registry(Registries.ENTITY_TYPE)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "game_event")
    @MessageVariant
    public static class GameEventGroup extends Group {
        public List<Tag> tags;

        @MessageVariant
        public static class Tag {
            public Identifier name;
            @Registry(Registries.GAME_EVENT)
            public IntList entries;
        }
    }

    @Polymorphic(otherwise = true)
    @MessageVariant
    public static class OtherGroup extends Group {
        public List<Tag> tags;

        @MessageVariant
        public static class Tag {
            public Identifier name;
            public IntList entries;
        }
    }
}
