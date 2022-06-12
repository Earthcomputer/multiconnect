package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.latest.ItemStack_Latest;
import net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_13_1.ItemStack_1_13_1;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Blocks_1_12_2;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class CommonTypes {
    @MessageVariant
    public static class Text {
        public String json;

        public Text() {
        }

        public Text(String json) {
            this.json = json;
        }

        public static Text createLiteral(String value) {
            var text = net.minecraft.text.Text.literal(value);
            Text result = new Text();
            result.json = net.minecraft.text.Text.Serializer.toJson(text);
            return result;
        }
    }

    @Message
    public interface BlockPos {
        net.minecraft.util.math.BlockPos toMinecraft();
    }

    @MessageVariant(minVersion = Protocols.V1_14)
    public static class BlockPos_Latest implements BlockPos {
        @Type(Types.LONG)
        @Introduce(direction = Introduce.Direction.FROM_OLDER, compute = "computePackedData")
        public long packedData;

        public static long computePackedData(@Argument("packedData") long packedData) {
            int x = (int) (packedData >> 38);
            int y = (int) (packedData << 26 >> 52);
            int z = (int) (packedData << 38 >> 38);
            return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
        }

        @Override
        public net.minecraft.util.math.BlockPos toMinecraft() {
            int x = (int) (packedData >> 38);
            int y = (int) (packedData << 52 >> 52);
            int z = (int) (packedData << 26 >> 38);
            return new net.minecraft.util.math.BlockPos(x, y, z);
        }

        public static BlockPos_Latest fromMinecraft(net.minecraft.util.math.BlockPos pos) {
            var result = new BlockPos_Latest();
            result.packedData = ((long)(pos.getX() & 0x3FFFFFF) << 38) | ((long)(pos.getZ() & 0x3FFFFFF) << 12) | (long)(pos.getY() & 0xFFF);
            return result;
        }
    }

    @MessageVariant
    public static class GlobalPos {
        public Identifier dimension;
        public BlockPos pos;
    }

    @MessageVariant
    public static class GameProfile {
        public UUID uuid;
        public String name;
        public List<Property> properties;

        public GameProfile() {
        }

        public GameProfile(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.properties = new ArrayList<>(0);
        }

        @MessageVariant
        public static class Property {
            public String name;
            public String value;
            public Optional<String> signature;
        }
    }

    @MessageVariant
    public static class PublicKey {
        @Type(Types.LONG)
        public long expiresAt;
        public byte[] key;
        public byte[] keySignature;
    }

    @Message
    public interface ItemStack {
        boolean isPresent();

        static ItemStack fromMinecraft(net.minecraft.item.ItemStack stack) {
            if (stack.isEmpty()) {
                return new ItemStack_Latest.Empty();
            } else {
                ItemStack_Latest.NonEmpty newStack = new ItemStack_Latest.NonEmpty();
                newStack.present = true;
                newStack.itemId = net.minecraft.util.registry.Registry.ITEM.getRawId(stack.getItem());
                newStack.count = (byte) stack.getCount();
                newStack.tag = stack.getNbt() != null ? stack.getNbt().copy() : null;
                return newStack;
            }
        }

        @Message
        interface Empty extends ItemStack {
        }

        @Message
        interface NonEmpty extends ItemStack {
            int getItemId();
            byte getCount();
            NbtCompound getTag();
        }
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
            @Registry(Registries.TRACKED_DATA_HANDLER)
            public int handler;

            @Polymorphic(stringValue = "byte")
            @MessageVariant
            public static class Byte extends TrackedData {
                public byte value;
            }

            @Polymorphic(stringValue = "integer")
            @MessageVariant
            public static class VarInt extends TrackedData {
                public int value;
            }

            @Polymorphic(stringValue = "float")
            @MessageVariant
            public static class Float extends TrackedData {
                public float value;
            }

            @Polymorphic(stringValue = "string")
            @MessageVariant
            public static class String extends TrackedData {
                public java.lang.String value;
            }

            @Polymorphic(stringValue = "text_component")
            @MessageVariant
            public static class Text extends TrackedData {
                public CommonTypes.Text value;
            }

            @Polymorphic(stringValue = "optional_text_component")
            @MessageVariant
            public static class OptionalText extends TrackedData {
                public Optional<CommonTypes.Text> value;
            }

            @Polymorphic(stringValue = "item_stack")
            @MessageVariant
            public static class ItemStack extends TrackedData {
                public CommonTypes.ItemStack value;
            }

            @Polymorphic(stringValue = "boolean")
            @MessageVariant
            public static class Boolean extends TrackedData {
                public boolean value;
            }

            @Polymorphic(stringValue = "rotation")
            @MessageVariant
            public static class Rotation extends TrackedData {
                public float x;
                public float y;
                public float z;
            }

            @Polymorphic(stringValue = "block_pos")
            @MessageVariant
            public static class BlockPos extends TrackedData {
                public CommonTypes.BlockPos value;
            }

            @Polymorphic(stringValue = "optional_block_pos")
            @MessageVariant
            public static class OptionalBlockPos extends TrackedData {
                public Optional<CommonTypes.BlockPos> value;
            }

            @Polymorphic(stringValue = "facing")
            @MessageVariant
            public static class Direction extends TrackedData {
                public CommonTypes.Direction value;
            }

            @Polymorphic(stringValue = "optional_uuid")
            @MessageVariant
            public static class OptionalUuid extends TrackedData {
                public Optional<UUID> value;
            }

            @Polymorphic(stringValue = "optional_block_state")
            @MessageVariant
            public static class OptionalBlockState extends TrackedData {
                @Registry(Registries.BLOCK_STATE)
                public int value;
            }

            @Polymorphic(stringValue = "nbt_compound")
            @MessageVariant
            public static class Nbt extends TrackedData {
                public NbtCompound value;
            }

            @Polymorphic(stringValue = "particle")
            @MessageVariant
            public static class Particle extends TrackedData {
                public CommonTypes.Particle value;
            }

            @Polymorphic(stringValue = "villager_data")
            @MessageVariant
            public static class VillagerData extends TrackedData {
                @Registry(Registries.VILLAGER_TYPE)
                public int villagerType;
                @Registry(Registries.VILLAGER_PROFESSION)
                public int villagerProfession;
                public int level;
            }

            @Polymorphic(stringValue = "optional_int")
            @MessageVariant
            public static class OptionalInt extends TrackedData {
                public int value;
            }

            @Polymorphic(stringValue = "entity_pose")
            @MessageVariant
            public static class Pose extends TrackedData {
                @Registry(Registries.ENTITY_POSE)
                public int value;
            }

            @Polymorphic(stringValue = "cat_variant")
            @MessageVariant
            public static class CatVariant extends TrackedData {
                @Registry(Registries.CAT_VARIANT)
                public int value;
            }

            @Polymorphic(stringValue = "frog_variant")
            @MessageVariant
            public static class FrogVariant extends TrackedData {
                @Registry(Registries.FROG_VARIANT)
                public int value;
            }

            @Polymorphic(stringValue = "optional_global_pos")
            @MessageVariant
            public static class OptionalGlobalPos extends TrackedData {
                public Optional<GlobalPos> value;
            }

            @Polymorphic(stringValue = "painting_variant")
            @MessageVariant
            public static class PaintingVariant extends TrackedData {
                @Registry(Registries.PAINTING_VARIANT)
                public int value;
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
        ;

        public static final Formatting[] VALUES = values();
    }

    @Message
    public interface Particle {
        @Message
        interface BlockState {
        }

        @Message
        interface Item {
        }

        @Message
        interface Dust {
        }

        @Message
        interface DustColorTransition {
        }

        @Message
        interface Vibration {
        }

        @Message
        interface Simple {
        }
    }

    @Polymorphic
    @MessageVariant(minVersion = Protocols.V1_13)
    public static abstract class Particle_Latest implements Particle {
        @Registry(Registries.PARTICLE_TYPE)
        @Type(Types.INT)
        public int particleId;

        @Polymorphic(stringValue = {"block", "falling_dust", "multiconnect:block_dust"})
        @MessageVariant(minVersion = Protocols.V1_13)
        public static class BlockState extends Particle_Latest implements Particle.BlockState {
            @Registry(Registries.BLOCK_STATE)
            @Introduce(compute = "computeBlockStateId")
            public int blockStateId;

            public static int computeBlockStateId(@Argument("blockStateId") int blockStateId) {
                return Blocks_1_12_2.convertToStateRegistryId(blockStateId);
            }
        }

        @Polymorphic(stringValue = "item")
        @MessageVariant(minVersion = Protocols.V1_13)
        public static class Item extends Particle_Latest implements Particle.Item {
            @Introduce(compute = "computeStack")
            public CommonTypes.ItemStack stack;

            public static CommonTypes.ItemStack computeStack(
                    @Argument("itemId") int itemId,
                    @Argument("damage") int damage,
                    @FilledArgument(fromVersion = Protocols.V1_12_2, toVersion = Protocols.V1_13) Function<ItemStack_1_12_2, ItemStack_1_13_1> itemStackTranslator
            ) {
                var stack = new ItemStack_1_12_2.NonEmpty();
                stack.itemId = (short) itemId;
                stack.count = 1;
                stack.damage = (short) damage;
                return itemStackTranslator.apply(stack);
            }
        }

        @Polymorphic(stringValue = "dust")
        @MessageVariant(minVersion = Protocols.V1_13)
        public static class Dust extends Particle_Latest implements Particle.Dust {
            @Introduce(doubleValue = 1)
            public float red;
            @Introduce(doubleValue = 1)
            public float green;
            @Introduce(doubleValue = 1)
            public float blue;
            public float scale;
        }

        @Polymorphic(stringValue = "dust_color_transition")
        @MessageVariant(minVersion = Protocols.V1_13)
        public static class DustColorTransition extends Particle_Latest implements Particle.DustColorTransition {
            public float fromRed;
            public float fromGreen;
            public float fromBlue;
            public float scale;
            public float toRed;
            public float toGreen;
            public float toBlue;
        }

        @Polymorphic(stringValue = "vibration")
        @MessageVariant(minVersion = Protocols.V1_13)
        public static class Vibration extends Particle_Latest implements Particle.Vibration {
            public VibrationPath path;
        }

        @Polymorphic(otherwise = true)
        @MessageVariant(minVersion = Protocols.V1_13)
        public static class Simple extends Particle_Latest implements Particle.Simple {
        }
    }

    @Message
    public interface VibrationPath {
    }

    @MessageVariant(minVersion = Protocols.V1_19)
    public static class VibrationPath_Latest implements VibrationPath {
        @Introduce(compute = "computeSource")
        public PositionSource source;
        public int ticks;

        public static PositionSource computeSource(
                @Argument("destX") double destX,
                @Argument("destY") double destY,
                @Argument("destZ") double destZ,
                @DefaultConstruct PositionSource.Block source
        ) {
            source.pos = BlockPos_Latest.fromMinecraft(new net.minecraft.util.math.BlockPos(destX, destY, destZ));
            return source;
        }
    }

    @Polymorphic
    @MessageVariant
    public static abstract class PositionSource {
        @Registry(Registries.POSITION_SOURCE_TYPE)
        public Identifier type;

        @Polymorphic(stringValue = "block")
        @MessageVariant
        public static class Block extends PositionSource {
            public CommonTypes.BlockPos pos;
        }

        @Polymorphic(stringValue = "entity")
        @MessageVariant
        public static class Entity extends PositionSource {
            public int entityId;
        }
    }
}
