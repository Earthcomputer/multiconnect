package net.earthcomputer.multiconnect.packets.v1_15_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.DefaultConstruct;
import net.earthcomputer.multiconnect.ap.FilledArgument;
import net.earthcomputer.multiconnect.ap.Introduce;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Polymorphic;
import net.earthcomputer.multiconnect.ap.Registries;
import net.earthcomputer.multiconnect.ap.Registry;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.UUID;

@Polymorphic
@MessageVariant(maxVersion = Protocols.V1_15_2)
@DefaultConstruct(subType = ItemStack_1_15_2.Empty.class)
public abstract class ItemStack_1_15_2 implements CommonTypes.ItemStack {
    public boolean present;

    @Override
    public boolean isPresent() {
        return present;
    }

    @Polymorphic(booleanValue = false)
    @MessageVariant(maxVersion = Protocols.V1_15_2)
    public static class Empty extends ItemStack_1_15_2 implements CommonTypes.ItemStack.Empty {
    }

    @Polymorphic(booleanValue = true)
    @MessageVariant(maxVersion = Protocols.V1_15_2)
    public static class NonEmpty extends ItemStack_1_15_2 implements CommonTypes.ItemStack.NonEmpty {
        @Registry(Registries.ITEM)
        public int itemId;
        @DefaultConstruct(intValue = 1)
        public byte count;
        @Introduce(direction = Introduce.Direction.FROM_NEWER, compute = "translateTagServerbound")
        public NbtCompound tag;

        @Override
        public int getItemId() {
            return itemId;
        }

        @Override
        public byte getCount() {
            return count;
        }

        @Override
        public NbtCompound getTag() {
            return tag;
        }

        public static NbtCompound translateTagServerbound(
                @Argument("itemId") int itemId,
                @FilledArgument(fromRegistry = @FilledArgument.FromRegistry(registry = Registries.ITEM, value = "player_head")) int playerHeadId,
                @Argument("tag") NbtCompound tag
        ) {
            if (itemId != playerHeadId || tag == null) {
                return tag;
            }

            if (tag.contains("SkullOwner", NbtElement.COMPOUND_TYPE)) {
                NbtCompound skullOwner = tag.getCompound("SkullOwner");
                if (skullOwner.containsUuid("Id")) {
                    UUID uuid = skullOwner.getUuid("Id");
                    skullOwner.putString("Id", uuid.toString());
                }
            }

            return tag;
        }
    }
}
