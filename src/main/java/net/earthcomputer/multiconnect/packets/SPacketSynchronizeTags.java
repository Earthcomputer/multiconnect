package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Message(minVersion = Protocols.V1_17)
public class SPacketSynchronizeTags {
    @Introduce(compute = "computeGroups")
    public List<Group> groups;

    public static List<Group> computeGroups(
            @Argument("groups") List<Group> oldGroups,
            @DefaultConstruct GameEventGroup gameEventGroup
    ) {
        var ret = new ArrayList<>(oldGroups);
        ret.add(gameEventGroup);
        return ret;
    }

    @Polymorphic
    @Message
    public static abstract class Group {
        public Identifier id;
    }

    @Polymorphic(stringValue = "block")
    @Message
    public static class BlockGroup extends Group {
        public List<Tag> tags;

        @Message
        public static class Tag {
            public Identifier name;
            @Registry(Registries.BLOCK)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "item")
    @Message
    public static class ItemGroup extends Group {
        public List<Tag> tags;

        @Message
        public static class Tag {
            public Identifier name;
            @Registry(Registries.ITEM)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "fluid")
    @Message
    public static class FluidGroup extends Group {
        public List<Tag> tags;

        @Message
        public static class Tag {
            public Identifier name;
            @Registry(Registries.FLUID)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "entity_type")
    @Message
    public static class EntityTypeGroup extends Group {
        public List<Tag> tags;

        @Message
        public static class Tag {
            public Identifier name;
            @Registry(Registries.ENTITY_TYPE)
            public IntList entries;
        }
    }

    @Polymorphic(stringValue = "game_event")
    @Message
    public static class GameEventGroup extends Group {
        public List<Tag> tags;

        @Message
        public static class Tag {
            public Identifier name;
            @Registry(Registries.GAME_EVENT)
            public IntList entries;
        }
    }

    @Polymorphic(otherwise = true)
    @Message
    public static class OtherGroup extends Group {
        public List<Tag> tags;

        @Message
        public static class Tag {
            public Identifier name;
            public IntList entries;
        }
    }
}
