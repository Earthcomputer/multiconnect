package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_12_2.ItemStack_1_12_2;
import net.earthcomputer.multiconnect.packets.v1_13_1.ItemStack_1_13_1;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Blocks_1_12_2;

import java.util.function.Function;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
@Polymorphic
public abstract class Particle_1_18_2 implements CommonTypes.Particle {
    @Registry(Registries.PARTICLE_TYPE)
    @Type(Types.INT)
    public int particleId;

    @Polymorphic(stringValue = {"block", "falling_dust", "multiconnect:block_dust"})
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class BlockState extends Particle_1_18_2 implements CommonTypes.Particle.BlockState {
        @Registry(Registries.BLOCK_STATE)
        @Introduce(compute = "computeBlockStateId")
        public int blockStateId;

        public static int computeBlockStateId(@Argument("blockStateId") int blockStateId) {
            return Blocks_1_12_2.convertToStateRegistryId(blockStateId);
        }
    }

    @Polymorphic(stringValue = "item")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class Item extends Particle_1_18_2 implements CommonTypes.Particle.Item {
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
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class Dust extends Particle_1_18_2 implements CommonTypes.Particle.Dust {
        @Introduce(doubleValue = 1)
        public float red;
        @Introduce(doubleValue = 1)
        public float green;
        @Introduce(doubleValue = 1)
        public float blue;
        @Introduce(doubleValue = 1)
        public float scale;
    }

    @Polymorphic(stringValue = "dust_color_transition")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class DustColorTransition extends Particle_1_18_2 implements CommonTypes.Particle.DustColorTransition {
        public float fromRed;
        public float fromGreen;
        public float fromBlue;
        public float scale;
        public float toRed;
        public float toGreen;
        public float toBlue;
    }

    @Polymorphic(stringValue = "vibration")
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class Vibration extends Particle_1_18_2 implements CommonTypes.Particle.Vibration {
        public CommonTypes.VibrationPath path;
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
    public static class Simple extends Particle_1_18_2 implements CommonTypes.Particle.Simple {
    }
}
