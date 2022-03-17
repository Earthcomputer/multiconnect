package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@MessageVariant
@Polymorphic
public abstract class SPacketPlayerList {
    public Action action;

    @Polymorphic(stringValue = "ADD_PLAYER")
    @MessageVariant
    public static class AddPlayer extends SPacketPlayerList {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public String name;
            public List<Property> properties;
            public int gamemode;
            public int ping;
            public Optional<CommonTypes.Text> displayName;

            @MessageVariant
            public static class Property {
                public String name;
                public String value;
                public Optional<String> signature;
            }
        }
    }

    @Polymorphic(stringValue = "UPDATE_GAMEMODE")
    @MessageVariant
    public static class UpdateGamemode extends SPacketPlayerList {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public int gamemode;
        }
    }

    @Polymorphic(stringValue = "UPDATE_LATENCY")
    @MessageVariant
    public static class UpdateLatency extends SPacketPlayerList {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public int ping;
        }
    }

    @Polymorphic(stringValue = "UPDATE_DISPLAY_NAME")
    @MessageVariant
    public static class UpdateDisplayName extends SPacketPlayerList {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public Optional<CommonTypes.Text> displayName;
        }
    }

    @Polymorphic(stringValue = "REMOVE_PLAYER")
    @MessageVariant
    public static class RemovePlayer extends SPacketPlayerList {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
        }
    }

    @NetworkEnum
    public enum Action {
        ADD_PLAYER, UPDATE_GAMEMODE, UPDATE_LATENCY, UPDATE_DISPLAY_NAME, REMOVE_PLAYER
    }
}
