package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
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
        @OnlyIf(field = "flags", condition = "isRedirect")
        public int redirectNode;
        @OnlyIf(field = "flags", condition = "isArgumentOrLiteral")
        public String name;
        @OnlyIf(field = "flags", condition = "isArgument")
        public Argument argument;
        @OnlyIf(field = "flags", condition = "hasSuggestions")
        public Identifier suggestionsProvider;

        public static boolean isRedirect(int flags) {
            return (flags & 8) != 0;
        }

        public static boolean isArgumentOrLiteral(int flags) {
            int type = flags & 3;
            return type == 1 || type == 2;
        }

        public static boolean isArgument(int flags) {
            return (flags & 3) == 2;
        }

        public static boolean hasSuggestions(int flags) {
            return (flags & 16) != 0;
        }
    }

    @Polymorphic
    @Message
    public static abstract class Argument {
        public Identifier parser;
    }

    @Polymorphic(stringValue = "brigadier:double")
    @Message
    public static class DoubleArgument extends Argument {
        public byte flags;
        @OnlyIf(field = "flags", condition = "hasMin")
        public double min;
        @OnlyIf(field = "flags", condition = "hasMax")
        public double max;

        public static boolean hasMin(byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:float")
    @Message
    public static class FloatArgument extends Argument {
        public byte flags;
        @OnlyIf(field = "flags", condition = "hasMin")
        public float min;
        @OnlyIf(field = "flags", condition = "hasMax")
        public float max;

        public static boolean hasMin(byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:integer")
    @Message
    public static class IntArgument extends Argument {
        public byte flags;
        @OnlyIf(field = "flags", condition = "hasMin")
        @Type(Types.INT)
        public int min;
        @OnlyIf(field = "flags", condition = "hasMax")
        @Type(Types.INT)
        public int max;

        public static boolean hasMin(byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:long")
    @Message
    public static class LongArgument extends Argument {
        public byte flags;
        @OnlyIf(field = "flags", condition = "hasMin")
        @Type(Types.LONG)
        public long min;
        @OnlyIf(field = "flags", condition = "hasMax")
        @Type(Types.LONG)
        public long max;

        public static boolean hasMin(byte flags) {
            return (flags & 1) != 0;
        }

        public static boolean hasMax(byte flags) {
            return (flags & 2) != 0;
        }
    }

    @Polymorphic(stringValue = "brigadier:string")
    @Message
    public static class StringArgument extends Argument {
        public Type type;
        public enum Type {
            SINGLE_WORD, QUOTABLE_PHRASE, GREEDY_PHRASE
        }
    }

    @Polymorphic(stringValue = "minecraft:entity")
    @Message
    public static class EntityArgument extends Argument {
        public byte flags;
    }

    @Polymorphic(stringValue = "minecraft:score_holder")
    @Message
    public static class ScoreHolderArgument extends Argument {
        public byte flags;
    }

    @Polymorphic(stringValue = "minecraft:range")
    @Message
    public static class RangeArgument extends Argument {
        public boolean decimals;
    }

    @Polymorphic(otherwise = true)
    @Message
    public static class ConstantArgument extends Argument {
    }
}
