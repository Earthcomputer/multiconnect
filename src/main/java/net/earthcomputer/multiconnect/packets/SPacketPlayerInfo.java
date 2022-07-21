package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@MessageVariant
@Polymorphic
public abstract class SPacketPlayerInfo {
    public Action action;

    @Polymorphic(stringValue = "ADD_PLAYER")
    @MessageVariant
    public static class AddPlayer extends SPacketPlayerInfo {
        public List<Player> players;

        @Message
        public interface Player {
        }

        @MessageVariant(minVersion = Protocols.V1_19)
        public static class Player_Latest implements Player {
            public CommonTypes.GameProfile profile;
            public int gamemode;
            public int ping;
            public Optional<CommonTypes.Text> displayName;
            @Introduce(defaultConstruct = true)
            public Optional<CommonTypes.PublicKey> profilePublicKey;
        }
    }

    @Polymorphic(stringValue = "UPDATE_GAMEMODE")
    @MessageVariant
    public static class UpdateGamemode extends SPacketPlayerInfo {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public int gamemode;
        }
    }

    @Polymorphic(stringValue = "UPDATE_LATENCY")
    @MessageVariant
    public static class UpdateLatency extends SPacketPlayerInfo {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public int ping;
        }
    }

    @Polymorphic(stringValue = "UPDATE_DISPLAY_NAME")
    @MessageVariant
    public static class UpdateDisplayName extends SPacketPlayerInfo {
        public List<Player> players;

        @MessageVariant
        public static class Player {
            public UUID uuid;
            public Optional<CommonTypes.Text> displayName;
        }
    }

    @Polymorphic(stringValue = "REMOVE_PLAYER")
    @MessageVariant
    public static class RemovePlayer extends SPacketPlayerInfo {
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
