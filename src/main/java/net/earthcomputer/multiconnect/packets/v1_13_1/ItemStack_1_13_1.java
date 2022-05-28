package net.earthcomputer.multiconnect.packets.v1_13_1;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.v1_13_2.ItemStack_1_13_2;
import net.minecraft.nbt.NbtCompound;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_13_1)
@DefaultConstruct(subType = ItemStack_1_13_1.Empty.class)
public abstract class ItemStack_1_13_1 implements CommonTypes.ItemStack {
    @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "computeItemId")
    public short itemId;

    public static short computeItemId(
            @Argument("this") ItemStack_1_13_2 self
    ) {
        if (!self.present) {
            return -1;
        } else {
            return (short) ((ItemStack_1_13_2.NonEmpty) self).itemId;
        }
    }

    @Override
    public boolean isPresent() {
        return itemId != -1;
    }

    @Polymorphic(intValue = -1)
    @MessageVariant(maxVersion = Protocols.V1_13_1)
    public static class Empty extends ItemStack_1_13_1 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(otherwise = true)
    @MessageVariant(maxVersion = Protocols.V1_13_1)
    public static class NonEmpty extends ItemStack_1_13_1 implements CommonTypes.ItemStack.NonEmpty {
        @DefaultConstruct(intValue = 1)
        public byte count;
        public NbtCompound tag;

        @Override
        public int getItemId() {
            return Short.toUnsignedInt(itemId);
        }

        @Override
        public byte getCount() {
            return count;
        }

        @Override
        public NbtCompound getTag() {
            return tag;
        }
    }
}
