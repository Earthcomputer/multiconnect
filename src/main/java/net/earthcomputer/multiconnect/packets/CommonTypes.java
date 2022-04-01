package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.minecraft.nbt.NbtCompound;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class CommonTypes {
    @MessageVariant
    public static class Text {
        public String json;
    }

    @MessageVariant
    public static class BlockPos {
        @Type(Types.LONG)
        public long packedData;
    }

    @Polymorphic
    @MessageVariant
    @DefaultConstruct(subType = EmptyItemStack.class)
    public static abstract class ItemStack {
        public boolean present;

        public static ItemStack fromMinecraft(net.minecraft.item.ItemStack stack) {
            if (stack.isEmpty()) {
                return new EmptyItemStack();
            } else {
                NonEmptyItemStack newStack = new NonEmptyItemStack();
                newStack.present = true;
                newStack.itemId = net.minecraft.util.registry.Registry.ITEM.getRawId(stack.getItem());
                newStack.count = (byte) stack.getCount();
                newStack.tag = stack.getTag() != null ? stack.getTag().copy() : null;
                return newStack;
            }
        }
    }

    @Polymorphic(booleanValue = false)
    @MessageVariant
    public static class EmptyItemStack extends ItemStack {
    }

    @Polymorphic(booleanValue = true)
    @MessageVariant
    public static class NonEmptyItemStack extends ItemStack {
        @Registry(Registries.ITEM)
        public int itemId;
        @DefaultConstruct(intValue = 1)
        public byte count;
        public NbtCompound tag;
    }

    @Polymorphic
    @MessageVariant(tailrec = true)
    public static abstract class EntityTrackerEntry {
        @Type(Types.UNSIGNED_BYTE)
        public int field;

        @Polymorphic(intValue = 255)
        @MessageVariant
        public static class Empty extends EntityTrackerEntry {
        }

        @Polymorphic(otherwise = true)
        @MessageVariant
        public static class Other extends EntityTrackerEntry {
            public TrackedData trackedData;
            public EntityTrackerEntry next;
        }

        @Polymorphic
        @MessageVariant
        public static abstract class TrackedData {
            public Handler handler;

            @Polymorphic(stringValue = "BYTE")
            @MessageVariant
            public static class Byte extends TrackedData {
                public byte value;
            }

            @Polymorphic(stringValue = "VAR_INT")
            @MessageVariant
            public static class VarInt extends TrackedData {
                public int value;
            }

            @Polymorphic(stringValue = "FLOAT")
            @MessageVariant
            public static class Float extends TrackedData {
                public float value;
            }

            @Polymorphic(stringValue = "STRING")
            @MessageVariant
            public static class String extends TrackedData {
                public java.lang.String value;
            }

            @Polymorphic(stringValue = "TEXT")
            @MessageVariant
            public static class Text extends TrackedData {
                public CommonTypes.Text value;
            }

            @Polymorphic(stringValue = "OPTIONAL_TEXT")
            @MessageVariant
            public static class OptionalText extends TrackedData {
                public Optional<CommonTypes.Text> value;
            }

            @Polymorphic(stringValue = "ITEM_STACK")
            @MessageVariant
            public static class ItemStack extends TrackedData {
                public CommonTypes.ItemStack value;
            }

            @Polymorphic(stringValue = "BOOLEAN")
            @MessageVariant
            public static class Boolean extends TrackedData {
                public boolean value;
            }

            @Polymorphic(stringValue = "ROTATION")
            @MessageVariant
            public static class Rotation extends TrackedData {
                public float x;
                public float y;
                public float z;
            }

            @Polymorphic(stringValue = "BLOCK_POS")
            @MessageVariant
            public static class BlockPos extends TrackedData {
                public CommonTypes.BlockPos value;
            }

            @Polymorphic(stringValue = "OPTIONAL_BLOCK_POS")
            @MessageVariant
            public static class OptionalBlockPos extends TrackedData {
                public Optional<CommonTypes.BlockPos> value;
            }

            @Polymorphic(stringValue = "DIRECTION")
            @MessageVariant
            public static class Direction extends TrackedData {
                public CommonTypes.Direction value;
            }

            @Polymorphic(stringValue = "OPTIONAL_UUID")
            @MessageVariant
            public static class OptionalUuid extends TrackedData {
                public Optional<UUID> value;
            }

            @Polymorphic(stringValue = "OPTIONAL_BLOCK_STATE")
            @MessageVariant
            public static class OptionalBlockState extends TrackedData {
                @Registry(Registries.BLOCK_STATE)
                public OptionalInt value;
            }

            @Polymorphic(stringValue = "NBT")
            @MessageVariant
            public static class Nbt extends TrackedData {
                public NbtCompound value;
            }

            @Polymorphic(stringValue = "PARTICLE")
            @MessageVariant
            public static class Particle extends TrackedData {
                public CommonTypes.Particle value;
            }

            @Polymorphic(stringValue = "VILLAGER_DATA")
            @MessageVariant
            public static class VillagerData extends TrackedData {
                @Registry(Registries.VILLAGER_TYPE)
                public int villagerType;
                @Registry(Registries.VILLAGER_PROFESSION)
                public int villagerProfession;
                public int level;
            }

            @Polymorphic(stringValue = "OPTIONAL_VAR_INT")
            @MessageVariant
            public static class OptionalVarInt extends TrackedData {
                public OptionalInt value;
            }

            @Polymorphic(stringValue = "POSE")
            @MessageVariant
            public static class Pose extends TrackedData {
                public CommonTypes.Pose value;
            }

            @NetworkEnum
            public enum Handler {
                BYTE,
                VAR_INT,
                FLOAT,
                STRING,
                TEXT,
                OPTIONAL_TEXT,
                ITEM_STACK,
                BOOLEAN,
                ROTATION,
                BLOCK_POS,
                OPTIONAL_BLOCK_POS,
                DIRECTION,
                OPTIONAL_UUID,
                OPTIONAL_BLOCK_STATE,
                NBT,
                PARTICLE,
                VILLAGER_DATA,
                OPTIONAL_VAR_INT,
                POSE
            }
        }
    }

    @NetworkEnum
    public enum SoundCategory {
        MASTER, MUSIC, RECORDS, WEATHER, BLOCKS, HOSTILE, NEUTRAL, PLAYERS, AMBIENT, VOICE
    }

    @NetworkEnum
    public enum Hand {
        MAIN_HAND, OFF_HAND
    }

    @NetworkEnum
    public enum Direction {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

    @NetworkEnum
    public enum Pose {
        STANDING, FALL_FLYING, SLEEPING, SWIMMING, SPIN_ATTACK, SNEAKING, DYING, LONG_JUMPING
    }

    @NetworkEnum
    public enum Formatting {
        BLACK,
        DARK_BLUE,
        DARK_GREEN,
        DARK_AQUA,
        DARK_RED,
        DARK_PURPLE,
        GOLD,
        GRAY,
        DARK_GRAY,
        BLUE,
        GREEN,
        AQUA,
        RED,
        LIGHT_PURPLE,
        YELLOW,
        WHITE,
        OBFUSCATED,
        BOLD,
        STRIKETHROUGH,
        UNDERLINE,
        ITALIC,
        RESET,
    }

    @Polymorphic
    @MessageVariant
    public static abstract class Particle {
        @Registry(Registries.PARTICLE_TYPE)
        @Type(Types.INT)
        public int particleId;

        @Polymorphic(stringValue = {"block", "falling_dust"})
        @MessageVariant
        public static class BlockStateParticlePacket extends Particle {
            @Registry(Registries.BLOCK_STATE)
            public int blockStateId;
        }

        @Polymorphic(stringValue = "item")
        @MessageVariant
        public static class ItemParticlePacket extends Particle {
            public CommonTypes.ItemStack stack;
        }

        @Polymorphic(stringValue = "dust")
        @MessageVariant
        public static class DustParticlePacket extends Particle {
            public float red;
            public float green;
            public float blue;
            public float scale;
        }

        @Polymorphic(stringValue = "dust_color_transition")
        @MessageVariant
        public static class DustColorTransitionPacket extends Particle {
            public float fromRed;
            public float fromGreen;
            public float fromBlue;
            public float scale;
            public float toRed;
            public float toGreen;
            public float toBlue;
        }

        @Polymorphic(stringValue = "vibration")
        @MessageVariant
        public static class VibrationPacket extends Particle {
            public double originX;
            public double originY;
            public double originZ;
            public double destX;
            public double destY;
            public double destZ;
            @Type(Types.INT)
            public int ticks;
        }

        @Polymorphic(otherwise = true)
        @MessageVariant
        public static class SimpleParticlePacket extends Particle {
        }
    }
}
