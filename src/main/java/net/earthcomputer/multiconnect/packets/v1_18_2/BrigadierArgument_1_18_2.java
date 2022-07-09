package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.OnlyIf;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketCommands;
import net.minecraft.resources.ResourceLocation;

@Polymorphic
@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
public abstract class BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument {
    public ResourceLocation parser;

    @Polymorphic(stringValue = "brigadier:double")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class DoubleArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.DoubleArgument {
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
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class FloatArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.FloatArgument {
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
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class IntArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.IntArgument {
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
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class LongArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.LongArgument {
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
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class StringArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.StringArgument {
        public SPacketCommands.StringArgument.Type type;
        @NetworkEnum
        public enum Type {
            SINGLE_WORD, QUOTABLE_PHRASE, GREEDY_PHRASE
        }
    }

    @Polymorphic(stringValue = "minecraft:entity")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class EntityArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.EntityArgument {
        public byte flags;
    }

    @Polymorphic(stringValue = "minecraft:score_holder")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class ScoreHolderArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.ScoreHolderArgument {
        public byte flags;
    }

    @Polymorphic(stringValue = {"minecraft:resource", "minecraft:resource_or_tag"})
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class RegistryKeyArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.RegistryKeyArgument {
        public ResourceLocation registry;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class ConstantArgument extends BrigadierArgument_1_18_2 implements SPacketCommands.BrigadierArgument.ConstantArgument {
    }
}
