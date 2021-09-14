package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.util.Identifier;

import java.util.List;

@Message
public class SPacketCommandTree {
    public List<Node> nodes;
    public int rootIndex;

    @Message
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
        public Identifier suggestionsProvider;

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

    @Polymorphic
    @Message
    public static abstract class BrigadierArgument {
        public Identifier parser;
    }

    @Polymorphic(stringValue = "brigadier:double")
    @Message
    public static class DoubleArgument extends BrigadierArgument {
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
    @Message
    public static class FloatArgument extends BrigadierArgument {
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
    @Message
    public static class IntArgument extends BrigadierArgument {
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
    @Message
    public static class LongArgument extends BrigadierArgument {
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
    @Message
    public static class StringArgument extends BrigadierArgument {
        public Type type;
        public enum Type {
            SINGLE_WORD, QUOTABLE_PHRASE, GREEDY_PHRASE
        }
    }

    @Polymorphic(stringValue = "minecraft:entity")
    @Message
    public static class EntityArgument extends BrigadierArgument {
        public byte flags;
    }

    @Polymorphic(stringValue = "minecraft:score_holder")
    @Message
    public static class ScoreHolderArgument extends BrigadierArgument {
        public byte flags;
    }

    @Polymorphic(stringValue = "minecraft:range")
    @Message
    public static class RangeArgument extends BrigadierArgument {
        public boolean decimals;
    }

    @Polymorphic(otherwise = true)
    @Message
    public static class ConstantArgument extends BrigadierArgument {
    }
}
