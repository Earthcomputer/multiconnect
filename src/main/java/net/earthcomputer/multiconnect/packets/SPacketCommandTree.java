package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

@MessageVariant
public class SPacketCommandTree {
    public List<Node> nodes;
    public int rootIndex;

    @MessageVariant
    public static class Node {
        public int flags;
        public IntList children;
        @OnlyIf("isRedirect")
        public int redirectNode;
        @OnlyIf(value = "isArgumentOrLiteral")
        public String name;
        @OnlyIf(value = "isArgument")
        public BrigadierArgument argument;
        @OnlyIf(value = "hasSuggestions")
        public ResourceLocation suggestionsProvider;

        public static boolean isRedirect(@Argument("flags") int flags) {
            return (flags & 8) != 0;
        }

        public static boolean isArgumentOrLiteral(@Argument("flags") int flags) {
            int type = flags & 3;
            return type == 1 || type == 2;
        }

        public static boolean isArgument(@Argument("flags") int flags) {
            return (flags & 3) == 2;
        }

        public static boolean hasSuggestions(@Argument("flags") int flags) {
            return (flags & 16) != 0;
        }
    }

    @Message
    public interface BrigadierArgument {
        @Message
        interface DoubleArgument {
        }

        @Message
        interface FloatArgument {
        }

        @Message
        interface IntArgument {
        }

        @Message
        interface LongArgument {
        }

        @Message
        interface StringArgument {
        }

        @Message
        interface EntityArgument {
        }

        @Message
        interface ScoreHolderArgument {
        }

        @Message
        interface RegistryKeyArgument {
        }

        @Message
        interface ConstantArgument {
        }
    }

    @Polymorphic
    @MessageVariant(minVersion = Protocols.V1_19)
    public static abstract class BrigadierArgument_Latest implements BrigadierArgument {
        @Registry(Registries.COMMAND_ARGUMENT_TYPE)
        @Introduce(compute = "computeParser")
        public int parser;

        public static int computeParser(@Argument("parser") ResourceLocation parser) {
            Integer rawId = PacketSystem.serverIdToRawId(net.minecraft.core.Registry.COMMAND_ARGUMENT_TYPE, parser);
            return rawId == null ? 0 : rawId;
        }
    }

    @Polymorphic(stringValue = "brigadier:double")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class DoubleArgument extends BrigadierArgument_Latest implements BrigadierArgument.DoubleArgument {
        public byte flags;
        @OnlyIf("hasMin")
        public double min;
        @OnlyIf("hasMax")
        public double max;

        public static boolean hasMin(@Argument("flags") byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(@Argument("flags") byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:float")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class FloatArgument extends BrigadierArgument_Latest implements BrigadierArgument.FloatArgument {
        public byte flags;
        @OnlyIf("hasMin")
        public float min;
        @OnlyIf("hasMax")
        public float max;

        public static boolean hasMin(@Argument("flags") byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(@Argument("flags") byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:integer")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class IntArgument extends BrigadierArgument_Latest implements BrigadierArgument.IntArgument {
        public byte flags;
        @OnlyIf("hasMin")
        @Type(Types.INT)
        public int min;
        @OnlyIf("hasMax")
        @Type(Types.INT)
        public int max;

        public static boolean hasMin(@Argument("flags") byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(@Argument("flags") byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:long")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class LongArgument extends BrigadierArgument_Latest implements BrigadierArgument.LongArgument {
        public byte flags;
        @OnlyIf("hasMin")
        @Type(Types.LONG)
        public long min;
        @OnlyIf("hasMax")
        @Type(Types.LONG)
        public long max;

        public static boolean hasMin(@Argument("flags") byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(@Argument("flags") byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:string")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class StringArgument extends BrigadierArgument_Latest implements BrigadierArgument.StringArgument {
        public net.earthcomputer.multiconnect.packets.SPacketCommandTree.StringArgument.Type type;
        @NetworkEnum
        public enum Type {
            SINGLE_WORD, QUOTABLE_PHRASE, GREEDY_PHRASE
        }
    }

    @Polymorphic(stringValue = "minecraft:entity")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class EntityArgument extends BrigadierArgument_Latest implements BrigadierArgument.EntityArgument {
        public byte flags;
    }

    @Polymorphic(stringValue = "minecraft:score_holder")
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class ScoreHolderArgument extends BrigadierArgument_Latest implements BrigadierArgument.ScoreHolderArgument {
        public byte flags;
    }

    @Polymorphic(stringValue = {"minecraft:resource", "minecraft:resource_or_tag"})
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class RegistryKeyArgument extends BrigadierArgument_Latest implements BrigadierArgument.RegistryKeyArgument {
        public ResourceLocation registry;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_19)
    public static class ConstantArgument extends BrigadierArgument_Latest implements BrigadierArgument.ConstantArgument {
    }
}
